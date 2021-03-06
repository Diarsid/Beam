/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.analyze.variantsweight.WeightAnalyze;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.VariantConversions.View;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.util.MutableString;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoCommands;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPatternChoices;
import diarsid.support.objects.Pool;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowDone;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.View.SHOW_VARIANT_TYPE;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.commandsToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_MEM;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_MEM;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.Commands.createInvocationCommandFrom;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_TEXT_CHARS;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.MutableString.emptyMutableString;
import static diarsid.beam.core.base.util.MutableString.mutableString;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.TEXT_RULE;

/**
 *
 * @author Diarsid
 */
class CommandsMemoryKeeperWorker implements CommandsMemoryKeeper {
    
    private final InnerIoEngine ioEngine;
    private final ResponsiveDaoCommands daoCommands;
    private final ResponsiveDaoPatternChoices daoPatternChoices;
    private final WeightAnalyze analyze;
    private final Pool<KeeperLoopValidationDialog> dialogPool;
    private final Help enterMemoryHelp;
    private final Help exactMatchHelp;
    private final Help removeRelatedMemsHelp;
    private final Help chooseOneCommandHelp;
    private final Help isOneCommandRelevantHelp;

    CommandsMemoryKeeperWorker(
            ResponsiveDaoCommands daoCommands, 
            ResponsiveDaoPatternChoices daoChoices, 
            InnerIoEngine ioEngine,
            WeightAnalyze analyze,
            Pool<KeeperLoopValidationDialog> dialogPool) {
        this.daoCommands = daoCommands;
        this.daoPatternChoices = daoChoices;
        this.ioEngine = ioEngine;
        this.analyze = analyze;
        this.dialogPool = dialogPool;
        this.enterMemoryHelp = this.ioEngine.addToHelpContext(
                "", 
                "" + UNACCEPTABLE_TEXT_CHARS.stream().collect(joining()));
        this.exactMatchHelp = this.ioEngine.addToHelpContext(
                "Choose search mode. ",
                "Exact will search by exact pattern match, while",
                "non-exact will search by pattern similarity.",
                "Use:",
                "   - y/yes/+ to choose exact search",
                "   - n/no to choose similarity search",
                "   - dot to break"
        );
        this.removeRelatedMemsHelp = this.ioEngine.addToHelpContext(
                "Choose whether you want remove other mems, linked to the same",
                "entity or pattern, or remove only one mem. If you choose 'yes', ",
                "all mems, related to specified name or pattern, will be removed ",
                "also. If you choose 'no', only specified mem will be removed.",
                "Use:",
                "   - y/yes/+ to remove all related mems",
                "   - n/no to remove only specified mem",
                "   - dot to break"
        );
        this.chooseOneCommandHelp = this.ioEngine.addToHelpContext(
                "Choose one command from given variants.",
                "Use:",
                "   - number to choose command",
                "   - part of command to choose it",
                "   - n/no to see other, less relevant commands",
                "   - dot to break"
        );
        this.isOneCommandRelevantHelp = this.ioEngine.addToHelpContext(
                "Choose if this command is relevant to what you are searching for.",
                "Use:",
                "   - y/yes/+ if command is relevant",
                "   - n/no if it is not relevant",
                "   - dot to break"
        );
    }

