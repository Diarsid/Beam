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
import diarsid.beam.core.base.analyze.variantsweight.Analyze;
import diarsid.beam.core.base.analyze.variantsweight.Variants;
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
import diarsid.support.objects.Pool;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.entitiesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.os.treewalking.base.FileSearchMode.FILES_ONLY;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;


class ProgramsKeeperWorker implements ProgramsKeeper {
    
    private final InnerIoEngine ioEngine;
    private final Walker walker;
    private final Analyze analyze;
    private final Pool<KeeperLoopValidationDialog> dialogPool;
    private final ProgramsCatalog programsCatalog;
    private final Set<CommandType> operatingCommandTypes;
    private final Help enterProgramNameHelp;
    private final Help chooseOneProgramHelp;
    private boolean useWalker;
    
    ProgramsKeeperWorker(
            InnerIoEngine ioEngine, 
            Walker walker,
            Analyze analyze, 
            Pool<KeeperLoopValidationDialog> dialogPool,
            ProgramsCatalog programsCatalog) {
        this.ioEngine = ioEngine;
        this.walker = walker;
        this.analyze = analyze;
        this.dialogPool = dialogPool;
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
        return valueFlowDoneWith(this.programsCatalog.findProgramByDirectName(name));
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
        
        try (KeeperLoopValidationDialog dialog = this.dialogPool.give()) {
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
            case DONE : {
                if ( fileFlow.asDone().hasValue() ) {
                    Optional<Program> program = this.programsCatalog.toProgram(
                            fileFlow.asDone().orThrow());
                    return valueFlowDoneWith(program);
                } else if ( fileFlow.asDone().hasMessage() ) {
                    return valueFlowDoneEmpty(fileFlow.asDone().message());
                } else {
                    return valueFlowDoneEmpty(format(
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
            if ( flow.isDoneEmpty() ) {
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
            return valueFlowDoneEmpty();
        }
    }
    
    private ValueFlow<Program> chooseOneProgram(
            Initiator initiator, String pattern, List<Program> programs) {
        if ( hasOne(programs) ) {
            Program program = getOne(programs);
            if ( this.analyze.isEntitySatisfiable(pattern, program) ) {
                return valueFlowDoneWith(program);
            } else {
                return valueFlowDoneEmpty();
            }            
        } else if ( hasMany(programs) ) {
            Variants variants = this.analyze.weightVariants(
                    pattern, entitiesToVariants(programs));
            if ( variants.isEmpty() ) {
                return valueFlowDoneEmpty();
            }
            Answer answer = this.ioEngine.ask(
                    initiator, variants, this.chooseOneProgramHelp);
            if ( answer.isGiven() ) {
                return valueFlowDoneWith(programs.get(answer.index()));
            } else {
                if ( answer.isRejection() ) {
                    return valueFlowStopped();
                } else if ( answer.variantsAreNotSatisfactory() ) {
                    return valueFlowDoneEmpty();
                } else {
                    return valueFlowDoneEmpty();
                }
            }
        } else {
            return valueFlowDoneEmpty();
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
        return valueFlowDoneWith(entitiesToOptionalMessageWithHeader(
                    "all Programs:", this.programsCatalog.getAll()));
    }
}
