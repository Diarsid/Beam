/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.Program;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;

import diarsid.beam.core.base.control.flow.ValueFlow;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;


class ProgramsKeeperWorker 
        implements 
                ProgramsKeeper, 
                NamedEntitiesKeeper {
    
    private final InnerIoEngine ioEngine;
    private final ProgramsCatalog programsCatalog;
    private final KeeperDialogHelper helper;
    private final Set<CommandType> operatingCommandTypes;
    private final Help chooseOneProgramHelp;
    
    ProgramsKeeperWorker(
            InnerIoEngine ioEngine, 
            ProgramsCatalog programsCatalog, 
            KeeperDialogHelper keeperDialogHelper) {
        this.ioEngine = ioEngine;
        this.programsCatalog = programsCatalog;
        this.helper = keeperDialogHelper;
        this.operatingCommandTypes = toSet(RUN_PROGRAM);
        this.chooseOneProgramHelp = this.ioEngine.addToHelpContext(
                "Choose one Program.",
                "Use:",
                "   - number to choose Program",
                "   - Program name part to choose it",
                "   - n/no to see more variants, if any",
                "   - dot to break");
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.operatingCommandTypes.contains(command.type());
    }

    @Override
    public ValueFlow<Program> findByExactName(Initiator initiator, String name) {
        return valueFlowCompletedWith(this.programsCatalog.findProgramByDirectName(name));
    }

    @Override
    public ValueFlow<Program> findProgram(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_PROGRAM) ) {
            return valueFlowFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.joinedArguments();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return valueFlowStopped();
        }
        
        return this.findByNamePattern(initiator, name);        
    }

    @Override
    public ValueFlow<Program> findByNamePattern(Initiator initiator, String pattern) {
        List<Program> foundPrograms = this.programsCatalog.findProgramsByWholePattern(pattern);
        if ( nonEmpty(foundPrograms) ) {
            return this.chooseOneFromMany(initiator, pattern, foundPrograms);
        } else {
            foundPrograms = this.programsCatalog.findProgramsByPatternSimilarity(pattern);
            if ( nonEmpty(foundPrograms) ) {
                return this.chooseOneFromMany(initiator, pattern, foundPrograms);
            } else {
                return valueFlowCompletedEmpty();
            }
        }
    }
    
    private ValueFlow<Program> chooseOneFromMany(
            Initiator initiator, String pattern, List<Program> programs) {
        if ( hasOne(programs) ) {
            return valueFlowCompletedWith(getOne(programs));
        } else if ( hasMany(programs) ) {
            WeightedVariants variants = weightVariants(pattern, entitiesToVariants(programs));
            if ( variants.isEmpty() ) {
                return valueFlowCompletedEmpty();
            }
            Answer answer = this.ioEngine.chooseInWeightedVariants(
                    initiator, variants, this.chooseOneProgramHelp);
            if ( answer.isGiven() ) {
                return valueFlowCompletedWith(programs.get(answer.index()));
            } else {
                if ( answer.isRejection() ) {
                    return valueFlowStopped();
                } else if ( answer.variantsAreNotSatisfactory() ) {
                    return valueFlowCompletedEmpty();
                } else {
                    return valueFlowCompletedEmpty();
                }
            }
        } else {
            return valueFlowCompletedEmpty();
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
