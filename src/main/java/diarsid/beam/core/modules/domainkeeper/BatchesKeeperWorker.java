/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoBatches;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPatternChoices;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.isNameSatisfiable;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightStrings;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.PERFECT;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.entitiesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_BATCH;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_DOMAIN_CHARS;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.COMMANDS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.Validities.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.Validities.validationOk;
import static diarsid.support.objects.Pools.giveBackToPool;
import static diarsid.support.objects.Pools.takeFromPool;

class BatchesKeeperWorker implements BatchesKeeper {
    
    private final Object allBatchesConsistencyLock;
    private final ResponsiveDaoBatches dao;
    private final ResponsiveDaoPatternChoices daoPatternChoices;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final Interpreter interpreter;
    private final PropertyAndTextParser propertyAndTextParser;
    private final Set<CommandType> subjectedCommandTypes;
    private final Help enterBatchNameHelp;
    private final Help chooseBatchNameHelp;
    private final Help enterNewBatchNameHelp;
    private final Help enterNewBatchCommandHelp;
    private final Help editCommandModeHelp;
    private final Help chooseOneCommandToEditHelp;
    private final Help editOneCommandHelp;
    
    BatchesKeeperWorker(
            ResponsiveDaoBatches daoBatches, 
            ResponsiveDaoPatternChoices daoPatternChoices,
            CommandsMemoryKeeper commandsMemoryKeeper,
            InnerIoEngine ioEngine,
            KeeperDialogHelper helper,
            Interpreter interpreter,
            PropertyAndTextParser propertyAndTextParser) {
        this.allBatchesConsistencyLock = new Object();
        this.dao = daoBatches;
        this.daoPatternChoices = daoPatternChoices;
        this.commandsMemory = commandsMemoryKeeper;
        this.ioEngine = ioEngine;
        this.helper = helper;
        this.interpreter = interpreter;
        this.propertyAndTextParser = propertyAndTextParser;
        this.subjectedCommandTypes = toSet(CALL_BATCH);
        this.enterBatchNameHelp = this.ioEngine.addToHelpContext(
                "Enter batch name.", 
                "Name must be unique and must not contain path separators",
                "nor following characters: " + 
                        UNACCEPTABLE_DOMAIN_CHARS.stream().collect(joining())
        );
        this.chooseBatchNameHelp = this.ioEngine.addToHelpContext(
                "Choose batch from given variants.",
                "Use:",
                "   - number of batch to choose it",
                "   - part of batch name to choose it",
                "   - n/no to see another batches, if any",
                "   - dot to break"
        );
        this.enterNewBatchNameHelp = this.ioEngine.addToHelpContext(
                "Enter new Batch name. ",
                "It should be unique and should not contain special characters."
        );
        this.enterNewBatchCommandHelp = this.ioEngine.addToHelpContext(
                "Enter new command.",
                "Following commands can be added into Batch:",
                "   - browse WebPage;",
                "   - open Location;",
                "   - open target in Location;",
                "   - run Program;",
                "   - call anything by name;",
                "   - pause with specified time.",
                "In order to stop batch recording, print empty line or dot."
        );
        this.editCommandModeHelp = this.ioEngine.addToHelpContext(
                "Choose whether you want edit just one command in Batch",
                "or rewrite entire Batch with a new set of commands.",
                "Use:",
                "   - number of mode to choose it",
                "   - mode name to choose it",
                "   - dot, n/no or nothing to break"
        );
        this.chooseOneCommandToEditHelp = this.ioEngine.addToHelpContext(
                "Choose number of command you want to edit.",
                "Print:",
                "   - number of command to choose it",
                "   - part of command to choose it",
                "   - dot, n/no or nothing to break",
                "Chosen command will be replaced with a new one you will add.");
        this.editOneCommandHelp = this.ioEngine.addToHelpContext(
                "Enter new command to save.",
                "Following commands can be accepted:",
                "   - browse WebPage;",
                "   - open Location;",
                "   - open target in Location;",
                "   - run Program;",
                "   - call anything by name;",
                "   - pause with specified time.");
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }

    private void asyncCleanCommandsMemory(Initiator initiator, String extended) {
        asyncDo(() -> {
            this.commandsMemory.removeByExactExtendedAndType(initiator, extended, CALL_BATCH);
        });
    }
    
    private void asyncChangeCommandsMemory(
            Initiator initiator, String batchOldName, String batchNewName) {
        asyncDo(() -> {
            this.commandsMemory.removeByExactExtendedAndType(initiator, batchOldName, CALL_BATCH);
            this.commandsMemory.save(
                    initiator, new CallBatchCommand(batchNewName, batchNewName, NEW, TARGET_FOUND));
        });
    }
    
