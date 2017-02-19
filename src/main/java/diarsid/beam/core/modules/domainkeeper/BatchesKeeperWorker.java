/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.modules.data.DaoBatches;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Operations.returnOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.returnOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_BATCH;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.COMMANDS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.PROPERTY_UNDEFINED;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;

import diarsid.beam.core.base.control.flow.ReturnOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;

import static diarsid.beam.core.base.control.flow.Operations.ok;
import static diarsid.beam.core.base.control.flow.Operations.okWith;
import static diarsid.beam.core.base.control.flow.Operations.okWith;


class BatchesKeeperWorker implements BatchesKeeper {
    
    private final DaoBatches dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final Interpreter interpreter;
    
    BatchesKeeperWorker(
            DaoBatches daoBatches, 
            InnerIoEngine ioEngine,
            KeeperDialogHelper helper,
            Interpreter interpreter) {
        this.dao = daoBatches;
        this.ioEngine = ioEngine;
        this.helper = helper;
        this.interpreter = interpreter;
    }

    @Override
    public Optional<Batch> getBatchByNamePattern(Initiator initiator, String batchNamePattern) {
        List<String> foundBatchNames = this.getMatchingBatches(initiator, batchNamePattern);
        if ( hasOne(foundBatchNames) ) {
            return this.dao.getBatchByName(initiator, getOne(foundBatchNames));        
        } else if ( hasMany(foundBatchNames) ) {
            return this.manageWithManyBatchNames(initiator, foundBatchNames);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Batch> manageWithManyBatchNames(Initiator initiator, List<String> foundBatchNames) {
        Answer answer = this.ioEngine.ask(
                initiator, question("choose batch").withAnswerStrings(foundBatchNames));
        if ( answer.isGiven() ) {
            return this.dao.getBatchByName(initiator, answer.getText());
        } else {
            return Optional.empty();
        }
    }
    
    private List<String> getMatchingBatches(Initiator initiator, String batchNamePattern) {
        if ( hasWildcard(batchNamePattern) ) {
            return this.dao.getBatchNamesByNamePatternParts(
                    initiator, splitByWildcard(batchNamePattern));
        } else {
            return this.dao.getBatchNamesByNamePattern(
                    initiator, batchNamePattern);
        }
    }

    @Override
    public ReturnOperation<Batch> findBatch(Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(FIND_BATCH) ) {
            return returnOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArg() ) {
            name = command.getArg();
        } else {
            name = "";
            name = this.helper.validateEntityNameInteractively(initiator, name);
        }
        
        if ( name.isEmpty() ) {
            return returnOperationStopped();
        }
        
        return okWith(this.getBatchByNamePattern(initiator, command.getArg()));
    }

    @Override
    public List<Batch> getAllBatches(Initiator initiator) {
        return this.dao.getAllBatches(initiator);
    }

    @Override
    public VoidOperation createBatch(Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(CREATE_BATCH) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArg() ) {
            name = command.getArg();
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
        
        List<ArgumentedCommand> batchCommands = this.inputNewCommands(initiator);        
        if ( batchCommands.isEmpty() ) {
            return voidOperationStopped();
        }
        
        Batch newBatch = new Batch(name, batchCommands);
        if ( this.dao.saveBatch(initiator, newBatch) ) {
            return ok();
        } else {
            return voidOperationFail("DAO failed to save new batch.");
        }
    }

    private List<ArgumentedCommand> inputNewCommands(Initiator initiator) {
        List<ArgumentedCommand> batchCommands = new ArrayList<>();
        String input;
        Command interpretedCommand;
        Optional<ArgumentedCommand> possibleCommand;
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
    
    private Optional<ArgumentedCommand> checkInterpreted(
            Initiator initiator, Command batchCommand) {        
        switch ( batchCommand.type() ) {
            case BATCH_PAUSE : 
            case SEE_WEBPAGE : 
            case OPEN_PATH : 
            case OPEN_LOCATION :
            case RUN_PROGRAM : {
                ArgumentedCommand argumented = (ArgumentedCommand) batchCommand;
                this.ioEngine.report(initiator, "...accepted.");
                return Optional.of(argumented);
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
    public VoidOperation editBatch(Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(EDIT_BATCH) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        EntityProperty property;
        if ( command.hasArg() ) {
            property = argToProperty(command.getArg());
            if ( property.isDefined() ) {
                name = "";
            } else {
                name = command.getArg();
            }
        } else {
            name = "";
            property = PROPERTY_UNDEFINED;
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        property = this.helper.validatePropertyInteractively(
                initiator, property, NAME, COMMANDS);
        if ( property.isNotDefined() ) {
            return voidOperationStopped();
        } 
        
        Optional<Batch> editedBatch = this.getBatchByNamePattern(initiator, name);
        if ( editedBatch.isPresent() ) {
            switch ( property ) {
                case NAME : {
                    return this.editBatchName(initiator, editedBatch.get());
                } 
                case COMMANDS : {
                    return this.editBatchCommands(initiator, editedBatch.get());
                } 
                default : {
                    return voidOperationFail("unexpected property.");
                }
            }
        } else {
            return voidOperationFail("there is no such batch.");
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
        if ( this.dao.editBatchName(initiator, batch.getName(), newName) ) {
            return ok();
        } else {
            return voidOperationFail("DAO failed to rename batch.");
        }
    }
    
    private VoidOperation editBatchCommands(Initiator initiator, Batch batch) {
        Question question = question("edit all commands or just one?")
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
        Question question = question("choose command")
                .withAnswerStrings(batch.stringifyCommands());
        Answer answer = this.ioEngine.ask(initiator, question);
        if ( answer.isGiven() ) {
            Optional<ArgumentedCommand> newCommand = Optional.empty();
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
                    initiator, batch.getName(), answer.index(), newCommand.get()) ) {
                return ok();
            } else {
                return voidOperationFail("DAO failed to change one command.");
            }
        } else {
            return voidOperationStopped();
        }
    }
    
    private VoidOperation editBatchAllCommands(Initiator initiator, Batch batch) {
        List<ArgumentedCommand> newCommands = this.inputNewCommands(initiator);
        if ( newCommands.isEmpty() ) {
            return voidOperationStopped();
        }
        
        if ( this.dao.editBatchCommands(initiator, batch.getName(), newCommands) ) {
            return ok();
        } else {
            return voidOperationFail("DAO failed to change all commands.");
        }
    }

    @Override
    public VoidOperation removeBatch(Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(DELETE_BATCH) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArg() ) {
            name = command.getArg();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        List<String> batchNames = this.getMatchingBatches(initiator, name);
        if ( hasOne(batchNames) ) {
            if ( this.dao.removeBatch(initiator, getOne(batchNames)) ) {
                return ok();
            } else {
                return voidOperationFail("DAO failed to remove batch");
            }
        } else if ( hasMany(batchNames) ) {
            Question question = question("choose batch").withAnswerStrings(batchNames);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                if ( this.dao.removeBatch(initiator, batchNames.get(answer.index())) ) {
                    return ok();
                } else {
                    return voidOperationFail("DAO failed to remove batch.");
                }
            } else {
                return voidOperationStopped();
            }
        } else {
            return voidOperationFail("this is not the batch you are looking for.");
        }        
    }
}
