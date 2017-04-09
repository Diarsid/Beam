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
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;
import diarsid.beam.core.base.control.io.commands.EntityInvocationCommand;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.modules.data.DaoBatches;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Operations.valueFound;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_BATCH;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.COMMANDS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;


class BatchesKeeperWorker 
        implements 
                BatchesKeeper, 
                NamedEntitiesKeeper {
    
    private final DaoBatches dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final Interpreter interpreter;
    private final PropertyAndTextParser propertyAndTextParser;
    private final Set<CommandType> subjectedCommandTypes;
    
    BatchesKeeperWorker(
            DaoBatches daoBatches, 
            InnerIoEngine ioEngine,
            KeeperDialogHelper helper,
            Interpreter interpreter,
            PropertyAndTextParser propertyAndTextParser) {
        this.dao = daoBatches;
        this.ioEngine = ioEngine;
        this.helper = helper;
        this.interpreter = interpreter;
        this.propertyAndTextParser = propertyAndTextParser;
        this.subjectedCommandTypes = toSet(CALL_BATCH);
    }

    @Override
    public boolean isSubjectedTo(EntityInvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }

    @Override
    public Optional<Batch> findByNamePattern(
            Initiator initiator, String batchNamePattern) {
        List<String> foundBatchNames = this.getMatchingBatches(initiator, batchNamePattern);
        if ( hasOne(foundBatchNames) ) {
            return this.dao.getBatchByExactName(initiator, getOne(foundBatchNames));        
        } else if ( hasMany(foundBatchNames) ) {
            return this.manageWithManyBatchNames(initiator, foundBatchNames);
        } else {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<Batch> findByExactName(
            Initiator initiator, String exactName) {
        return this.dao.getBatchByExactName(initiator, exactName);
    }

    private Optional<Batch> manageWithManyBatchNames(
            Initiator initiator, List<String> foundBatchNames) {
        Answer answer = this.ioEngine.ask(
                initiator, question("choose batch").withAnswerStrings(foundBatchNames));
        if ( answer.isGiven() ) {
            return this.dao.getBatchByExactName(initiator, answer.text());
        } else {
            return Optional.empty();
        }
    }
    
    private List<String> getMatchingBatches(
            Initiator initiator, String batchNamePattern) {
        if ( hasWildcard(batchNamePattern) ) {
            return this.dao.getBatchNamesByNamePatternParts(
                    initiator, splitByWildcard(batchNamePattern));
        } else {
            return this.dao.getBatchNamesByNamePattern(
                    initiator, batchNamePattern);
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
            name = this.helper.validateEntityNameInteractively(initiator, name);
        }
        
        if ( name.isEmpty() ) {
            return valueOperationStopped();
        }
        
        return valueFound(this.findByNamePattern(initiator, name));
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
        
        List<ExtendableCommand> batchCommands = this.inputNewCommands(initiator);        
        if ( batchCommands.isEmpty() ) {
            return voidOperationStopped();
        }
        
        Batch newBatch = new Batch(name, batchCommands);
        if ( this.dao.saveBatch(initiator, newBatch) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to save new batch.");
        }
    }

    private List<ExtendableCommand> inputNewCommands(Initiator initiator) {
        List<ExtendableCommand> batchCommands = new ArrayList<>();
        String input;
        Command interpretedCommand;
        Optional<ExtendableCommand> possibleCommand;
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
    
    private Optional<ExtendableCommand> checkInterpreted(
            Initiator initiator, Command batchCommand) {        
        switch ( batchCommand.type() ) {
            case BATCH_PAUSE : 
            case SEE_WEBPAGE : 
            case OPEN_LOCATION_TARGET : 
            case OPEN_LOCATION :
            case RUN_PROGRAM : {
                ExtendableCommand argumented = (ExtendableCommand) batchCommand;
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
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        } 
        
        Optional<Batch> editedBatch = this.findByNamePattern(initiator, name);
        if ( editedBatch.isPresent() ) {
            this.ioEngine.report(initiator, format("'%s' found.", editedBatch.get().name()));
        } else {
            return voidOperationFail("no such batch.");
        }
        
        property = this.helper.validatePropertyInteractively(
                initiator, property, NAME, COMMANDS);
        if ( property.isUndefined() ) {
            return voidOperationStopped();
        }
        
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
            return voidCompleted();
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
        Question question = question("choose command").withAnswerEntities(batch.batchedCommands());
        Answer answer = this.ioEngine.ask(initiator, question);
        if ( answer.isGiven() ) {
            Optional<ExtendableCommand> newCommand = Optional.empty();
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
        List<ExtendableCommand> newCommands = this.inputNewCommands(initiator);
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
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        List<String> batchNames = this.getMatchingBatches(initiator, name);
        if ( hasOne(batchNames) ) {
            this.ioEngine.report(initiator, format("'%s' found.", getOne(batchNames)));
            if ( this.dao.removeBatch(initiator, getOne(batchNames)) ) {
                return voidCompleted();
            } else {
                return voidOperationFail("DAO failed to remove batch");
            }
        } else if ( hasMany(batchNames) ) {
            Question question = question("choose batch").withAnswerStrings(batchNames);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                if ( this.dao.removeBatch(initiator, batchNames.get(answer.index())) ) {
                    return voidCompleted();
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
