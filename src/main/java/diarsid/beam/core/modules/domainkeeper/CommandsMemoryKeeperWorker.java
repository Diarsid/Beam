/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.patternsanalyze.WeightedVariants;
import diarsid.beam.core.base.util.StringHolder;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoCommandsChoices;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.commandsToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_MEM;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_MEM;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.Commands.createInvocationCommandFrom;
import static diarsid.beam.core.base.patternsanalyze.Analyze.weightVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringHolder.empty;
import static diarsid.beam.core.base.util.StringHolder.hold;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.TEXT_RULE;

/**
 *
 * @author Diarsid
 */
class CommandsMemoryKeeperWorker implements CommandsMemoryKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoCommands daoCommands;
    private final DaoCommandsChoices daoCommandsChoices;
    private final KeeperDialogHelper helper;

    CommandsMemoryKeeperWorker(
            DaoCommands daoCommands, 
            DaoCommandsChoices daoChoices, 
            InnerIoEngine ioEngine, 
            KeeperDialogHelper helper) {
        this.daoCommands = daoCommands;
        this.daoCommandsChoices = daoChoices;
        this.ioEngine = ioEngine;
        this.helper = helper;
    }

    @Override
    public ValueOperation<List<InvocationCommand>> findMems(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_MEM) ) {
            return valueOperationFail("wrong command type!");
        }
        
        StringHolder memPattern;
        if ( command.hasArguments() ) {
            memPattern = hold(command.joinedArguments());
        } else {
            memPattern = empty();
        }
        
        return this.findCommands(initiator, memPattern);
    }

    private ValueOperation<List<InvocationCommand>> findCommands(
            Initiator initiator, StringHolder memPattern) {       
        
        memPattern.set(helper.validateInteractively(initiator, memPattern.get(), "mem", TEXT_RULE));
        
        if ( memPattern.isEmpty() ) {
            return valueOperationStopped();
        }
        
        boolean isExactSearch = false;
        Choice choice;
        List<InvocationCommand> foundCommands;        
        searching: while ( true ) { 
            
            choice = this.ioEngine.ask(initiator, "exact match");
            if ( choice.isPositive() ) {
                isExactSearch = true;
            } else if ( choice.isNotMade() || choice.isNegative() ) {
                isExactSearch = false;
            } else if ( choice.isRejected() ) {
                return valueOperationStopped();
            } 
            
            if ( memPattern.isEmpty() ) {
                memPattern.set(helper.validateInteractively(
                        initiator, memPattern.get(), "mem", TEXT_RULE));
                if ( memPattern.isEmpty() ) {
                    return valueOperationStopped();
                }
            }            
            
            if ( isExactSearch ) {
                foundCommands = this.daoCommands
                        .getByExactOriginalOfAnyType(initiator, memPattern.get());
            } else {
                foundCommands = this.daoCommands
                        .searchInOriginalByPattern(initiator, memPattern.get());
                foundCommands.addAll(this.daoCommands
                        .searchInExtendedByPattern(initiator, memPattern.get()));
                foundCommands = foundCommands.stream().distinct().collect(toList());
            }
            
            if ( foundCommands.isEmpty() ) {
                this.ioEngine.report(initiator, format("not found by '%s'", memPattern.get()));
                memPattern.setEmpty();
                continue searching;
            } else {
                return valueCompletedWith(foundCommands);
            }
        } 
    }

    @Override
    public VoidOperation remove(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_MEM) ) {
            return voidOperationFail("wrong command type!");
        }
        
        StringHolder memPattern;
        if ( command.hasArguments() ) {
            memPattern = hold(command.joinedArguments());
        } else {
            memPattern = empty();
        }
        
        ValueOperation<List<InvocationCommand>> commandsFlow = 
                this.findCommands(initiator, memPattern);        
        
        switch ( commandsFlow.result() ) {
            case COMPLETE : {
                return this.chooseOneCommandAndRemoveIt(
                        initiator, memPattern.get(), commandsFlow.asComplete().getOrThrow());
            }
            case FAIL : {
                return voidOperationFail(commandsFlow.asFail().reason());
            }
            case STOP : {
                return voidOperationStopped();
            }
            default : {
                return voidOperationFail("unknown ValueOperation result.");
            }
        }
    }
    
    private VoidOperation chooseOneCommandAndRemoveIt(
            Initiator initiator, String memPattern, List<InvocationCommand> commands) {
        if ( hasOne(commands) ) {
            return this.removeCommand(initiator, getOne(commands));
        } else if ( hasMany(commands) ) {            
            return this.chooseOneAndRemoveCommand(initiator, memPattern, commands);
        } else {
            return voidOperationStopped();
        }
    }
    
    private VoidOperation removeCommand(
            Initiator initiator, InvocationCommand command) {
        this.ioEngine.report(initiator, format("found: %s", command.toMessageString()));
        Choice choice = this.ioEngine.ask(initiator, "remove all related mems also");
        if ( choice.isPositive() ) {
            this.daoCommandsChoices.delete(command);
            boolean removed = this.daoCommands.delete(initiator, command);
            return this.reportRemoving(removed);
        } else if ( choice.isNotMade() || choice.isNegative() ) {
            boolean removed = this.daoCommands.deleteByExactOriginalOfType(
                    initiator, command.originalArgument(), command.type());
            return this.reportRemoving(removed);
        } else if ( choice.isRejected() ) {
            return voidOperationStopped();
        } else {
            return voidOperationFail("cannot determine choice.");
        }
    }

    private VoidOperation reportRemoving(boolean removed) {
        if ( removed ) {
            return voidCompleted("removed.");
        } else {
            return voidOperationFail("DAO cannot remove command.");
        }
    }
    
    private VoidOperation chooseOneAndRemoveCommand(
            Initiator initiator, String memPattern, List<InvocationCommand> commands) {
        ValueOperation<InvocationCommand> removedFlow = 
                    this.justChooseOneCommand(initiator, memPattern, commands);
        
        switch ( removedFlow.result() ) {
            case COMPLETE : {
                if ( removedFlow.asComplete().hasValue() ) {
                    return this.removeCommand(initiator, removedFlow.asComplete().getOrThrow());
                } else {
                    return voidOperationStopped();
                }
            }
            case FAIL : {
                return voidOperationFail(removedFlow.asFail().reason());
            }
            case STOP : {
                return voidOperationStopped();
            }
            default : {
                return voidOperationFail("unknown ValueOperation result.");
            }  
        }
    }

    @Override
    public VoidOperation tryToExtendCommand(
            Initiator initiator, InvocationCommand command) {
        Optional<InvocationCommand> exactMatch = this.daoCommands.getByExactOriginalAndType(
                initiator, command.originalArgument(), command.type());        
        debug("[COMMANDS MEMORY] [find by exact match] " + command.originalArgument());
        if ( exactMatch.isPresent() ) {
            debug("[COMMANDS MEMORY] [find by exact match] found " + command.extendedArgument());
            List<InvocationCommand> matchingCommands = 
                    this.daoCommands.searchInExtendedByPatternAndType(
                            initiator, command.originalArgument(), command.type());
            if ( nonEmpty(matchingCommands) ) {
                return this.replaceExtendedIfNecessary(
                        initiator, command, exactMatch.get(), matchingCommands);
            } else {
                return voidCompleted();
            } 
        } else {
            command.setNew();
            return voidCompleted();
        }
    }  

    private VoidOperation replaceExtendedIfNecessary(
            Initiator initiator, 
            InvocationCommand command, 
            InvocationCommand exactMatch, 
            List<InvocationCommand> matchingCommands) {
        matchingCommands.add(exactMatch);
        WeightedVariants variants = weightVariants(
                command.originalArgument(), commandsToVariants(matchingCommands));
        if ( variants.isEmpty() ) {
            return voidCompleted();
        }
        variants.removeWorseThan(exactMatch.extendedArgument());
        if ( variants.hasOne() ) {
            command.setStored();
            command.argument().setExtended(exactMatch.extendedArgument());
            return voidCompleted();
        } else {
            if ( this.daoCommandsChoices.isChoiceDoneFor(command.originalArgument(), variants) ) {
                debug("[COMMANDS MEMORY] choice is done for original: " + command.originalArgument() + ", return " + exactMatch.stringify());
                command.setStored();
                command.argument().setExtended(exactMatch.extendedArgument());
                asyncDo(() -> {
                    this.daoCommands.save(initiator, command);
                });
                return voidCompleted();
            } else {
                Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
                if ( answer.isGiven() ) {
                    command.setStored();
                    command.argument().setExtended(answer.text());
                    asyncDo(() -> {
                        this.daoCommandsChoices.save(command, variants);
                        this.daoCommands.save(initiator, command);
                    });
                    return voidCompleted();
                } else if ( answer.isRejection() ) {
                    command.setNew();
                    return voidOperationStopped();
                } else {
                    command.setNew();
                    return voidCompleted();
                }
            }            
        }
    }
    
    @Override
    public VoidOperation tryToExtendCommandByPattern(
            Initiator initiator, InvocationCommand command) {
        List<InvocationCommand> foundCommands;
        foundCommands = this.daoCommands.searchInOriginalByPatternAndType(
                initiator, command.originalArgument(), command.type());
        if ( nonEmpty(foundCommands) ) {
            return this.chooseOneCommandAndUseAsExtension(initiator, foundCommands, command);
        } else {
            foundCommands = this.daoCommands.searchInExtendedByPatternAndType(
                    initiator, command.originalArgument(), command.type());
            if ( nonEmpty(foundCommands) ) {
                return this.chooseOneCommandAndUseAsExtension(initiator, foundCommands, command);
            } else {
                return voidCompleted();
            }
        }
    }

    private VoidOperation chooseOneCommandAndUseAsExtension(
            Initiator initiator, 
            List<InvocationCommand> foundCommands, 
            InvocationCommand command) {
        if ( hasOne(foundCommands) ) {
            command.argument().setExtended(getOne(foundCommands).extendedArgument());
            return voidCompleted();
        } else {
            ValueOperation<InvocationCommand> commandFlow = this.chooseOneCommandAndSaveChoice(
                    initiator, command.originalArgument(), foundCommands);
            switch ( commandFlow.result() ) {
                case COMPLETE : {
                    if ( commandFlow.asComplete().hasValue() ) {
                        command.argument().setExtended(
                                commandFlow.asComplete().getOrThrow().extendedArgument());
                    } 
                    return voidCompleted();
                }
                case FAIL : {
                    return voidOperationFail(commandFlow.asFail().reason());
                }
                case STOP : {
                    return voidOperationStopped();
                }
                default : {
                    return voidOperationFail("unkown ValueOperation result.");
                }
            }
        }        
    }

    @Override
    public void save(Initiator initiator, InvocationCommand command) {
        switch ( command.type() ) {
            case OPEN_LOCATION_TARGET : {
                List<InvocationCommand> derived = 
                        ((OpenLocationTargetCommand) command).decompose();
                this.daoCommands.save(initiator, derived);
                break;
            }
            default : {
                this.daoCommands.save(initiator, command);
            }
        }
    }

    @Override
    public void remove(Initiator initiator, InvocationCommand command) {
        this.daoCommandsChoices.delete(command);
        this.daoCommands.delete(initiator, command);
    }
    
    @Override
    public void removeByExactOriginalAndType(
            Initiator initiator, String original, CommandType type) {
        this.daoCommands.deleteByExactOriginalOfType(initiator, original, type);
    }

    @Override
    public ValueOperation<InvocationCommand> findStoredCommandOfAnyType(
            Initiator initiator, String original) {
        debug("[COMMANDS MEMORY] find stored by : " + original);
        List<InvocationCommand> foundCommands = 
                this.daoCommands.getByExactOriginalOfAnyType(initiator, original);
        if ( hasOne(foundCommands) ) {
            return this.doWhenOneFoundByExactAndType(
                    initiator, original, getOne(foundCommands), Optional.empty());
        } else if ( hasMany(foundCommands) ) {
            debug("[COMMANDS MEMORY] many found by exact: " + foundCommands);
            return this.chooseOneCommandAndSaveChoice(initiator, original, foundCommands);
        } else {
            return this.doWhenNoOneFoundByExactAndType(initiator, original, Optional.empty());
        }
    }
    
    private ValueOperation<InvocationCommand> doWhenOneFoundByExactAndType(
            Initiator initiator, 
            String original, 
            InvocationCommand foundCommand, 
            Optional<CommandType> type) {
        debug("[COMMANDS MEMORY] found one stored by exact : " + foundCommand.stringify());
        InvocationCommand exactMatch = foundCommand;
        if ( exactMatch.extendedArgument().equalsIgnoreCase(original) ) {
            debug("[COMMANDS MEMORY] exact match! " + original + " -> " + foundCommand.stringify());
            return valueCompletedWith(exactMatch);
        }
        
        List<InvocationCommand> matchingCommands;        
        if ( type.isPresent() ) {
            matchingCommands = this.daoCommands.searchInExtendedByPatternAndType(
                    initiator, original, type.get());
        } else {
            matchingCommands = this.daoCommands.searchInExtendedByPattern(initiator, original);
        }
                
        matchingCommands.add(exactMatch);
        WeightedVariants variants = weightVariants(original, commandsToVariants(matchingCommands));
        if ( variants.isEmpty() ) {
            return valueCompletedEmpty();
        }
        variants.removeWorseThan(exactMatch.extendedArgument());
        if ( variants.hasOne() ) {
            InvocationCommand newCommand = createInvocationCommandFrom(
                    matchingCommands.get(variants.best().index()).type(), 
                    original, 
                    variants.best().text());
            if ( exactMatch.equals(newCommand) ) {
                return valueCompletedWith(exactMatch);
            }
            asyncDo(() -> {                    
                this.daoCommands.deleteByExactOriginalOfType(
                        initiator, exactMatch.originalArgument(), exactMatch.type());
                this.daoCommands.save(initiator, newCommand);
            });
            return valueCompletedWith(newCommand);
        } else {
            if ( this.daoCommandsChoices.isChoiceDoneFor(original, variants) ) {
                debug("[COMMANDS MEMORY] choice is done for original: " + original + ", return " + exactMatch.stringify());
                return valueCompletedWith(exactMatch);
            }
            Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
            if ( answer.isGiven() ) {
                InvocationCommand newCommand = createInvocationCommandFrom(
                        matchingCommands.get(answer.index()).type(),
                        original, 
                        answer.text());
                if ( exactMatch.equals(newCommand) ) {
                    asyncDo(() -> {
                        this.daoCommandsChoices.save(exactMatch, variants);
                    });
                    return valueCompletedWith(exactMatch);
                } else {
                    asyncDo(() -> {
                        this.daoCommands.deleteByExactOriginalOfType(
                                initiator, exactMatch.originalArgument(), exactMatch.type());
                        this.daoCommands.save(initiator, newCommand);
                        this.daoCommandsChoices.save(newCommand, variants);
                    });
                    return valueCompletedWith(newCommand);
                }                
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
    }
    
    private ValueOperation<InvocationCommand> doWhenNoOneFoundByExactAndType(
            Initiator initiator, String original, Optional<CommandType> type) {
        debug("[COMMANDS MEMORY] not found by exact original: " + original);
        List<InvocationCommand> foundCommands;
        if ( type.isPresent() ) {
            foundCommands = this.daoCommands.searchInExtendedByPatternAndType(
                    initiator, original, type.get());
        } else {
            foundCommands = this.daoCommands.searchInExtendedByPattern(initiator, original);
        }
                
        if ( hasOne(foundCommands) ) {
            debug("[COMMANDS MEMORY] found one by original in extended: " + original + " -> " + getOne(foundCommands).extendedArgument() );
            Choice choice = this.ioEngine.ask(initiator, getOne(foundCommands).stringify());
            switch ( choice ) {
                case POSITIVE : {
                    InvocationCommand found = getOne(foundCommands);
                    InvocationCommand newCommand = createInvocationCommandFrom(
                            found.type(), original, found.extendedArgument());
                    asyncDo(() -> {                        
                        this.daoCommands.save(initiator, newCommand);
                    });
                    return valueCompletedWith(newCommand);
                }
                case NEGATIVE : {
                    return valueCompletedEmpty();
                }
                case REJECT : {
                    return valueOperationStopped();
                }
                case NOT_MADE : 
                default : {
                    return valueCompletedEmpty();
                }
            }
        } else if ( hasMany(foundCommands) ) {
            debug("[COMMANDS MEMORY] found many by original in extended: " + original + " -> " + foundCommands);
            WeightedVariants variants = weightVariants(original, commandsToVariants(foundCommands));
            if ( variants.isEmpty() ) {
                return valueCompletedEmpty();
            }
            Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
            if ( answer.isGiven() ) {                    
                InvocationCommand newCommand = createInvocationCommandFrom(
                        foundCommands.get(answer.index()).type(),
                        original, 
                        answer.text());
                asyncDo(() -> {
                    variants.removeWorseThan(newCommand.extendedArgument());
                    this.daoCommandsChoices.save(newCommand, variants);
                    this.daoCommands.save(initiator, newCommand);
                });
                return valueCompletedWith(newCommand);
            } else if ( answer.isRejection() ) {
                return valueOperationStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueCompletedEmpty();
            } else {
                return valueCompletedEmpty();
            }
        } else {
            return valueCompletedEmpty();
        }
    }
    
    private ValueOperation<InvocationCommand> justChooseOneCommand(
            Initiator initiator, 
            String pattern, 
            List<InvocationCommand> commands) {
        WeightedVariants variants = weightVariants(pattern, commandsToVariants(commands));
        if ( variants.isEmpty() ) {
            return valueCompletedEmpty();
        }
        debug("[COMMANDS MEMORY] [chosing one] variants qty: " + variants.size() );
        if ( variants.best().text().equalsIgnoreCase(pattern) ) {
            return valueCompletedWith(commands.get(variants.best().index()));
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
        if ( answer.isGiven() ) {
            debug("[COMMANDS MEMORY] [choosen one] " + commands.get(answer.index()).stringify() );
            return valueCompletedWith(commands.get(answer.index()));
        } else {
            debug("[COMMANDS MEMORY] [chosing one] answer not given");
            if ( answer.isRejection() ) {
                return valueOperationStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueCompletedEmpty();
            } else {
                return valueCompletedEmpty();
            }            
        }
    }
    
    private ValueOperation<InvocationCommand> chooseOneCommandAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            List<InvocationCommand> commands) {
        WeightedVariants variants = weightVariants(pattern, commandsToVariants(commands));
        if ( variants.isEmpty() ) {
            return valueCompletedEmpty();
        }
        debug("[COMMANDS MEMORY] [chosing one] variants qty: " + variants.size() );
        if ( variants.best().text().equalsIgnoreCase(pattern) ) {
            return valueCompletedWith(commands.get(variants.best().index()));
        }
        Optional<CommandType> chosenType = 
                this.daoCommandsChoices.isTypeChoiceDoneFor(pattern, variants);
        if ( chosenType.isPresent() ) {
            debug("[COMMANDS MEMORY] [chosing one] choice " + chosenType.get() + " found for stamp:" + variants.stamp());
            return valueCompletedWith(commands
                    .stream()
                    .filter(command -> command.type().is(chosenType.get()))
                    .findFirst());
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
        if ( answer.isGiven() ) {
            InvocationCommand chosen = commands.get(answer.index());
            debug("[COMMANDS MEMORY] [choosen one] " + chosen.stringify());
            asyncDo(() -> {
//                variants.removeWorseThan(chosen.extendedArgument());
                this.daoCommandsChoices.save(chosen, variants);
            });
            return valueCompletedWith(commands.get(answer.index()));
        } else {
            debug("[COMMANDS MEMORY] [chosing one] answer not given");
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
    public ValueOperation<InvocationCommand> findStoredCommandByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        Optional<InvocationCommand> found = 
                this.daoCommands.getByExactOriginalAndType(initiator, pattern, type);
        if ( found.isPresent() ) {
            return this.doWhenOneFoundByExactAndType(
                    initiator, pattern, found.get(), Optional.of(type));
        } else {
            return this.doWhenNoOneFoundByExactAndType(
                    initiator, pattern, Optional.of(type));
        }
    }

    @Override
    public void removeByExactExtendedAndType(
            Initiator initiator, String extended, CommandType type) {
        this.daoCommands.deleteByExactExtendedOfType(initiator, extended, type);
    }

    @Override
    public void removeByExactExtendedLocationPrefixInPath(
            Initiator initiator, String extended) {
        this.daoCommands.deleteByPrefixInExtended(initiator, extended, OPEN_LOCATION_TARGET);
    }
}
