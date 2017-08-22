/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.Program;
import diarsid.beam.core.base.patternsanalyze.WeightedVariants;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.patternsanalyze.Analyze.weightVariants;


class ProgramsKeeperWorker 
        implements 
                ProgramsKeeper, 
                NamedEntitiesKeeper {
    
    private final InnerIoEngine ioEngine;
    private final ProgramsCatalog programsCatalog;
    private final KeeperDialogHelper helper;
    private final Set<CommandType> operatingCommandTypes;
    
    ProgramsKeeperWorker(
            InnerIoEngine ioEngine, 
            ProgramsCatalog programsCatalog, 
            KeeperDialogHelper keeperDialogHelper) {
        this.ioEngine = ioEngine;
        this.programsCatalog = programsCatalog;
        this.helper = keeperDialogHelper;
        this.operatingCommandTypes = toSet(RUN_PROGRAM);
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.operatingCommandTypes.contains(command.type());
    }

    @Override
    public ValueOperation<Program> findByExactName(Initiator initiator, String name) {
        return valueCompletedWith(this.programsCatalog.findProgramByDirectName(name));
    }

    @Override
    public ValueOperation<Program> findProgram(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_PROGRAM) ) {
            return valueOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.joinedArguments();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return valueOperationStopped();
        }
        
        return this.findByNamePattern(initiator, name);        
    }

    @Override
    public ValueOperation<Program> findByNamePattern(Initiator initiator, String pattern) {
        List<Program> foundPrograms = this.programsCatalog.findProgramsByWholePattern(pattern);
        if ( nonEmpty(foundPrograms) ) {
            return this.chooseOneFromMany(initiator, pattern, foundPrograms);
        } else {
            foundPrograms = this.programsCatalog.findProgramsByPatternSimilarity(pattern);
            if ( nonEmpty(foundPrograms) ) {
                return this.chooseOneFromMany(initiator, pattern, foundPrograms);
            } else {
                return valueCompletedEmpty();
            }
        }
    }
    
    private ValueOperation<Program> chooseOneFromMany(
            Initiator initiator, String pattern, List<Program> programs) {
        if ( hasOne(programs) ) {
            return valueCompletedWith(getOne(programs));
        } else if ( hasMany(programs) ) {
            WeightedVariants variants = weightVariants(pattern, entitiesToVariants(programs));
            if ( variants.isEmpty() ) {
                return valueCompletedEmpty();
            }
            Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
            if ( answer.isGiven() ) {
                return valueCompletedWith(programs.get(answer.index()));
            } else {
                if ( answer.isRejection() ) {
                    return valueOperationStopped();
                } else if ( answer.variantsAreNotSatisfactory() ) {
                    return valueCompletedEmpty();
                } else {
                    return valueCompletedEmpty();
                }
            }
        } else {
            return valueCompletedEmpty();
        }
    }

    @Override
    public List<Program> getProgramsByPattern(Initiator initiator, String pattern) {
        List<Program> foundPrograms = this.programsCatalog.findProgramsByWholePattern(pattern);
        if ( nonEmpty(foundPrograms) ) {
            return foundPrograms;
        } else {
            return this.programsCatalog.findProgramsByPatternSimilarity(pattern);
        }
    }
}
