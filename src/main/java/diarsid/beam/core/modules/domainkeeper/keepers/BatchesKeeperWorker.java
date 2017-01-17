/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper.keepers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Answer;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.base.Question;
import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.commands.CreateEntityCommand;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.interpreter.Interpreter;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.domainkeeper.BatchesKeeper;
import diarsid.beam.core.modules.domainkeeper.KeeperDialogHelper;

import static java.lang.String.format;

import static diarsid.beam.core.control.io.base.Question.question;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.FIND_BATCH;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_COMMANDS;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_NAME;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.util.StringUtils.splitByWildcard;


public class BatchesKeeperWorker implements BatchesKeeper {
    
    private final DaoBatches dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final Interpreter interpreter;
    
    public BatchesKeeperWorker(
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
    public Optional<Batch> findBatch(Initiator initiator, FindEntityCommand command) {
        if ( this.helper.checkFinding(initiator, command, FIND_BATCH) ) {
            return this.getBatchByNamePattern(initiator, command.getArg());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Batch> getAllBatches(Initiator initiator) {
        return this.dao.getAllBatches(initiator);
    }

    @Override
    public boolean createBatch(Initiator initiator, CreateEntityCommand command) {
        if ( command.type().isNot(CREATE_BATCH) ) {
            return false;
        }
        
        String newBatchName = "";
        boolean nameNotValidOrNotFree = true;
        while ( nameNotValidOrNotFree ) {
            newBatchName = this.ioEngine.askInput(initiator, "new batch name");
            newBatchName = this.helper.validateEntityNameInteractively(initiator, newBatchName);
            if ( newBatchName.isEmpty() ) {
                return false;
            }
            if ( this.dao.isNameFree(initiator, newBatchName) ) {
                nameNotValidOrNotFree = false;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
            }
        }
        
        if ( newBatchName.isEmpty() ) {
            return false;
        }
        
        List<ArgumentedCommand> batchCommands = this.inputNewCommands(initiator);        
        if ( batchCommands.isEmpty() ) {
            return false;
        }
        
        Batch newBatch = new Batch(newBatchName, batchCommands);
        return this.dao.saveBatch(initiator, newBatch);
    }

    private List<ArgumentedCommand> inputNewCommands(Initiator initiator) {
        List<ArgumentedCommand> batchCommands = new ArrayList<>();
        String input = "";
        Optional<ArgumentedCommand> possibleCommand;
        boolean work = true;
        while ( work ) {
            input = this.ioEngine.askInput(
                    initiator, format("command %d", batchCommands.size() + 1));
            if ( input.isEmpty() ) {
                work = false;
                continue;
            }
            possibleCommand = this.interpreteAndCheck(initiator, input);
            if ( possibleCommand.isPresent() ) {
                batchCommands.add(possibleCommand.get());
            } 
        }
        return batchCommands;
    }
    
    private Optional<ArgumentedCommand> interpreteAndCheck(
            Initiator initiator, String possibleArgumentedCommand) {
        Command batchCommand = this.interpreter.interprete(possibleArgumentedCommand);
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
    public boolean editBatch(Initiator initiator, EditEntityCommand command) {
        if ( this.helper.checkEdition(
                initiator, command, EDIT_BATCH, TARGET_NAME, TARGET_COMMANDS) ) {
            Optional<Batch> editedBatch = this.getBatchByNamePattern(initiator, command.getName());
            if ( editedBatch.isPresent() ) {
                switch ( command.getTarget() ) {
                    case TARGET_NAME : {
                        return this.editBatchName(initiator, editedBatch.get());
                    } 
                    case TARGET_COMMANDS : {
                        return this.editBatchCommands(initiator, editedBatch.get());
                    } 
                    default : {
                        return false;
                    }
                }
            } else {
                this.ioEngine.report(initiator, "there is no such batch.");
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean editBatchName(Initiator initiator, Batch batch) {
        boolean nameIsNotFreeOrValid = true;
        String newName = "";
        while ( nameIsNotFreeOrValid ) {
            newName = this.ioEngine.askInput(initiator, "new name");
            if ( newName.isEmpty() ) {
                return false;
            }
            newName = this.helper.validateEntityNameInteractively(initiator, newName);
            if ( newName.isEmpty() ) {
                return false;
            }
            if ( this.dao.isNameFree(initiator, newName) ) {
                nameIsNotFreeOrValid = false;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
            }
        }
        return this.dao.editBatchName(initiator, batch.getName(), newName);
    }
    
    private boolean editBatchCommands(Initiator initiator, Batch batch) {
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
            return false;
        }
    }
    
    private boolean editBatchOneCommand(Initiator initiator, Batch batch) {
        Question question = question("choose command")
                .withAnswerStrings(batch.stringifyCommands());
        Answer answer = this.ioEngine.ask(initiator, question);
        if ( answer.isGiven() ) {
            Optional<ArgumentedCommand> newCommand = Optional.empty();
            String newCommandString;
            boolean newCommandIsNotValid = true;
            while ( newCommandIsNotValid ) {
                newCommandString = this.ioEngine.askInput(initiator, "new command");
                if ( newCommandString.isEmpty() ) {
                    return false;
                }
                newCommand = this.interpreteAndCheck(initiator, newCommandString);
                if ( newCommand.isPresent() ) {
                    newCommandIsNotValid = false;
                }
            }
            return this.dao.editBatchOneCommand(
                    initiator, batch.getName(), answer.getIndex(), newCommand.get());
        } else {
            return false;
        }
    }
    
    private boolean editBatchAllCommands(Initiator initiator, Batch batch) {
        List<ArgumentedCommand> newCommands = this.inputNewCommands(initiator);
        if ( newCommands.isEmpty() ) {
            return false;
        }
        
        return this.dao.editBatchCommands(initiator, batch.getName(), newCommands);
    }

    @Override
    public boolean removeBatch(Initiator initiator, RemoveEntityCommand command) {
        if ( this.helper.checkDeletion(initiator, command, DELETE_BATCH) ) {
            List<String> batchNames = this.getMatchingBatches(initiator, command.getArg());
            if ( hasOne(batchNames) ) {
                return this.dao.removeBatch(initiator, getOne(batchNames));
            } else if ( hasMany(batchNames) ) {
                Answer answer = this.ioEngine.ask(
                        initiator, question("choose batch").withAnswerStrings(batchNames));
                if ( answer.isGiven() ) {
                    return this.dao.removeBatch(initiator, batchNames.get(answer.getIndex()));
                } else {
                    return false;
                }
            } else {
                this.ioEngine.report(initiator, "this is not the batch you are looking for.");
                return false;
            }
        } else {
            return false;
        }        
    }
}