    private void asyncAddCommand(Initiator initiator, String batchName) {
        asyncDo(() -> {
            this.commandsMemory.save(
                    initiator, new CallBatchCommand(batchName, batchName, NEW, TARGET_FOUND));
        });
    }
    
    private ValueFlow<Batch> findExistingBatchInternally(Initiator initiator, String name) {
        List<String> foundBatchNames;     
        Optional<Batch> foundBatch;
        WeightedVariants weightedBatchNames;
        Answer answer;
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class)
                .withInitialArgument(name)
                .withRule(ENTITY_NAME_RULE)
                .withInputSource(() -> {
                    return this.ioEngine.askInput(initiator, "name", this.enterBatchNameHelp);
                })
                .withOutputDestination((validationFail) -> {
                    this.ioEngine.report(initiator, validationFail);
                });
        
        try {
            batchDiscussing: while ( true ) {                 
                name = dialog
                        .withInitialArgument(name)
                        .validateAndGet();

                foundBatchNames = this.dao.getBatchNamesByNamePattern(initiator, name);
                if ( hasOne(foundBatchNames) ) {
                    foundBatch = this.dao.getBatchByExactName(initiator, getOne(foundBatchNames));
                    if ( foundBatch.isPresent() ) {
                        this.ioEngine.report(
                                initiator, format("'%s' found.", foundBatch.get().name()));
                        return valueFlowCompletedWith(foundBatch);
                    } else {
                        this.ioEngine.report(initiator, format("not found by '%s'", name));
                        name = "";
                        continue batchDiscussing;
                    }
                } else if ( hasMany(foundBatchNames) ) {
                    weightedBatchNames = weightStrings(name, foundBatchNames);
                    if ( weightedBatchNames.isEmpty() ) {
                        this.ioEngine.report(initiator, format("cannot get Batch by '%s'", name));
                        name = "";
                        continue batchDiscussing;
                    }
                    answer = this.ioEngine.chooseInWeightedVariants(
                            initiator, weightedBatchNames, this.chooseBatchNameHelp);
                    if ( answer.isGiven() ) {
                        foundBatch = this.dao.getBatchByExactName(initiator, answer.text());
                        if ( foundBatch.isPresent() ) {
                            return valueFlowCompletedWith(foundBatch);
                        } else {
                            this.ioEngine.report(
                                    initiator, format("cannot get Batch by '%s'", name));
                            name = "";
                            continue batchDiscussing;
                        }
                    } else if ( answer.isRejection() ) {
                        return valueFlowStopped();
                    } else if ( answer.variantsAreNotSatisfactory() ) {
                        name = "";
                        continue batchDiscussing;
                    } else {
                        this.ioEngine.report(initiator, "cannot determine your answer.");
                        return valueFlowStopped();
                    }
                } else {
                    this.ioEngine.report(initiator, format("not found by '%s'", name));
                    name = "";
                    continue batchDiscussing;
                }
            }
        } finally {
            giveBackToPool(dialog);
        }    
    }

    @Override
    public ValueFlow<Batch> findByNamePattern(
            Initiator initiator, String pattern) {
        List<String> foundBatchNames = 
                this.dao.getBatchNamesByNamePattern(initiator, pattern);
        if ( hasOne(foundBatchNames) ) {
            String batchName = getOne(foundBatchNames);
            if ( batchName.equalsIgnoreCase(pattern) || 
                 isNameSatisfiable(pattern, batchName) ) {
                return this.findByExactName(initiator, batchName); 
            } else {
                return valueFlowCompletedEmpty();
            }  
        } else if ( hasMany(foundBatchNames) ) {
            return this.manageWithManyBatchNames(initiator, pattern, foundBatchNames);
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    @Override
    public ValueFlow<Batch> findByExactName(
            Initiator initiator, String exactName) {
        return valueFlowCompletedWith(this.dao.getBatchByExactName(initiator, exactName));
    }

    private ValueFlow<Batch> manageWithManyBatchNames(
            Initiator initiator, String pattern, List<String> foundBatchNames) {
        WeightedVariants variants = weightVariants(pattern, stringsToVariants(foundBatchNames));
        if ( variants.isEmpty() ) {
            return valueFlowCompletedEmpty();
        }
        
        WeightedVariant bestVariant = variants.best();
        if ( bestVariant.text().equalsIgnoreCase(pattern) || 
             bestVariant.hasEqualOrBetterWeightThan(PERFECT) ) {
            return this.findByExactName(initiator, foundBatchNames.get(bestVariant.index()));
        } else {
            boolean hasMatch = this.daoPatternChoices
                    .hasMatchOf(initiator, pattern, bestVariant.text(), variants);
            if ( hasMatch ) {
                return this.findByExactName(initiator, foundBatchNames.get(bestVariant.index()));
            } else {
                return this.askUserForBatchAndSaveChoice(
                        initiator, pattern, variants, foundBatchNames);
            }            
        }
    } 
    
    private ValueFlow<Batch> askUserForBatchAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            WeightedVariants variants, 
            List<String> batchNames) {
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseBatchNameHelp);
        if ( answer.isGiven() ) {
            asyncDo(() -> {
                this.daoPatternChoices.save(
                        initiator, pattern, batchNames.get(answer.index()), variants);
            });
            return this.findByExactName(initiator, batchNames.get(answer.index()));
        } else if ( answer.isRejection() ) {
            return valueFlowStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueFlowCompletedEmpty();
        } else {
            return valueFlowCompletedEmpty();
        }
    }

    @Override
    public ValueFlow<Batch> findBatch(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_BATCH) ) {
            return valueFlowFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }
        
        return this.findExistingBatchInternally(initiator, name);
    }

    @Override
    public List<Batch> getAllBatches(Initiator initiator) {
        return this.dao.getAllBatches(initiator);
    }
    
    private String discussBatchNewName(Initiator initiator, String name) {
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        
        try {
            return dialog
                    .withInitialArgument(name)
                    .withRule(ENTITY_NAME_RULE)
                    .withRule((newName) -> {
                        if ( this.dao.isNameFree(initiator, newName) ) {
                            return validationOk();
                        } else {
                            return validationFailsWith("this name is not free!");
                        }                        
                    })
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(initiator, "name", this.enterBatchNameHelp);
                    })
                    .withOutputDestination((validationFail) -> {
                        this.ioEngine.report(initiator, validationFail);
                    })
                    .validateAndGet();
        } finally {
            giveBackToPool(dialog);
        }
    }

    @Override
    public VoidFlow createBatch(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_BATCH) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }  
        
        synchronized ( this.allBatchesConsistencyLock ) {
            name = this.discussBatchNewName(initiator, name);
            if ( name.isEmpty() ) {
                return voidFlowStopped();
            }

            List<ExecutorCommand> batchCommands = this.inputNewCommands(initiator);        
            if ( batchCommands.isEmpty() ) {
                return voidFlowStopped();
            }

            Batch newBatch = new Batch(name, batchCommands);
            if ( this.dao.saveBatch(initiator, newBatch) ) {
                this.asyncAddCommand(initiator, newBatch.name());
                return voidFlowCompleted();
            } else {
                return voidFlowFail("DAO failed to save new batch.");
            }
        }    
    }

    private List<ExecutorCommand> inputNewCommands(Initiator initiator) {
        List<ExecutorCommand> batchCommands = new ArrayList<>();
        String input;
        Command interpretedCommand;
        Optional<ExecutorCommand> possibleCommand;
        boolean work = true;
        while ( work ) {
            input = this.ioEngine.askInput(
                    initiator, 
                    format("command %d", batchCommands.size() + 1),
                    this.enterNewBatchCommandHelp);
            if ( input.isEmpty() ) {
                work = false;
                continue;
            }
            interpretedCommand = this.interpreter.interprete(input);
            possibleCommand = this.checkInterpreted(initiator, interpretedCommand);
            if ( possibleCommand.isPresent() ) {
                batchCommands.add(possibleCommand.get());
            } 
        }
        return batchCommands;
    }
    
    private Optional<ExecutorCommand> checkInterpreted(
            Initiator initiator, Command batchCommand) {        
        switch ( batchCommand.type() ) {
            case BATCH_PAUSE : 
            case BROWSE_WEBPAGE : 
            case OPEN_LOCATION_TARGET : 
            case OPEN_LOCATION :
            case RUN_PROGRAM : {
                ExecutorCommand command = (ExecutorCommand) batchCommand;
                this.ioEngine.report(initiator, "...accepted.");
                return Optional.of(command);
            }
            case CALL_BATCH : {
                this.ioEngine.report(initiator, "call batch inside batch inside batch? No way.");
                return Optional.empty();
            }
            default : {
                this.ioEngine.report(initiator, "not allowed as batch command.");
                return Optional.empty();
            }
        }
    }

    @Override
    public VoidFlow editBatch(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_BATCH) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String name;
        EntityProperty property;
        if ( command.hasArguments()) {
            PropertyAndText propAndText = this.propertyAndTextParser.parse(command.arguments());
            name = propAndText.text();
            property = propAndText.property();
        } else {
            name = "";
            property = UNDEFINED_PROPERTY;
        }
        
        synchronized ( this.allBatchesConsistencyLock ) {
            Batch editedBatch;
            ValueFlow<Batch> batchFlow = this.findExistingBatchInternally(initiator, name);
            switch ( batchFlow.result() ) {
                case COMPLETE : {
                    if ( batchFlow.asComplete().hasValue() ) {
                        editedBatch = batchFlow.asComplete().orThrow();
                    } else {                    
                        return voidFlowFail("no such batch.");
                    }
                    break; 
                }
                case FAIL : {
                    return voidFlowFail(batchFlow.asFail().reason());
                }
                case STOP : {
                    return voidFlowStopped();
                }
                default : {
                    return voidFlowFail("unknown ValueFlow result.");
                }
            }

            property = this.helper.validatePropertyInteractively(
                    initiator, property, NAME, COMMANDS);
            if ( property.isUndefined() ) {
                return voidFlowStopped();
            }

            switch ( property ) {
                case NAME : {
                    return this.editBatchName(initiator, editedBatch);
                } 
                case COMMANDS : {
                    return this.editBatchCommands(initiator, editedBatch);
                } 
                default : {
                    return voidFlowFail("unexpected property.");
                }
            }
        }    
    }
    
    private VoidFlow editBatchName(Initiator initiator, Batch batch) {
        String newName = this.discussBatchNewName(initiator, "");
        if ( newName.isEmpty() ) {
            return voidFlowStopped();
        }        
        
        if ( this.dao.editBatchName(initiator, batch.name(), newName) ) {
            this.asyncChangeCommandsMemory(initiator, batch.name(), newName);
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to rename batch.");
        }
    }
    
    private VoidFlow editBatchCommands(Initiator initiator, Batch batch) {
        VariantsQuestion question = question("edit all commands or just one?")
                .withAnswerString("one")
                .withAnswerString("all");
        Answer answer = this.ioEngine.ask(initiator, question, this.editCommandModeHelp);
        if ( answer.isGiven() ) {
            if ( answer.is("one") ) {
                return this.editBatchOneCommand(initiator, batch);
            } else {
                return this.editBatchAllCommands(initiator, batch);
            }
        } else {
            return voidFlowStopped();
        }
    }
    
    private VoidFlow editBatchOneCommand(Initiator initiator, Batch batch) {
        VariantsQuestion question = 
                question("choose command").withAnswerEntities(batch.batchedCommands());
        Answer answer = this.ioEngine.ask(initiator, question, this.chooseOneCommandToEditHelp);
        if ( answer.isGiven() ) {
            Optional<ExecutorCommand> newCommand = Optional.empty();
            Command interpertedCommand;
            String newCommandString;
            boolean newCommandIsNotValid = true;
            while ( newCommandIsNotValid ) {
                newCommandString = this.ioEngine.askInput(
                        initiator, "new command", this.editOneCommandHelp);
                if ( newCommandString.isEmpty() ) {
                    return voidFlowStopped();
                }
                interpertedCommand = this.interpreter.interprete(newCommandString);
                newCommand = this.checkInterpreted(initiator, interpertedCommand);
                if ( newCommand.isPresent() ) {
                    newCommandIsNotValid = false;
                }
            }
            if ( ! newCommand.isPresent() ) {
                return voidFlowStopped();
            }
            if ( this.dao.editBatchOneCommand(
                    initiator, batch.name(), answer.index(), newCommand.get()) ) {
                return voidFlowCompleted();
            } else {
                return voidFlowFail("DAO failed to change one command.");
            }
        } else {
            return voidFlowStopped();
        }
    }
    
    private VoidFlow editBatchAllCommands(Initiator initiator, Batch batch) {
        List<ExecutorCommand> newCommands = this.inputNewCommands(initiator);
        if ( newCommands.isEmpty() ) {
            return voidFlowStopped();
        }
        
        if ( this.dao.editBatchCommands(initiator, batch.name(), newCommands) ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to change all commands.");
        }
    }

    @Override
    public VoidFlow removeBatch(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_BATCH) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }
        
        Batch removedBatch;
        ValueFlow<Batch> batchFlow = this.findExistingBatchInternally(initiator, name);
        switch ( batchFlow.result() ) {
            case COMPLETE : {
                if ( batchFlow.asComplete().hasValue() ) {
                    removedBatch = batchFlow.asComplete().orThrow();
                } else {                    
                    return voidFlowFail("no such batch.");
                }
                break; 
            }
            case FAIL : {
                return voidFlowFail(batchFlow.asFail().reason());
            }
            case STOP : {
                return voidFlowStopped();
            }
            default : {
                return voidFlowFail("unknown ValueFlow result.");
            }
        }
        
        if ( this.dao.removeBatch(initiator, removedBatch.name()) ) {
            this.asyncCleanCommandsMemory(initiator, removedBatch.name());
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to remove batch");
        }      
    }

    @Override
    public ValueFlow<Message> findAll(Initiator initiator) {
        return valueFlowCompletedWith(entitiesToOptionalMessageWithHeader(
                    "all Batches:", this.dao.getAllBatches(initiator)));       
    }
}
