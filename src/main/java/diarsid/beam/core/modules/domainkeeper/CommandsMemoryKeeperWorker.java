/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;
import diarsid.beam.core.modules.data.DaoCommands;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.commandsToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.Commands.createInvocationCommandFrom;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.domain.patternsanalyze.Analyze.weightVariants;

/**
 *
 * @author Diarsid
 */
class CommandsMemoryKeeperWorker implements CommandsMemoryKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoCommands daoCommands;

    CommandsMemoryKeeperWorker(DaoCommands daoCommands, InnerIoEngine ioEngine) {
        this.daoCommands = daoCommands;
        this.ioEngine = ioEngine;
    }

    @Override
    public void tryToExtendCommand(Initiator initiator, InvocationCommand command) {
        Optional<InvocationCommand> exactMatch = this.daoCommands.getByExactOriginalAndType(
                initiator, command.originalArgument(), command.type());        
        debug("[COMMANDS MEMORY] [find by eaxct match] " + command.originalArgument());
        if ( exactMatch.isPresent() ) {
            debug("[COMMANDS MEMORY] [find by eaxct match] found " + command.extendedArgument());
            List<InvocationCommand> matchingCommands = 
                    this.daoCommands.searchInExtendedByPatternAndType(
                            initiator, command.originalArgument(), command.type());
            if ( nonEmpty(matchingCommands) ) {
                this.replaceExtendedIfNecessary(
                        initiator, command, exactMatch.get(), matchingCommands);
            }            
        } else {
            command.setNew();
        }
    }  

    private void replaceExtendedIfNecessary(
            Initiator initiator, 
            InvocationCommand command, 
            InvocationCommand exactMatch, 
            List<InvocationCommand> matchingCommands) {
        matchingCommands.add(exactMatch);
        WeightedVariants variants = weightVariants(
                command.originalArgument(), commandsToVariants(matchingCommands));
        variants.removeWorseThan(exactMatch.extendedArgument());
        if ( variants.hasOne() ) {
            command.setStored();
            command.argument().setExtended(exactMatch.extendedArgument());
        } else {
            Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
            if ( answer.isGiven() ) {
                command.setStored();
                command.argument().setExtended(answer.text());
                asyncDo(() -> {
                    this.daoCommands.save(initiator, command);
                });
            } else {
                command.setNew();
            }
        }
    }
    
    @Override
    public void tryToExtendCommandByPattern(Initiator initiator, InvocationCommand command) {
        List<InvocationCommand> foundCommands;
        foundCommands = this.daoCommands.searchInOriginalByPatternAndType(
                initiator, command.originalArgument(), command.type());
        if ( nonEmpty(foundCommands) ) {
            this.chooseOneCommandAndUseAsExtension(initiator, foundCommands, command);
        } else {
            foundCommands = this.daoCommands.searchInExtendedByPatternAndType(
                    initiator, command.originalArgument(), command.type());
            if ( nonEmpty(foundCommands) ) {
                this.chooseOneCommandAndUseAsExtension(initiator, foundCommands, command);
            }
        }
    }

    private void chooseOneCommandAndUseAsExtension(
            Initiator initiator, 
            List<InvocationCommand> foundCommands, 
            InvocationCommand command) {
        if ( hasOne(foundCommands) ) {
            command.argument().setExtended(getOne(foundCommands).extendedArgument());
        } else {
            ValueOperation<InvocationCommand> commandFlow = 
                    this.chooseOneCommand(initiator, command.originalArgument(), foundCommands);
            switch ( commandFlow.result() ) {
                case COMPLETE : {
                    if ( commandFlow.asComplete().hasValue() ) {
                        command.argument().setExtended(
                                commandFlow.asComplete().getOrThrow().extendedArgument());
                    } 
                    break; 
                }
                case FAIL : {
                    this.ioEngine.report(initiator, commandFlow.asFail().reason());
                    break; 
                }
                case STOP : {
                    break; 
                }
                default : {
                    this.ioEngine.report(initiator, "unkown ValueOperation result.");
                    break; 
                }
            }
        }        
    }

    @Override
    public void save(Initiator initiator, InvocationCommand command) {
        debug("[COMMANDS MEMORY] saving: " + command.stringifyOriginal() + ":" + command.stringify());
        boolean created = this.daoCommands.save(initiator, command);
        if ( created ) {
            debug("[COMMANDS MEMORY] saved.");
        }
    }

    @Override
    public void remove(Initiator initiator, InvocationCommand command) {
        debug("[COMMANDS MEMORY] removing: " + command.stringifyOriginal() + ":" + command.stringify());
        boolean removed = this.daoCommands.delete(initiator, command);
        if ( removed ) {
            debug("[COMMANDS MEMORY] removed.");
        }
    }

    @Override
    public ValueOperation<InvocationCommand> findStoredCommandByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        List<InvocationCommand> foundCommands = 
                this.daoCommands.getByExactOriginalOfAnyType(initiator, original);
        if ( hasOne(foundCommands) ) {
            List<InvocationCommand> matchingCommands = 
                    this.daoCommands.searchInExtendedByPattern(initiator, original);
            InvocationCommand exactMatch = getOne(foundCommands);
            matchingCommands.add(exactMatch);
            WeightedVariants variants = 
                    weightVariants(original, commandsToVariants(matchingCommands));
            variants.removeWorseThan(exactMatch.extendedArgument());
            if ( variants.hasOne() ) {
                InvocationCommand newCommand = createInvocationCommandFrom(
                        matchingCommands.get(variants.best().index()).type(), 
                        original, 
                        variants.best().text());
                asyncDo(() -> {
                    this.daoCommands.save(initiator, newCommand);
                    this.daoCommands.deleteByExactOriginalOfType(
                            initiator, exactMatch.originalArgument(), exactMatch.type());
                });
                return valueCompletedWith(newCommand);
            } else {
                Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
                if ( answer.isGiven() ) {
                    InvocationCommand newCommand = createInvocationCommandFrom(
                            matchingCommands.get(answer.index()).type(),
                            original, 
                            answer.text());
                    asyncDo(() -> {
                        this.daoCommands.save(initiator, newCommand);
                        this.daoCommands.deleteByExactOriginalOfType(
                                initiator, exactMatch.originalArgument(), exactMatch.type());
                    });
                    return valueCompletedWith(newCommand);
                } else {
                    return valueOperationStopped();
                }
            }
        } else if ( hasMany(foundCommands) ) {
            return this.obtainOneUsing(initiator, original, foundCommands);
        } else {
            return valueCompletedEmpty();
        }
    }

    public ValueOperation<InvocationCommand> obtainOneUsing(
            Initiator initiator, String original, List<InvocationCommand> foundCommands) {
        if ( hasOne(foundCommands) ) {
            debug("[COMMANDS MEMORY] [obtain one] " + getOne(foundCommands).stringify() );
            return valueCompletedWith(getOne(foundCommands));
        } else {
            return this.chooseOneCommand(initiator, original, foundCommands);
        }
    }
    
    private ValueOperation<InvocationCommand> chooseOneCommand(
            Initiator initiator, 
            String pattern, 
            List<InvocationCommand> commands) {
        WeightedVariants question = 
                weightVariants(pattern, commandsToVariants(commands));
        debug("[COMMANDS MEMORY] [choose one] variants qty: " + question.size() );
        Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, question);
        if ( answer.isGiven() ) {
            debug("[COMMANDS MEMORY] [choose one] " + commands.get(answer.index()).stringify() );
            return valueCompletedWith(commands.get(answer.index()));
        } else {
            debug("[COMMANDS MEMORY] [choose one] answer not given");
            return valueOperationStopped();
        }
    }
    
    @Override
    public ValueOperation<InvocationCommand> findStoredCommandByPatternOfAnyType(
            Initiator initiator, String pattern) {
        debug("[COMMANDS MEMORY] [find by pattern] " + pattern);
        List<InvocationCommand> foundCommands = 
                this.daoCommands.searchInOriginalByPattern(initiator, pattern);
        if ( nonEmpty(foundCommands) ) {
            debug("[COMMANDS MEMORY] [find by pattern] found in original");
            return this.obtainOneUsing(initiator, pattern, foundCommands);
        } else {
            foundCommands = this.daoCommands.searchInExtendedByPattern(initiator, pattern);
            if ( nonEmpty(foundCommands) ) {     
                debug("[COMMANDS MEMORY] [find by pattern] found in extended");
                return this.obtainOneUsing(initiator, pattern, foundCommands);
            } else {
                return valueCompletedEmpty();
            }
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