    @Override
    public ValueFlow<List<InvocationCommand>> findMems(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_MEM) ) {
            return valueFlowFail("wrong command type!");
        }
        
        MutableString memPattern;
        if ( command.hasArguments() ) {
            memPattern = mutableString(command.joinedArguments());
        } else {
            memPattern = emptyMutableString();
        }
        
        return this.findCommands(initiator, memPattern);
    }

    private ValueFlow<List<InvocationCommand>> findCommands(
            Initiator initiator, MutableString memPattern) {           
        try (KeeperLoopValidationDialog dialog = this.dialogPool.give()) {
            boolean isExactSearch = false;
            Choice choice;
            String mem;
            List<InvocationCommand> foundCommands;        
            dialog
                    .withRule(TEXT_RULE)
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(initiator, "mem", this.enterMemoryHelp);
                    })
                    .withOutputDestination((validationFail) -> {
                        this.ioEngine.report(initiator, validationFail);
                    });
            
            searching: while ( true ) {
                
                mem = dialog
                        .withInitialArgument(memPattern.get())
                        .validateAndGet();
                
                memPattern.muteTo(mem);

                if ( memPattern.isEmpty() ) {
                    return valueFlowStopped();
                }
                
                choice = this.ioEngine.ask(initiator, "exact match", this.exactMatchHelp);
                if ( choice.isPositive() ) {
                    isExactSearch = true;
                } else if ( choice.isNotMade() || choice.isNegative() ) {
                    isExactSearch = false;
                } else if ( choice.isRejected() ) {
                    return valueFlowStopped();
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
                    this.ioEngine.report(initiator, format("'%s' not found", memPattern.get()));
                    memPattern.empty();
                    continue searching;
                } else {
                    Variants variants = this.analyze.weightVariants(
                            memPattern.get(), commandsToVariants(foundCommands));
                    if ( variants.isNotEmpty() ) {
                       return this.getAllFoundUsingVariantsIndexes(foundCommands, variants.indexes());
                    } else {
                        this.ioEngine.report(initiator, format("'%s' not found", memPattern.get()));
                        memPattern.empty();
                        continue searching;
                    }                
                }
            }             
        }   
    }
    
    private ValueFlow<List<InvocationCommand>> getAllFoundUsingVariantsIndexes(
            List<InvocationCommand> foundCommands, IntStream indexes) {
        List<InvocationCommand> commands = indexes                
                .mapToObj(index -> foundCommands.get(index))
                .collect(toList());
        return valueFlowDoneWith(commands);
    }

    @Override
    public VoidFlow remove(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_MEM) ) {
            return voidFlowFail("wrong command type!");
        }
        
        MutableString memPattern;
        if ( command.hasArguments() ) {
            memPattern = mutableString(command.joinedArguments());
        } else {
            memPattern = emptyMutableString();
        }
        
        ValueFlow<List<InvocationCommand>> commandsFlow = 
                this.findCommands(initiator, memPattern);        
        
        switch ( commandsFlow.result() ) {
            case DONE : {
                return this.chooseOneCommandAndRemoveIt(
                        initiator, memPattern.get(), commandsFlow.asDone().orThrow());
            }
            case FAIL : {
                return voidFlowFail(commandsFlow.asFail().reason());
            }
            case STOP : {
                return voidFlowStopped();
            }
            default : {
                return voidFlowFail("unknown ValueFlow result.");
            }
        }
    }
    
    private VoidFlow chooseOneCommandAndRemoveIt(
            Initiator initiator, String memPattern, List<InvocationCommand> commands) {
        if ( hasOne(commands) ) {
            return this.removeCommand(initiator, getOne(commands));
        } else if ( hasMany(commands) ) {            
            return this.chooseOneAndRemoveCommand(initiator, memPattern, commands);
        } else {
            return voidFlowStopped();
        }
    }
    
    private VoidFlow removeCommand(
            Initiator initiator, InvocationCommand command) {
        this.ioEngine.report(initiator, format("found: %s", command.toMessageString()));
        Choice choice = this.ioEngine.ask(
                initiator, "remove all related mems also", this.removeRelatedMemsHelp);
        if ( choice.isPositive() ) {
            this.daoPatternChoices.delete(initiator, command);
            boolean removedDirect = this.daoCommands.delete(initiator, command);
            boolean removedOthers = this.daoCommands.deleteByPrefixInExtended(
                    initiator, command.extendedArgument(), command.type());            
            return this.reportRemoving(removedDirect || removedOthers);
        } else if ( choice.isNotMade() || choice.isNegative() ) {
            boolean removed = this.daoCommands.deleteByExactOriginalOfType(
                    initiator, command.originalArgument(), command.type());
            return this.reportRemoving(removed);
        } else if ( choice.isRejected() ) {
            return voidFlowStopped();
        } else {
            return voidFlowFail("cannot determine choice.");
        }
    }

    private VoidFlow reportRemoving(boolean removed) {
        if ( removed ) {
            return voidFlowDone("removed.");
        } else {
            return voidFlowFail("DAO cannot remove command.");
        }
    }
    
    private VoidFlow chooseOneAndRemoveCommand(
            Initiator initiator, String memPattern, List<InvocationCommand> commands) {
        ValueFlow<InvocationCommand> removedFlow = 
                    this.justChooseOneCommand(initiator, memPattern, commands);
        
        switch ( removedFlow.result() ) {
            case DONE : {
                if ( removedFlow.asDone().hasValue() ) {
                    return this.removeCommand(initiator, removedFlow.asDone().orThrow());
                } else {
                    return voidFlowDone(format("'%s' not found", memPattern));
                }
            }
            case FAIL : {
                return voidFlowFail(removedFlow.asFail().reason());
            }
            case STOP : {
                return voidFlowStopped();
            }
            default : {
                return voidFlowFail("unknown ValueFlow result.");
            }  
        }
    }

    @Override
    public VoidFlow tryToExtendCommand(
            Initiator initiator, InvocationCommand command) {
        Optional<InvocationCommand> exactMatch = this.daoCommands.getByExactOriginalAndType(
                initiator, command.originalArgument(), command.type());
        if ( exactMatch.isPresent() ) {
            List<InvocationCommand> matchingCommands = 
                    this.daoCommands.searchInExtendedByPatternAndType(
                            initiator, command.originalArgument(), command.type());
            if ( nonEmpty(matchingCommands) ) {
                return this.replaceExtendedIfNecessary(
                        initiator, command, exactMatch.get(), matchingCommands);
            } else {
                return voidFlowDone();
            } 
        } else {
            command.setNew();
            return voidFlowDone();
        }
    }  

    private VoidFlow replaceExtendedIfNecessary(
            Initiator initiator, 
            InvocationCommand command, 
            InvocationCommand exactMatch, 
            List<InvocationCommand> matchingCommands) {        
        filterMatchingCommandsOnLongerDuplicatesOfExactInExtended(exactMatch, matchingCommands);
        String exactMatchExtended = exactMatch.extendedArgument();
        if ( matchingCommands.isEmpty() ) {
            command.setStored();
            command.argument().setExtended(exactMatchExtended);
            return voidFlowDone();
        }
        matchingCommands.add(0, exactMatch);
        Variants variants = this.analyze.weightVariants(
                command.originalArgument(), 
                exactMatchExtended, 
                commandsToVariants(matchingCommands));
        if ( variants.isEmpty() ) {
            return voidFlowDone();
        }
//        variants.removeWorseThan(exactMatchExtended);
        if ( variants.hasOne() ) {
            command.setStored();
            command.argument().setExtended(exactMatchExtended);
            return voidFlowDone();
        } else {
            boolean exactMatchChoosen = this.daoPatternChoices.hasMatchOf(
                    initiator, command.originalArgument(), exactMatchExtended, variants);
            if ( exactMatchChoosen ) {
                command.setStored();
                command.argument().setExtended(exactMatchExtended);
                asyncDo(() -> {
                    this.daoCommands.save(initiator, command);
                });
                return voidFlowDone();
            } else {
                Answer answer = this.ioEngine.ask(
                        initiator, variants, this.chooseOneCommandHelp);
                if ( answer.isGiven() ) {
                    command.setStored();
                    command.argument().setExtended(answer.text());
                    asyncDo(() -> {
                        this.daoPatternChoices.save(initiator, command, variants);
                        this.daoCommands.save(initiator, command);
                    });
                    return voidFlowDone();
                } else if ( answer.isRejection() ) {
                    command.setNew();
                    return voidFlowStopped();
                } else {
                    command.setNew();
                    return voidFlowDone();
                }
            }            
        }
    }
    
    private void filterMatchingCommandsOnLongerDuplicatesOfExactInExtended(
            InvocationCommand exactMatch, List<InvocationCommand> matchingCommands) {
        InvocationCommand matchingCommand;
        for (int i = 0; i < matchingCommands.size(); i++) {
            matchingCommand = matchingCommands.get(i);
            if ( this.matchingCommandIsLongerDuplicateOfExact(
                    exactMatch.extendedArgument(), matchingCommand.extendedArgument()) ) {
                matchingCommands.remove(i);
                i--;
            }            
        }
    }
    
    private boolean matchingCommandIsLongerDuplicateOfExact(
            String exact, String matching) {
        return 
                exact.equalsIgnoreCase(matching) ||
                ( containsIgnoreCase(matching, exact) &&  matching.length() > exact.length() );
    }
    
    @Override
    public VoidFlow tryToExtendCommandByPattern(
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
                return voidFlowDone();
            }
        }
    }

    private VoidFlow chooseOneCommandAndUseAsExtension(
            Initiator initiator, 
            List<InvocationCommand> foundCommands, 
            InvocationCommand command) {
        if ( hasOne(foundCommands) ) {
            command.argument().setExtended(getOne(foundCommands).extendedArgument());
            return voidFlowDone();
        } else {
            ValueFlow<InvocationCommand> commandFlow = this.chooseOneCommandAndSaveChoice(
                    initiator, command.originalArgument(), foundCommands);
            switch ( commandFlow.result() ) {
                case DONE : {
                    if ( commandFlow.asDone().hasValue() ) {
                        command.argument().setExtended(
                                commandFlow.asDone().orThrow().extendedArgument());
                    } 
                    return voidFlowDone();
                }
                case FAIL : {
                    return voidFlowFail(commandFlow.asFail().reason());
                }
                case STOP : {
                    return voidFlowStopped();
                }
                default : {
                    return voidFlowFail("unkown ValueFlow result.");
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
        this.daoPatternChoices.delete(initiator, command);
        this.daoCommands.delete(initiator, command);
    }
    
    @Override
    public void removeByExactOriginalAndType(
            Initiator initiator, String original, CommandType type) {
        this.daoCommands.deleteByExactOriginalOfType(initiator, original, type);
    }

    @Override
    public ValueFlow<InvocationCommand> findStoredCommandOfAnyType(
            Initiator initiator, String original) {
        List<InvocationCommand> foundCommands = 
                this.daoCommands.getByExactOriginalOfAnyType(initiator, original);
        if ( hasOne(foundCommands) ) {
            return this.doWhenOneFoundByExactAndType(
                    initiator, original, getOne(foundCommands), Optional.empty(), SHOW_VARIANT_TYPE);
        } else if ( hasMany(foundCommands) ) {
            return this.chooseOneCommandAndSaveChoice(initiator, original, foundCommands);
        } else {
            return this.doWhenNoOneFoundByExactAndType(
                    initiator, original, Optional.empty(), SHOW_VARIANT_TYPE);
        }
    }
    
    private ValueFlow<InvocationCommand> doWhenOneFoundByExactAndType(
            Initiator initiator, 
            String original, 
            InvocationCommand foundCommand, 
            Optional<CommandType> type, 
            View view) {
        InvocationCommand exactMatch = foundCommand;
        if ( exactMatch.extendedArgument().equalsIgnoreCase(original) ) {
            return valueFlowDoneWith(exactMatch);
        }
        
        List<InvocationCommand> matchingCommands;        
        if ( type.isPresent() ) {
            matchingCommands = this.daoCommands.searchInExtendedByPatternAndTypeGroupByExtended(
                    initiator, original, type.get());
        } else {
            matchingCommands = this.daoCommands.searchInExtendedByPatternGroupByExtended(
                    initiator, original);
        }
        
        filterMatchingCommandsOnLongerDuplicatesOfExactInExtended(exactMatch, matchingCommands);
        if ( matchingCommands.isEmpty() ) {
            return valueFlowDoneWith(exactMatch);
        }
        matchingCommands.add(0, exactMatch);
        Variants variants = this.analyze.weightVariants(
                original, 
                exactMatch.extendedArgument(),
                commandsToVariants(matchingCommands, view));
        if ( variants.isEmpty() ) {
            return valueFlowDoneEmpty();
        }
//        variants.removeWorseThan(exactMatch.extendedArgument());
        if ( variants.hasOne() ) {
            InvocationCommand newCommand = createInvocationCommandFrom(
                    matchingCommands.get(variants.best().index()).type(), 
                    original, 
                    variants.best().value());
            if ( exactMatch.equals(newCommand) ) {
                return valueFlowDoneWith(exactMatch);
            }
            asyncDo(() -> {                    
                this.daoCommands.deleteByExactOriginalOfType(
                        initiator, exactMatch.originalArgument(), exactMatch.type());
                this.daoCommands.save(initiator, newCommand);
            });
            return valueFlowDoneWith(newCommand);
        } else {
            boolean exactMatchChoosen = this.daoPatternChoices.hasMatchOf(
                    initiator, original, exactMatch.extendedArgument(), variants);
            if ( exactMatchChoosen ) {
                return valueFlowDoneWith(exactMatch);
            }
            Answer answer = this.ioEngine.ask(
                    initiator, variants, this.chooseOneCommandHelp);
            if ( answer.isGiven() ) {
                InvocationCommand newCommand = createInvocationCommandFrom(
                        matchingCommands.get(answer.index()).type(),
                        original, 
                        answer.text());
                if ( exactMatch.equals(newCommand) ) {
                    asyncDo(() -> {
                        this.daoPatternChoices.save(initiator, exactMatch, variants);
                    });
                    return valueFlowDoneWith(exactMatch);
                } else {
                    asyncDo(() -> {
                        this.daoCommands.deleteByExactOriginalOfType(
                                initiator, exactMatch.originalArgument(), exactMatch.type());
                        this.daoCommands.save(initiator, newCommand);
                        this.daoPatternChoices.save(initiator, newCommand, variants);
                    });
                    return valueFlowDoneWith(newCommand);
                }                
            } else {
                if ( answer.isRejection() ) {
                    return valueFlowStopped();
                } else if ( answer.variantsAreNotSatisfactory() ) {
                    return valueFlowDoneEmpty();
                } else {
                    return valueFlowDoneEmpty();
                }                    
            }
        }
    }
    
    private ValueFlow<InvocationCommand> doWhenNoOneFoundByExactAndType(
            Initiator initiator, String original, Optional<CommandType> type, View view) {
        List<InvocationCommand> foundCommands;
        if ( type.isPresent() ) {
            foundCommands = this.daoCommands.searchInExtendedByPatternAndTypeGroupByExtended(
                    initiator, original, type.get());
        } else {
            foundCommands = this.daoCommands.searchInExtendedByPatternGroupByExtended(
                    initiator, original);
        }
                
        if ( hasOne(foundCommands) ) {
            InvocationCommand foundCommand = getOne(foundCommands);
            if ( ! this.analyze.isSatisfiable(original, foundCommand.extendedArgument() ) ) {
                return valueFlowDoneEmpty();
            }
            Choice choice = this.ioEngine.ask(
                    initiator, foundCommand.stringify(), this.isOneCommandRelevantHelp);
            switch ( choice ) {
                case POSITIVE : {                    
                    InvocationCommand newCommand = createInvocationCommandFrom(
                            foundCommand.type(), original, foundCommand.extendedArgument());
                    asyncDo(() -> {                        
                        this.daoCommands.save(initiator, newCommand);
                    });
                    return valueFlowDoneWith(newCommand);
                }
                case NEGATIVE : {
                    return valueFlowDoneEmpty();
                }
                case REJECT : {
                    return valueFlowStopped();
                }
                case NOT_MADE : 
                default : {
                    return valueFlowDoneEmpty();
                }
            }
        } else if ( hasMany(foundCommands) ) {
            Variants variants = this.analyze.weightVariants(
                    original, commandsToVariants(foundCommands, view));
            if ( variants.isEmpty() ) {
                return valueFlowDoneEmpty();
            }
            Answer answer = this.ioEngine.ask(
                    initiator, variants, this.chooseOneCommandHelp);
            if ( answer.isGiven() ) {                    
                InvocationCommand newCommand = createInvocationCommandFrom(
                        foundCommands.get(answer.index()).type(),
                        original, 
                        answer.text());
                asyncDo(() -> {
                    String argument = newCommand.extendedArgument();
                    Variants cleanedVariants = variants.removeWorseThan(argument);
                    this.daoPatternChoices.save(initiator, newCommand, variants);
                    this.daoCommands.save(initiator, newCommand);
                });
                return valueFlowDoneWith(newCommand);
            } else if ( answer.isRejection() ) {
                return valueFlowStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueFlowDoneEmpty();
            } else {
                return valueFlowDoneEmpty();
            }
        } else {
            return valueFlowDoneEmpty();
        }
    }
    
    private ValueFlow<InvocationCommand> justChooseOneCommand(
            Initiator initiator, 
            String pattern, 
            List<InvocationCommand> commands) {
        Variants variants = this.analyze.weightVariants(
                pattern, commandsToVariants(commands));
        if ( variants.isEmpty() ) {
            return valueFlowDoneEmpty();
        }
        if ( variants.best().value().equalsIgnoreCase(pattern) ) {
            return valueFlowDoneWith(commands.get(variants.best().index()));
        }
        Answer answer = this.ioEngine.ask(
                initiator, variants, this.chooseOneCommandHelp);
        if ( answer.isGiven() ) {
            return valueFlowDoneWith(commands.get(answer.index()));
        } else if ( answer.isRejection() ) {
            return valueFlowStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueFlowDoneEmpty();
        } else {
            return valueFlowDoneEmpty();
        } 
    }
    
    private ValueFlow<InvocationCommand> chooseOneCommandAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            List<InvocationCommand> commands) {
        Variants variants = this.analyze.weightVariants(
                pattern, commandsToVariants(commands));
        if ( variants.isEmpty() ) {
            return valueFlowDoneEmpty();
        }
        if ( variants.best().value().equalsIgnoreCase(pattern) ) {
            return valueFlowDoneWith(commands.get(variants.best().index()));
        }
        Optional<String> choice = 
                this.daoPatternChoices.findChoiceFor(initiator, pattern, variants);
        if ( choice.isPresent() ) {
            return valueFlowDoneWith(commands
                    .stream()
                    .filter(command -> command.isExtendedArgument(choice.get()))
                    .findFirst());
        }
        Answer answer = this.ioEngine.ask(
                initiator, variants, this.chooseOneCommandHelp);
        if ( answer.isGiven() ) {
            InvocationCommand chosen = commands.get(answer.index());
            asyncDo(() -> {
//                variants.removeWorseThan(chosen.extendedArgument());
                this.daoPatternChoices.save(initiator, chosen, variants);
            });
            return valueFlowDoneWith(commands.get(answer.index()));
        } else {
            if ( answer.isRejection() ) {
                return valueFlowStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueFlowDoneEmpty();
            } else {
                return valueFlowDoneEmpty();
            }            
        }
    }

    @Override
    public ValueFlow<InvocationCommand> findStoredCommandByPatternAndType(
            Initiator initiator, String pattern, CommandType type, View view) {
        Optional<InvocationCommand> found = 
                this.daoCommands.getByExactOriginalAndType(initiator, pattern, type);
        if ( found.isPresent() ) {
            return this.doWhenOneFoundByExactAndType(
                    initiator, pattern, found.get(), Optional.of(type), view);
        } else {
            return this.doWhenNoOneFoundByExactAndType(
                    initiator, pattern, Optional.of(type), view);
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
