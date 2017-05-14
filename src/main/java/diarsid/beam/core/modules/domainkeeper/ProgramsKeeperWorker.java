/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.Program;

import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.domain.patternsanalyze.Analyze.analyzeAndWeightVariants;


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
    public Optional<Program> findByExactName(Initiator initiator, String name) {
        return this.programsCatalog.findProgramByDirectName(name);
    }

    @Override
    public Optional<Program> findProgram(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_PROGRAM) ) {
            return Optional.empty();
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.joinedArguments();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return Optional.empty();
        }
        
        return this.findByNamePattern(initiator, name);        
    }

    @Override
    public Optional<Program> findByNamePattern(Initiator initiator, String pattern) {
        List<Program> foundPrograms = this.programsCatalog.findProgramsByWholePattern(pattern);
        if ( nonEmpty(foundPrograms) ) {
            return this.chooseOneFromMany(initiator, pattern, foundPrograms);
        } else {
            foundPrograms = this.programsCatalog.findProgramsByPatternSimilarity(pattern);
            if ( nonEmpty(foundPrograms) ) {
                return this.chooseOneFromMany(initiator, pattern, foundPrograms);
            } else {
                return Optional.empty();
            }
        }
    }
    
    private Optional<Program> chooseOneFromMany(
            Initiator initiator, String pattern, List<Program> programs) {
        if ( hasOne(programs) ) {
            return Optional.of(getOne(programs));
        } else if ( hasMany(programs) ) {
            Answer answer = this.ioEngine.chooseInWeightedVariants(
                    initiator, analyzeAndWeightVariants(pattern, entitiesToVariants(programs)));
            if ( answer.isGiven() ) {
                return Optional.of(programs.get(answer.index()));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
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
