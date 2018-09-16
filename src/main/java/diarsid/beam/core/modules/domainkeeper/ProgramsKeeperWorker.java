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
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.os.treewalking.advanced.Walker;
import diarsid.beam.core.domain.entities.Program;

import static java.lang.String.format;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.isEntitySatisfiable;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.entitiesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.os.treewalking.base.FileSearchMode.FILES_ONLY;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;
import static diarsid.support.objects.Pools.giveBackToPool;
import static diarsid.support.objects.Pools.takeFromPool;


class ProgramsKeeperWorker implements ProgramsKeeper {
    
    private final InnerIoEngine ioEngine;
    private final Walker walker;
    private final ProgramsCatalog programsCatalog;
    private final Set<CommandType> operatingCommandTypes;
    private final Help enterProgramNameHelp;
    private final Help chooseOneProgramHelp;
    private boolean useWalker;
    
    ProgramsKeeperWorker(
            InnerIoEngine ioEngine, 
            Walker walker,
            ProgramsCatalog programsCatalog) {
        this.ioEngine = ioEngine;
        this.walker = walker;
        this.programsCatalog = programsCatalog;
        this.operatingCommandTypes = toSet(RUN_PROGRAM);
        this.enterProgramNameHelp = this.ioEngine.addToHelpContext(
                "Enter Program name.", 
                "Programs are executable files placed in configured catalog.", 
                "Currently this catalog is " + 
                        this.programsCatalog.path().toAbsolutePath().toString());
        this.chooseOneProgramHelp = this.ioEngine.addToHelpContext(
                "Choose one Program.",
                "Use:",
                "   - number to choose Program",
                "   - Program name part to choose it",
                "   - n/no to see more variants, if any",
                "   - dot to break");
        this.useWalker = true;
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
                
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        try {
            name = dialog
                    .withInitialArgument(name)
                    .withRule(ENTITY_NAME_RULE)
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(
                                initiator, "name", this.enterProgramNameHelp);
                    })
                    .withOutputDestination((validationFail) -> {
                        this.ioEngine.report(initiator, validationFail);
                    })
                    .validateAndGet();
        } finally {
            giveBackToPool(dialog);
        }
        if ( name.isEmpty() ) {
            return valueFlowStopped();
        }
        
        return this.findByNamePattern(initiator, name);        
    }

    @Override
    public ValueFlow<Program> findByNamePattern(Initiator initiator, String pattern) {
        if ( this.useWalker ) {
            return this.findProgramInCatalogUsingWalker(initiator, pattern);
        } else {
            return this.findProgramInCatalogDirectly(initiator, pattern);
        }
    }
    
    private ValueFlow<Program> findProgramInCatalogUsingWalker(
            Initiator initiator, String pattern) {
        ValueFlow<String> fileFlow = this.walker
                .lookingFor(FILES_ONLY)
                .walkToFind(pattern)
                .in(this.programsCatalog)
                .by(initiator)
                .andGetResult();
        
        switch ( fileFlow.result() ) {
            case COMPLETE : {
                if ( fileFlow.asComplete().hasValue() ) {
                    Optional<Program> program = this.programsCatalog.toProgram(
                            fileFlow.asComplete().orThrow());
                    return valueFlowCompletedWith(program);
                } else if ( fileFlow.asComplete().hasMessage() ) {
                    return valueFlowCompletedEmpty(fileFlow.asComplete().message());
                } else {
                    return valueFlowCompletedEmpty(format(
                            "'%s' not found in %s", pattern, this.programsCatalog.name()));
                }
            }    
            case FAIL : {
                return valueFlowFail(fileFlow.asFail().reason());
            }    
            case STOP : {
                return valueFlowStopped();
            }    
            default : {
                return valueFlowFail("Unknown flow result.");
            }
        }
    }
    
    private ValueFlow<Program> findProgramInCatalogDirectly(
            Initiator initiator, String pattern) {
        List<Program> foundPrograms = this.programsCatalog.findProgramsByWholePattern(pattern);
        if ( nonEmpty(foundPrograms) ) {
            ValueFlow<Program> flow = this.chooseOneProgram(initiator, pattern, foundPrograms);
            if ( flow.isCompletedEmpty() ) {
                foundPrograms = this.programsCatalog.findProgramsByPatternSimilarity(pattern);
            } else {
                return flow;
            }
        } else {
            foundPrograms = this.programsCatalog.findProgramsByPatternSimilarity(pattern);            
        }
        
        if ( nonEmpty(foundPrograms) ) {
            return this.chooseOneProgram(initiator, pattern, foundPrograms);
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    private ValueFlow<Program> chooseOneProgram(
            Initiator initiator, String pattern, List<Program> programs) {
        if ( hasOne(programs) ) {
            Program program = getOne(programs);
            if ( isEntitySatisfiable(pattern, program) ) {
                return valueFlowCompletedWith(program);
            } else {
                return valueFlowCompletedEmpty();
            }            
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

    @Override
    public ValueFlow<Message> findAll(Initiator initiator) {
        return valueFlowCompletedWith(entitiesToOptionalMessageWithHeader(
                    "all Programs:", this.programsCatalog.getAll()));
    }
}
