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

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
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
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;
import diarsid.beam.core.modules.data.DaoBatches;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_BATCH;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.COMMANDS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.patternsanalyze.Analyze.weightStrings;

class BatchesKeeperWorker 
        implements 
                BatchesKeeper, 
                NamedEntitiesKeeper {
    
    private final DaoBatches dao;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final Interpreter interpreter;
    private final PropertyAndTextParser propertyAndTextParser;
    private final Set<CommandType> subjectedCommandTypes;
    
    BatchesKeeperWorker(
            DaoBatches daoBatches, 
            CommandsMemoryKeeper commandsMemoryKeeper,
            InnerIoEngine ioEngine,
            KeeperDialogHelper helper,
            Interpreter interpreter,
            PropertyAndTextParser propertyAndTextParser) {
        this.dao = daoBatches;
        this.commandsMemory = commandsMemoryKeeper;
        this.ioEngine = ioEngine;
        this.helper = helper;
        this.interpreter = interpreter;
        this.propertyAndTextParser = propertyAndTextParser;
        this.subjectedCommandTypes = toSet(CALL_BATCH);
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
    
    private ValueOperation<Batch> discussExistingBatch(Initiator initiator, String name) {
        List<String> foundBatchNames;     
        Optional<Batch> foundBatch;
        WeightedVariants weightedBatchNames;
        Answer answer;
        batchDiscussing: while ( true ) {            
            name = this.helper.validateEntityNameInteractively(initiator, name);
            if (name.isEmpty()) {
                return valueOperationStopped();
            }

            foundBatchNames = this.dao.getBatchNamesByNamePattern(initiator, name);
            if ( hasOne(foundBatchNames) ) {
                foundBatch = this.dao.getBatchByExactName(initiator, getOne(foundBatchNames));
                if ( foundBatch.isPresent() ) {
                    this.ioEngine.report(initiator, format("'%s' found.", foundBatch.get().name()));
                    return valueCompletedWith(foundBatch);
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
                answer = this.ioEngine.chooseInWeightedVariants(initiator, weightedBatchNames);
                if ( answer.isGiven() ) {
                    foundBatch = this.dao.getBatchByExactName(initiator, answer.text());
                    if ( foundBatch.isPresent() ) {
                        return valueCompletedWith(foundBatch);
                    } else {
                        this.ioEngine.report(initiator, format("cannot get Batch by '%s'", name));
                        name = "";
                        continue batchDiscussing;
                    }
                } else if ( answer.isRejection() ) {
                    return valueOperationStopped();
                } else if ( answer.variantsAreNotSatisfactory() ) {
                    name = "";
                    continue batchDiscussing;
                } else {
                    this.ioEngine.report(initiator, "cannot determine your answer.");
                    return valueOperationStopped();
                }
            } else {
                this.ioEngine.report(initiator, format("not found by '%s'", name));
                name = "";
                continue batchDiscussing;
            }
        }
    }

    @Override
    public ValueOperation<Batch> findByNamePattern(
            Initiator initiator, String batchNamePattern) {
        List<String> foundBatchNames = 
                this.dao.getBatchNamesByNamePattern(initiator, batchNamePattern);
        if ( hasOne(foundBatchNames) ) {
            return this.findByExactName(initiator, getOne(foundBatchNames));        
        } else if ( hasMany(foundBatchNames) ) {
            return this.manageWithManyBatchNames(initiator, foundBatchNames);
        } else {
            return valueCompletedEmpty();
        }
    }
    
    @Override
    public ValueOperation<Batch> findByExactName(
            Initiator initiator, String exactName) {
        return valueCompletedWith(this.dao.getBatchByExactName(initiator, exactName));
    }

    private ValueOperation<Batch> manageWithManyBatchNames(
            Initiator initiator, List<String> foundBatchNames) {
        Answer answer = this.ioEngine.ask(
                initiator, question("choose batch").withAnswerStrings(foundBatchNames));
        if ( answer.isGiven() ) {
            return this.findByExactName(initiator, answer.text());
        } else {
            if ( answer.isRejection() ) {
                return valueOperationStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueCompletedEmpty();
            } else {
                return valueCompletedEmpty();
            }
        }
    }

    @Override
    public ValueOperation<Batch> findBatch(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_BATCH) ) {
            return valueOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }
        
        return this.discussExistingBatch(initiator, name);
    }

    @Override
    public List<Batch> getAllBatches(Initiator initiator) {
        return this.dao.getAllBatches(initiator);
    }

    @Override
    public VoidOperation createBatch(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_BATCH) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }  
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }      
        
        boolean nameNotValidOrNotFree = true;
        while ( nameNotValidOrNotFree ) {
            if ( this.dao.isNameFree(initiator, name) ) {
                nameNotValidOrNotFree = false;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
                name = this.ioEngine.askInput(initiator, "name");
                if ( name.isEmpty() ) {
                    return voidOperationStopped();
                }
                name = this.helper.validateEntityNameInteractively(initiator, name);
                if ( name.isEmpty() ) {
                    return voidOperationStopped();
                }
            }
        }
        
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        List<ExecutorCommand> batchCommands = this.inputNewCommands(initiator);        
        if ( batchCommands.isEmpty() ) {
            return voidOperationStopped();
        }
        
        Batch newBatch = new Batch(name, batchCommands);
        if ( this.dao.saveBatch(initiator, newBatch) ) {
            this.asyncAddCommand(initiator, newBatch.name());
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to save new batch.");
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
                    initiator, format("command %d", batchCommands.size() + 1));
            interpretedCommand = this.interpreter.interprete(input);
            if ( input.isEmpty() ) {
                work = false;
                continue;
            }
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
    public VoidOperation editBatch(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_BATCH) ) {
            return voidOperationFail("wrong command type!");
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
        
        Batch editedBatch;
        ValueOperation<Batch> batchFlow = this.discussExistingBatch(initiator, name);
        switch ( batchFlow.result() ) {
            case COMPLETE : {
                if ( batchFlow.asComplete().hasValue() ) {
                    editedBatch = batchFlow.asComplete().getOrThrow();
                } else {                    
                    return voidOperationFail("no such batch.");
                }
                break; 
            }
            case FAIL : {
                return voidOperationFail(batchFlow.asFail().reason());
            }
            case STOP : {
                return voidOperationStopped();
            }
            default : {
                return voidOperationFail("unknown ValueOperation result.");
            }
        }
        
        property = this.helper.validatePropertyInteractively(
                initiator, property, NAME, COMMANDS);
        if ( property.isUndefined() ) {
            return voidOperationStopped();
        }
        
        switch ( property ) {
            case NAME : {
                return this.editBatchName(initiator, editedBatch);
            } 
            case COMMANDS : {
                return this.editBatchCommands(initiator, editedBatch);
            } 
            default : {
                return voidOperationFail("unexpected property.");
            }
        }
    }
    
    private VoidOperation editBatchName(Initiator initiator, Batch batch) {
        boolean nameIsNotFreeOrValid = true;
        String newName = "";
        while ( nameIsNotFreeOrValid ) {
            newName = this.ioEngine.askInput(initiator, "new name");
            if ( newName.isEmpty() ) {
                return voidOperationStopped();
            }
            newName = this.helper.validateEntityNameInteractively(initiator, newName);
            if ( newName.isEmpty() ) {
                return voidOperationStopped();
            }
            if ( this.dao.isNameFree(initiator, newName) ) {
                nameIsNotFreeOrValid = false;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
            }
        }
        if ( this.dao.editBatchName(initiator, batch.name(), newName) ) {
            this.asyncChangeCommandsMemory(initiator, batch.name(), newName);
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to rename batch.");
        }
    }
    
    private VoidOperation editBatchCommands(Initiator initiator, Batch batch) {
        VariantsQuestion question = question("edit all commands or just one?")
                .withAnswerString("one")
                .withAnswerString("all");
        Answer answer = this.ioEngine.ask(initiator, question);
        if ( answer.isGiven() ) {
            if ( answer.is("one") ) {
                return this.editBatchOneCommand(initiator, batch);
            } else {
                return this.editBatchAllCommands(initiator, batch);
            }
        } else {
            return voidOperationStopped();
        }
    }
    
    private VoidOperation editBatchOneCommand(Initiator initiator, Batch batch) {
        VariantsQuestion question = question("choose command").withAnswerEntities(batch.batchedCommands());
        Answer answer = this.ioEngine.ask(initiator, question);
        if ( answer.isGiven() ) {
            Optional<ExecutorCommand> newCommand = Optional.empty();
            Command interpertedCommand;
            String newCommandString;
            boolean newCommandIsNotValid = true;
            while ( newCommandIsNotValid ) {
                newCommandString = this.ioEngine.askInput(initiator, "new command");
                if ( newCommandString.isEmpty() ) {
                    return voidOperationStopped();
                }
                interpertedCommand = this.interpreter.interprete(newCommandString);
                newCommand = this.checkInterpreted(initiator, interpertedCommand);
                if ( newCommand.isPresent() ) {
                    newCommandIsNotValid = false;
                }
            }
            if ( ! newCommand.isPresent() ) {
                return voidOperationStopped();
            }
            if ( this.dao.editBatchOneCommand(
                    initiator, batch.name(), answer.index(), newCommand.get()) ) {
                return voidCompleted();
            } else {
                return voidOperationFail("DAO failed to change one command.");
            }
        } else {
            return voidOperationStopped();
        }
    }
    
    private VoidOperation editBatchAllCommands(Initiator initiator, Batch batch) {
        List<ExecutorCommand> newCommands = this.inputNewCommands(initiator);
        if ( newCommands.isEmpty() ) {
            return voidOperationStopped();
        }
        
        if ( this.dao.editBatchCommands(initiator, batch.name(), newCommands) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to change all commands.");
        }
    }

    @Override
    public VoidOperation removeBatch(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_BATCH) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }
        
        Batch removedBatch;
        ValueOperation<Batch> batchFlow = this.discussExistingBatch(initiator, name);
        switch ( batchFlow.result() ) {
            case COMPLETE : {
                if ( batchFlow.asComplete().hasValue() ) {
                    removedBatch = batchFlow.asComplete().getOrThrow();
                } else {                    
                    return voidOperationFail("no such batch.");
                }
                break; 
            }
            case FAIL : {
                return voidOperationFail(batchFlow.asFail().reason());
            }
            case STOP : {
                return voidOperationStopped();
            }
            default : {
                return voidOperationFail("unknown ValueOperation result.");
            }
        }
        
        if ( this.dao.removeBatch(initiator, removedBatch.name()) ) {
            this.asyncCleanCommandsMemory(initiator, removedBatch.name());
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to remove batch");
        }      
    }
}
