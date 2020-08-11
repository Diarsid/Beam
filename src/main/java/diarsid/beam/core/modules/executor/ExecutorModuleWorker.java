/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.ValueFlowDone;
import diarsid.beam.core.base.control.flow.ValueFlowFail;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.PluginTaskCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.plugins.Plugin;
import diarsid.beam.core.base.os.treewalking.advanced.Walker;
import diarsid.beam.core.base.os.treewalking.listing.FileLister;
import diarsid.beam.core.base.os.treewalking.search.FileSearcher;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchPauseCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.domainkeeper.CommandsMemoryKeeper;
import diarsid.support.strings.StringUtils;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.FlowResult.DONE;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.FlowResult.STOP;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowDone;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.View.HIDE_VARIANT_TYPE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.extractLastElementFromPath;
import static diarsid.beam.core.base.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.base.util.PathUtils.extractTargetFromPath;
import static diarsid.beam.core.base.util.PathUtils.findDepthOf;
import static diarsid.beam.core.base.util.PathUtils.joinPathFrom;
import static diarsid.beam.core.base.util.PathUtils.joinToPath;
import static diarsid.beam.core.base.util.PathUtils.joinToPathFrom;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.domain.entities.Entities.asBatch;
import static diarsid.beam.core.domain.entities.Entities.asLocation;
import static diarsid.beam.core.domain.entities.Entities.asProgram;
import static diarsid.beam.core.domain.entities.Entities.asWebPage;
import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;
import static diarsid.beam.core.domain.entities.NamedEntityType.PROGRAM;
import static diarsid.beam.core.domain.entities.NamedEntityType.WEBPAGE;
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorker implements ExecutorModule {
    
    private final static Logger log = logFor(ExecutorModule.class);
    private final static int MAX_WALKER_DEPTH = 5;
    
    private final DomainKeeperModule domain;
    private final InnerIoEngine ioEngine;
    private final Walker walker;
    private final FileLister fileLister;    
    private final Map<String, Plugin> plugins;
    
    private boolean directSubPathManipulationDisabled = true;

    ExecutorModuleWorker(
            InnerIoEngine ioEngine, 
            DomainKeeperModule domain,
            Set<Plugin> plugins,
            FileSearcher fileSearcher,
            Walker walk,
            FileLister fileLister) {
        this.ioEngine = ioEngine;
        this.walker = walk;
        this.domain = domain;
        this.fileLister = fileLister;
        this.plugins = new HashMap<>();
        plugins.forEach(plugin -> this.plugins.put(plugin.name(), plugin)); 
    }

    private void asyncSaveCommandIfNecessary(
            Initiator initiator, InvocationCommand command) {
        if ( command.wasNotUsedBefore() && command.isTargetFound() ) {
            asyncDo(() -> {
                this.domain.commandsMemory().save(initiator, command);
            });
        }
    }

    private void saveCommandIfNecessary(
            Initiator initiator, InvocationCommand command) {
        if ( command.wasNotUsedBefore() && command.isTargetFound() ) {
            this.domain.commandsMemory().save(initiator, command);
        }
    }

    private void saveCommandsIfNecessary(
            Initiator initiator, InvocationCommand command1, InvocationCommand command2) {
        CommandsMemoryKeeper commandsMemory = this.domain.commandsMemory();
        if ( command1.wasNotUsedBefore() && command1.isTargetFound() ) {
            commandsMemory.save(initiator, command1);
        }
        if ( command2.wasNotUsedBefore() && command2.isTargetFound() ) {
            commandsMemory.save(initiator, command2);
        }
    }
    
    private void asyncSaveCommandIfNecessary(
            Initiator initiator, Optional<InvocationCommand> optCommand) {
        if ( optCommand.isPresent() ) {
            asyncDo(() -> {
                InvocationCommand command = optCommand.get();
                if ( command.wasNotUsedBefore() && command.isTargetFound() ) {
                    this.domain.commandsMemory().save(initiator, command);
                } 
            });                       
        }        
    }

    private void deleteCommandIfNecessary(
            Initiator initiator, InvocationCommand command) {
        if ( command.wasUsedBeforeAndStored() ) {
            this.domain.commandsMemory().remove(initiator, command);
        }
    }
    
    private ValueFlow<NamedEntity> findNamedEntity(
            Initiator initiator, InvocationCommand command) {
        if ( command.argument().isNotExtended() ) {
            VoidFlow flow = this.domain
                    .commandsMemory()
                    .tryToExtendCommand(initiator, command);
            switch ( flow.result() ) {
                case FAIL : {
                    return valueFlowFail(flow.message());
                }    
                case STOP : {
                    return valueFlowStopped();
                }  
                default : {
                    // just proceed.
                }
            }
        }
        if ( command.argument().isExtended() ) {
            ValueFlow<NamedEntity> entityFlow = this.domain
                    .entitiesOperatedBy(command)
                    .findByExactName(initiator, command.argument().extended());
            switch ( entityFlow.result() ) {
                case DONE : {
                    if ( entityFlow.asDone().hasValue() ) {
                        command.setTargetFound();
                        return entityFlow;
                    } else {
                        this.domain.commandsMemory().remove(initiator, command);
                        command.argument().unextend();
                        command.setNew().setTargetNotFound();
                        return this.findNamedEntityByNamePattern(initiator, command);
                    }
                }
                case FAIL : {
                    this.domain.commandsMemory().remove(initiator, command);
                    command.argument().unextend();
                    command.setNew().setTargetNotFound();
                    return this.findNamedEntityByNamePattern(initiator, command);
                }
                case STOP : {
                    return entityFlow;
                }
                default : {
                    return valueFlowFail("Unknown ValueFlow result.");
                }
            }
        } else {
            return this.findNamedEntityByNamePattern(initiator, command);
        }
    }

    private ValueFlow<NamedEntity> findNamedEntityByNamePattern(
            Initiator initiator, InvocationCommand command) {
        command.setNew();
        ValueFlow<NamedEntity> entityFlow = this.domain
                .entitiesOperatedBy(command)
                .findByNamePattern(initiator, command.argument().original());
        switch ( entityFlow.result() ) {
            case DONE : {
                if ( entityFlow.asDone().hasValue() ) {
                    command.argument().setExtended(entityFlow.asDone().orThrow().name());        
                    command.setTargetFound();
                    return entityFlow;
                } else {
                    return this.tryToFindEntityInExtendedCommands(initiator, command);
                }
            }
            case FAIL : {
                return this.tryToFindEntityInExtendedCommands(initiator, command);
            }
            case STOP : {
                return entityFlow;
            }
            default : {
                return valueFlowFail("Unknown ValueFlow result.");
            }            
        }
    }

    private ValueFlow<NamedEntity> tryToFindEntityInExtendedCommands(
            Initiator initiator, InvocationCommand command) {
        VoidFlow flow = this.domain.commandsMemory()
                .tryToExtendCommandByPattern(initiator, command);
        switch ( flow.result() ) {
            case FAIL:
                return valueFlowFail(flow.message());
            case STOP : {
                return valueFlowStopped();
            }  
            default : {
                // just proceed.
            }
        }
        if ( command.argument().isExtended() ) {
            ValueFlow<NamedEntity> entityFlow = this.domain
                    .entitiesOperatedBy(command)
                    .findByExactName(initiator, command.argument().extended());
            switch ( entityFlow.result() ) {
                case DONE : {
                    if ( entityFlow.asDone().hasValue() ) {
                        command.setNew().setTargetFound();
                        return entityFlow;
                    } else {
                        this.domain.commandsMemory().remove(initiator, command);
                        command.argument().unextend();
                        command.setTargetNotFound();
                        return valueFlowDoneEmpty();
                    }
                }
                case FAIL : {
                    this.domain.commandsMemory().remove(initiator, command);
                    command.argument().unextend();
                    command.setTargetNotFound();
                    return entityFlow;
                }
                case STOP : {
                    return entityFlow;
                }
                default : {
                    return valueFlowFail("Unknown ValueFlow result.");
                }
            }
        } else {
            command.setTargetNotFound();
            return valueFlowDoneEmpty();
        }
    }
    
    private void executeBatchInternally(Initiator initiator, Batch batch) {
        batch.batchedCommands()
                .stream()
                .map(batchedCommand -> batchedCommand.unwrap())
                .filter(command -> command.type().isNot(CALL_BATCH))
                .forEach(command -> this.dispatchCommandInternally(initiator, command));         
    }
    
    private void dispatchCommandInternally(Initiator initiator, ExecutorCommand command) {
        dispatching: switch ( command.type() ) {
            case OPEN_LOCATION : {
                this.openLocation(initiator, (OpenLocationCommand) command);
                break dispatching;
            }
            case OPEN_LOCATION_TARGET : {
                this.openLocationTarget(initiator, (OpenLocationTargetCommand) command);
                break;
            }
            case BROWSE_WEBPAGE : {
                this.browsePage(initiator, (BrowsePageCommand) command);
                break dispatching;
            }
            case RUN_PROGRAM : {
                this.runProgram(initiator, (RunProgramCommand) command);
                break dispatching;
            }
            case BATCH_PAUSE : {
                this.suspendExecution(initiator, (BatchPauseCommand) command);
                break dispatching;
            }
            case CALL_BATCH : {
                this.callBatch(initiator, (CallBatchCommand) command);
                break dispatching;
            }
            default : {
                // do nothing.
            }
        }   
    }
        
    private void suspendExecution(Initiator initiator, BatchPauseCommand pause) {
        try {
            suspending: switch ( pause.timePeriod() ) {
                case SECONDS : {
                    this.ioEngine.report(
                            initiator, format("...pause %s seconds", pause.duration()));
                    sleep( 1000 * pause.duration() );
                    break suspending;                
                }
                case MINUTES : {
                    this.ioEngine.report(
                            initiator, format("...pause %s minutes", pause.duration()));
                    sleep( 1000 * 60 * pause.duration() );
                    break suspending;                
                }
                case HOURS : {
                    this.ioEngine.report(
                            initiator, format("...pause %s hours", pause.duration()));
                    sleep( 1000 * 60 * 60 * pause.duration() );
                    break suspending;                
                }
                case DAYS : 
                case WEEKS : 
                case MONTHS : 
                case YEARS : {
                    this.ioEngine.report(
                            initiator, "cannot suspend execution to time more than hours.");
                    break suspending;                
                }
                case UNDEFINED : {
                    // do nothing.
                    break suspending;                
                }
                default : {
                    // do nothing.
                }
            }
        } catch (InterruptedException e) {
            logFor(this).error(e.getMessage(), e);
            this.ioEngine.report(initiator, "...pause interrupted: " + e.getMessage());
        }        
    }    

    @Override
    public void stopModule() {
        // do nothing;
    }
    
    @Override
    public void openLocation(
            Initiator initiator, OpenLocationCommand command) {
        ValueFlow<NamedEntity> entityFlow = this.findNamedEntity(initiator, command);
        switch ( entityFlow.result() ) {
            case DONE : {
                if ( entityFlow.asDone().hasValue() ) {
                    NamedEntity entity = entityFlow.asDone().orThrow();
                    if ( entity.is(LOCATION) ) {
                        this.openFoundLocation(initiator, command, asLocation(entity));                        
                    } else {
                        this.doWhenLocationNotFound(initiator, command);
                    }
                } else {
                    this.doWhenLocationNotFound(initiator, command);
                }
                break;
            }
            case FAIL : {
                this.doWhenOperationFailed(initiator, command, entityFlow.asFail());
                break;
            }
            case STOP : {
                break;
            }
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }
        }
    }

    private void openFoundLocation(
            Initiator initiator, OpenLocationCommand command, Location location) {
        this.ioEngine.report(initiator, "...opening " + location.name());
        location.openAsync(
                this.thenDoOnSuccess(initiator, command),
                this.thenDoOnFail(initiator, command));
    }
    
    private void doWhenLocationNotFound(
            Initiator initiator, OpenLocationCommand command) {        
        this.deleteCommandIfNecessary(initiator, command);
        ValueFlow<InvocationCommand> commandFlow = this.domain
                .commandsMemory()
                .findStoredCommandByPatternAndType(initiator, 
                        command.originalArgument(), 
                        OPEN_LOCATION_TARGET, 
                        HIDE_VARIANT_TYPE);
        switch ( commandFlow.result() ) {
            case DONE : {
                if ( this.isOpenLocationTargetCommand(commandFlow) ) {
                    this.openLocationTarget(
                            initiator, 
                            (OpenLocationTargetCommand) commandFlow.asDone().orThrow());
                } else {
                    this.reportEntityNotFound(initiator, command);
                }
                break;
            }
            case FAIL : {
                this.reportEntityNotFound(initiator, command, commandFlow.asFail());
                break;
            }
            case STOP : {
                break;
            }
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }
        }
    }
    
    private boolean isOpenLocationTargetCommand(ValueFlow<InvocationCommand> commandFlow) {
        return commandFlow.asDone().hasValue() && 
                commandFlow.asDone().orThrow().type().equals(OPEN_LOCATION_TARGET);
    }
    
    private String notFoundReportFrom(InvocationCommand command) {
        switch ( command.type() ) {
            case OPEN_LOCATION_TARGET : {
                return format("Path '%s' not found", command.argument().get());
            }
            case RUN_PROGRAM : {
                String argument = command.argument().get();
                if ( containsPathSeparator(argument) ) {
                    String programName = extractLastElementFromPath(argument);
                    if ( containsIgnoreCase(programName, "start")) {
                        return format("startable Program '%s' not found", 
                                programName.replace("start", ""));
                    } else {
                        return format("Program '%s' not found", programName);
                    }
                } else {
                    if ( containsIgnoreCase(argument, "start")) {
                        return format("startable Program '%s' not found", 
                                argument.replace("start", ""));
                    } else {
                        return format("Program '%s' not found", argument);
                    }
                }                    
            }
            default : {
                return format("%s '%s' not found", 
                        command.subjectedEntityType().displayName(),
                        command.argument().get());
            }
        }
    }
    
    private void reportEntityNotFound(
            Initiator initiator, InvocationCommand command) {
        this.ioEngine.report(initiator, this.notFoundReportFrom(command));
    }

    private void reportEntityNotFound(
            Initiator initiator, InvocationCommand command, ValueFlowFail valueFail) {
        this.ioEngine.report(initiator,
                format("cannot find %s by name '%s': %s", 
                        command.subjectedEntityType().displayName(), 
                        command.argument().get(), 
                        valueFail.reason()));
    }
    
    private void tryToFindLocationByPattern(
            Initiator initiator, OpenLocationTargetCommand command) {
        ValueFlow<Location> valueFlow = this.domain
                .locations()
                .findByNamePattern(initiator, command.originalLocation());
        switch ( valueFlow.result() ) {
            case DONE : {
                if ( valueFlow.asDone().hasValue() ) {
                    this.doWhenLocationFoundFromExtended(
                            initiator, command, valueFlow.asDone().orThrow());
                } else {
                    this.doWhenNotFound(initiator, command);
                }
                break;
            }
            case FAIL : {
                this.doWhenOperationFailed(initiator, command, valueFlow.asFail());
                break;
            }
            case STOP : {
                break;
            }
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }
        }
    }

    @Override
    public void openLocationTarget(Initiator initiator, OpenLocationTargetCommand command) {
        log.info(".open location target: " + command.stringify());
        if ( command.argument().isNotExtended() ) {
            VoidFlow flow = this.domain
                    .commandsMemory()
                    .tryToExtendCommand(initiator, command);
            switch ( flow.result() ) {
                case FAIL : {
                    this.ioEngine.report(initiator, flow.message());
                    return;
                }    
                case STOP : {
                    return;
                }  
                default : {
                    // just proceed.
                }
            }
        }    
        if ( command.argument().isExtended() ) {
            this.openLocationTargetUsingExtendedArgument(initiator, command);
        } else {
            this.openLocationTargetUsingOriginalArgument(initiator, command);
        }
    }

    private void openLocationTargetUsingOriginalArgument(
            Initiator initiator, OpenLocationTargetCommand command) {
        ValueFlow<Location> locationFlow;        
        command.setNew();
        locationFlow = this.domain
                .locations()
                .findByNamePattern(initiator, command.originalLocation());
        switch ( locationFlow.result() ) {
            case DONE : {
                if ( locationFlow.asDone().hasValue() ) {
                    Location location = locationFlow.asDone().orThrow();
                    String target = command.originalTarget();
                    if ( location.has(target) ) {
                        this.openTargetAndExtendCommand(initiator, location, target, command);
                    } else {
                        this.doWhenTargetNotFoundDirectly(initiator, location, command);
                    }
                } else {
                    this.doWhenLocationForTargetNotFound(initiator, command);
                }
                break;
            }
            case FAIL : {
                this.doWhenOperationFailed(initiator, command, locationFlow.asFail());
                break;
            }
            case STOP : {
                break;
            }
            default : {    
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }
        }
    }
    
    private void doWhenLocationForTargetNotFound(
            Initiator initiator, OpenLocationTargetCommand command) {
        if ( directSubPathManipulationDisabled ) {
            this.doWhenNotFound(initiator, command);
            return;
        }
        
        ValueFlow<LocationSubPath> subPathFlow = this.domain
                .locationSubPaths()
                .findLocationSubPath(initiator, command.originalLocation());
        
        switch ( subPathFlow.result() ) {
            case DONE : {
                if ( subPathFlow.asDone().hasValue() ) {
                    LocationSubPath locationSubPath = subPathFlow.asDone().orThrow();   
                    if ( locationSubPath.notPointsToDirectory() ) {
                        this.doWhenNotFound(initiator, command);
                        return;
                    }
                    if ( locationSubPath.has(command.originalTarget()) ) {
                        this.openTargetAndExtendCommand(
                                initiator, locationSubPath, command.originalTarget(), command);
                    } else {
                        this.useSubPathToFindTarget(
                                initiator, locationSubPath, command.originalTarget(), command);
                    }
                } else {
                    this.doWhenNotFound(initiator, command);
                }    
                break;
            }    
            case FAIL : {
                this.doWhenOperationFailed(initiator, command, subPathFlow.asFail());
                break;
            }    
            case STOP : {                
                break;
            }    
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
            }    
        }
    }    
    
    private void useSubPathToFindTarget(
            Initiator initiator, 
            LocationSubPath locationSubPath, 
            String target, 
            OpenLocationTargetCommand command) {
        command.setNew();
        this.useSubPathToFindTargetWithFileWalker(initiator, locationSubPath, target, command);              
    }
    
    private void useSubPathToFindTargetWithFileWalker(
            Initiator initiator, 
            LocationSubPath locationSubPath, 
            String target, 
            OpenLocationTargetCommand command) {
        ValueFlow<String> targetFlow = this.walker                
                .walkToFind(target)
                .withMaxDepthOf(5)
                .in(locationSubPath)
                .by(initiator)
                .andGetResult();
        
        switch ( targetFlow.result() ) {
            case DONE : {
                ValueFlowDone<String> doneFlow = targetFlow.asDone();
                if ( doneFlow.hasValue() ) {
                    target = joinToPath(locationSubPath.subPath(), doneFlow.orThrow());
                    this.openTargetAndExtendCommand(initiator, locationSubPath, target, command);
                } else {
                    if ( doneFlow.hasMessage() ) {
                        this.ioEngine.report(initiator, doneFlow.message());
                    } else {
                        this.ioEngine.report(initiator, format(
                                "'%s' not found in %s", target, locationSubPath.name()));
                    }                   
                }                
                break;
            }    
            case FAIL : {
                this.ioEngine.report(initiator, targetFlow.asFail().reason());
                break;
            }    
            case STOP : 
            default : {
                // do nothing
            }    
        }
    }

    private void openLocationTargetUsingExtendedArgument(
            Initiator initiator, OpenLocationTargetCommand command) {
        ValueFlow<Location> valueFlow;
        valueFlow = this.domain
                .locations()
                .findByExactName(initiator, command.extendedLocation());
        switch ( valueFlow.result() ) {
            case DONE : {
                if ( valueFlow.asDone().hasValue() ) {
                    this.doWhenLocationFoundFromExtended(
                            initiator, command, valueFlow.asDone().orThrow());
                } else {
                    this.tryToFindLocationByPattern(initiator, command);
                }
                break;
            }
            case FAIL : {
                this.tryToFindLocationByPattern(initiator, command);
                break;
            }
            case STOP : {
                break;
            }
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }
        }
    }

    private void doWhenOperationResultUndefined(Initiator initiator, InvocationCommand command) {
        this.domain.commandsMemory().remove(initiator, command);
        this.ioEngine.report(initiator, "undefined operation result.");
    }

    private void doWhenLocationFoundFromExtended(
            Initiator initiator, OpenLocationTargetCommand command, Location location) {
        String target;
        target = command.extendedTarget();
        if ( location.has(target) ) {
            command.setTargetFound();
            this.ioEngine.report(initiator, "...opening " + command.extendedArgument());
            location.openAsync(
                    target,
                    this.thenDoOnSuccess(initiator, command),
                    this.thenDoOnFail(initiator, command));
        } else {
            this.domain.commandsMemory().remove(initiator, command);
            this.doWhenTargetNotFoundDirectly(initiator, location, command);
        }
    }
    
    private void doWhenTargetNotFoundDirectly(
            Initiator initiator, Location location, OpenLocationTargetCommand command) {
        this.doWhenTargetNotFoundDirectly(initiator, location, command.originalTarget(), command);
    }

    private void doWhenTargetNotFoundDirectly(
            Initiator initiator, 
            Location location, 
            String target, 
            OpenLocationTargetCommand command) {
        command.setNew();
        this.searchForTargetInLocationSubPaths(initiator, location, target, command);
    }
    
    private void searchForTargetInLocationSubPaths(
            Initiator initiator, 
            Location location, 
            String target, 
            OpenLocationTargetCommand command) {
        behavior_v2_searchForTargetInLocationSubPaths(initiator, location, target, command);
    }

    private void behavior_v2_searchForTargetInLocationSubPaths(
            Initiator initiator, Location location, String target, OpenLocationTargetCommand command) {
        List<Variant> foundSubPathsVariants = this.domain
                .locationSubPaths()
                .findAllLocationSubPaths(initiator, location, target);
        
        if ( nonEmpty(foundSubPathsVariants) ) {
            List<Variant> possibleTargets = foundSubPathsVariants
                    .stream()
                    .map((pathVariant) -> {
                        String fullSubPath = pathVariant.value();
                        String pathWithoutLocation = extractTargetFromPath(fullSubPath);
                        return pathVariant
                                .retainInValueOnly(pathWithoutLocation)
                                .retainInNameOnly(fullSubPath);
                    })
                    .collect(toList());
            this.searchForTargetByFileWalkerWithFoundSubPaths(
                    initiator, location, target, possibleTargets, command);
        } else {
            this.searchForTargetByFileWalker(initiator, location, target, command);            
        }
    }

    private void searchForTargetByFileWalkerWithFoundSubPaths(
            Initiator initiator, 
            Location location, 
            String target, 
            List<Variant> pathsVariants,
            OpenLocationTargetCommand command) {
        int maxSubPathsDepth = pathsVariants
                .stream()
                .mapToInt(subPathVariant -> findDepthOf(subPathVariant.nameOrValue()))
                .max()
                .orElse(0);
        
        if ( location.hasSubPath() ) {
            int locationSubPathDepth = findDepthOf(location.name());
            maxSubPathsDepth = maxSubPathsDepth - locationSubPathDepth;
            if ( maxSubPathsDepth < 1 ) {
                maxSubPathsDepth = 1;
            }
        }
        
        int depthToGo = maxSubPathsDepth > MAX_WALKER_DEPTH ? maxSubPathsDepth : MAX_WALKER_DEPTH;
        
        ValueFlow<String> targetFlow = this.walker
                .walkToFind(target)
                .withPredefined(pathsVariants)
                .withMinDepthOf(maxSubPathsDepth)
                .withMaxDepthOf(depthToGo)
                .in(location)
                .by(initiator)
                .andGetResult();
        switch ( targetFlow.result() ) {
            case DONE : {
                ValueFlowDone<String> doneFlow = targetFlow.asDone();
                if ( doneFlow.hasValue() ) {
                    target = targetFlow.asDone().orThrow();
                    this.openTargetAndExtendCommand(initiator, location, target, command);
                } else {
                    if ( doneFlow.hasMessage() ) {
                        this.ioEngine.report(initiator, doneFlow.message());
                    } else {
                        this.ioEngine.report(initiator, format(
                                "'%s' not found in %s", target, location.name()));
                    }
                }                
                break;
            }    
            case FAIL : {
                this.ioEngine.report(initiator, targetFlow.asFail().reason());
                break;
            }
            case STOP:
            default : {
                // do nothing;
            }
        }
    }
    
    private void searchForTargetByFileWalker(
            Initiator initiator, 
            Location location, 
            String target, 
            OpenLocationTargetCommand command) {
        ValueFlow<String> targetFlow = this.walker
                .walkToFind(target)
                .withMaxDepthOf(MAX_WALKER_DEPTH)
                .in(location)
                .by(initiator)
                .andGetResult();
        switch ( targetFlow.result() ) {
            case DONE : {
                ValueFlowDone<String> doneFlow = targetFlow.asDone();
                if ( doneFlow.hasValue() ) {
                    target = targetFlow.asDone().orThrow();
                    this.openTargetAndExtendCommand(initiator, location, target, command);
                } else {
                    if ( doneFlow.hasMessage() ) {
                        this.ioEngine.report(initiator, doneFlow.message());
                    } else {
                        this.ioEngine.report(initiator, format(
                                "'%s' not found in %s", target, location.name()));
                    }
                }                
                break;
            }    
            case FAIL : {
                this.ioEngine.report(initiator, targetFlow.asFail().reason());
                break;
            }
            case STOP:
            default : {
                // do nothing;
            }
        }
    }

    private void openTargetAndExtendCommand(
            Initiator initiator, 
            Location location, 
            String target, 
            OpenLocationTargetCommand command) {
        command.argument().setExtended(location.relativePathTo(target));
        command.setTargetFound();
        
        CallbackEmpty doOnSuccess;
        if ( location.hasSubPath() ) {
            LocationSubPath originalFoundSubPath = (LocationSubPath) location;
            OpenLocationTargetCommand originalSubPathCommand = new OpenLocationTargetCommand(
                    command.originalLocation(), 
                    originalFoundSubPath.name(), 
                    NEW, 
                    TARGET_FOUND);
            doOnSuccess = this.thenDoOnSuccess(initiator, originalSubPathCommand, command);
        } else {
            doOnSuccess = this.thenDoOnSuccess(initiator, command);
        }
        
        this.ioEngine.report(initiator, "...opening " + command.extendedArgument());
        location.openAsync(
                target,
                doOnSuccess,
                this.thenDoOnFail(initiator, command));
    }

    @Override
    public void runProgram(Initiator initiator, RunProgramCommand command) {
        ValueFlow<NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
        switch ( valueFlow.result() ) {
            case DONE : {
                if ( valueFlow.asDone().hasValue() ) {
                    NamedEntity entity = valueFlow.asDone().orThrow();
                    if ( entity.is(PROGRAM) ) {
                        this.ioEngine.report(
                                initiator, "...running " + asProgram(entity).simpleName());
                        asProgram(entity).runAsync(
                                this.thenDoOnSuccess(initiator, command), 
                                this.thenDoOnFail(initiator, command));
                    } else {
                        this.doWhenNotFound(initiator, command);
                    }                    
                } else {
                    this.doWhenNotFound(initiator, command);
                }
                break;
            }    
            case FAIL : {
                this.doWhenOperationFailed(initiator, command, valueFlow.asFail());
                break;
            }    
            case STOP : {
                break;
            }    
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }    
        }
    }
    
    private void doWhenNotFound(
            Initiator initiator, InvocationCommand command) {
        this.reportEntityNotFound(initiator, command);
        this.deleteCommandIfNecessary(initiator, command);
    }

    private void doWhenOperationFailed(
            Initiator initiator, InvocationCommand command, ValueFlowFail valueFlow) {
        this.reportEntityNotFound(initiator, command, valueFlow);
        this.deleteCommandIfNecessary(initiator, command);
    }

    @Override
    public void callBatch(Initiator initiator, CallBatchCommand command) {
        ValueFlow<NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
        switch ( valueFlow.result() ) {
            case DONE : {
                if ( valueFlow.asDone().hasValue() ) {
                    NamedEntity entity = valueFlow.asDone().orThrow();
                    if ( entity.is(BATCH) ) {
                        this.ioEngine.report(initiator, "...executing " + asBatch(entity).name());
                        this.executeBatchInternally(initiator, asBatch(entity));
                        this.asyncSaveCommandIfNecessary(initiator, command);
                    } else {
                        this.doWhenNotFound(initiator, command);
                    }                    
                } else {
                    this.doWhenNotFound(initiator, command);
                }
                break;
            }                
            case FAIL : {
                this.doWhenOperationFailed(initiator, command, valueFlow.asFail());
                break;
            }              
            case STOP : {
                break;
            }              
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }              
        }
    }

    @Override
    public void browsePage(Initiator initiator, BrowsePageCommand command) {
        ValueFlow<NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
        switch ( valueFlow.result() ) {
            case DONE : {
                if ( valueFlow.asDone().hasValue() ) {
                    NamedEntity entity = valueFlow.asDone().orThrow();
                    if ( entity.is(WEBPAGE) ) {
                        this.ioEngine.report(initiator, "...browsing " + asWebPage(entity).name());
                        asWebPage(entity).browseAsync(
                                this.thenDoOnSuccess(initiator, command), 
                                this.thenDoOnFail(initiator, command));
                    } else {
                        this.doWhenNotFound(initiator, command);
                    }
                } else {
                    this.doWhenNotFound(initiator, command);
                }
                break;
            }    
            case FAIL : {
                this.doWhenOperationFailed(initiator, command, valueFlow.asFail());
                break;
            }    
            case STOP : {
                break;
            }    
            default : {
                this.doWhenOperationResultUndefined(initiator, command);
                break;
            }    
        }
    }

    private CallbackEvent thenDoOnFail(Initiator initiator, InvocationCommand command) {
        return (fail) -> {
            this.ioEngine.report(initiator, fail);
            this.deleteCommandIfNecessary(initiator, command);
        };
    }
    
    private CallbackEvent thenDoOnFail(Initiator initiator) {
        return (fail) -> {
            this.ioEngine.report(initiator, fail);
        };
    }

    private CallbackEmpty thenDoOnSuccess(Initiator initiator, InvocationCommand command) {
        return () -> {
            this.saveCommandIfNecessary(initiator, command);
        };
    }

    private CallbackEmpty thenDoOnSuccess(
            Initiator initiator, 
            OpenLocationTargetCommand command1, 
            OpenLocationTargetCommand command2) {
        return () -> {
            this.saveCommandsIfNecessary(initiator, command1, command2);
        };
    }
    
    private CallbackEmpty thenDoOnSuccess(
            Initiator initiator, Optional<InvocationCommand> command) {
        return () -> {
            if ( command.isPresent() ) {
                this.saveCommandIfNecessary(initiator, command.get());                
            }
        };
    }

    @Override
    public void executeDefault(Initiator initiator, ExecutorDefaultCommand command) {
        ValueFlow<NamedEntity> entityFlow = this.domain
                .allEntities()
                .findByExactName(initiator, command.argument());
        switch ( entityFlow.result() ) {
            case DONE : {
                if ( entityFlow.asDone().hasValue() ) {
                    VoidFlow invokeFLow = this.invokeFoundEntity(
                            initiator, entityFlow.asDone().orThrow(), command);
                    switch ( invokeFLow.result() ) {
                        case DONE : {
                            return;
                        }
                        case FAIL : {
                            this.ioEngine.report(initiator, invokeFLow.message());
                            break; 
                        }
                        case STOP : {
                            return;
                        }
                        default : {
                            // do nothing, just proceed.
                        }
                    }
                }
                break; 
            }
            case FAIL : {
                this.ioEngine.report(initiator, entityFlow.asFail().reason());
                break; 
            }
            case STOP : {
                return;
            }
            default : {
                // do nothing, proceed.
            }            
        }
        
        ValueFlow<InvocationCommand> flow = this.domain
                .commandsMemory()
                .findStoredCommandOfAnyType(initiator, command.argument());
        switch ( flow.result() ) {
            case DONE : {
                if ( flow.asDone().hasValue() ) {
                    this.dispatchCommandInternally(initiator, flow.asDone().orThrow());
                } else {
                    this.findAndInvokeAnyNamedEntity(initiator, command);
                }
                break; 
            }
            case FAIL : {
                this.ioEngine.report(initiator, flow.asFail().reason());
                break; 
            }
            case STOP : {
                break; 
            }
            default : {
                this.ioEngine.report(initiator, "unknown operation result.");
                break; 
            }
        }
    }
 
    private void findAndInvokeAnyNamedEntity(
            Initiator initiator, ExecutorDefaultCommand command) {
        ValueFlow<NamedEntity> entityFlow = 
                this.findNamedEntityByArgument(initiator, command.argument());
        switch ( entityFlow.result() ) {
            case DONE : {
                if ( entityFlow.asDone().hasValue() ) {
                    VoidFlow invokeFlow = this.invokeFoundEntity(
                            initiator, entityFlow.asDone().orThrow(), command);
                    switch ( invokeFlow.result() ) {
                        case DONE : 
                        case STOP : {
                            return;
                        }
                        case FAIL : {
                            this.ioEngine.report(initiator, invokeFlow.message());
                            return; 
                        }
                        default : {
                            this.ioEngine.report(initiator, "Unkown VoidFlow type.");
                            return; 
                        }
                    }
                } else {
                    // do nothing.
                }
                return;
            } 
            case FAIL : {
                this.ioEngine.report(initiator, entityFlow.asFail().reason());
                return;
            }
            case STOP : {
                return;
            }
            default : {
                this.ioEngine.report(initiator, "Unkown ValueFlow type.");
            }
        }
    }
    
    private ValueFlow<NamedEntity> findNamedEntityByArgument(
            Initiator initiator, String argument) {
        ValueFlow<NamedEntity> entityFlow = this.domain
                .allEntities()
                .findByExactName(initiator, argument);
        switch ( entityFlow.result() ) {
            case DONE : {
                if ( entityFlow.asDone().hasValue() ) {
                    return entityFlow;
                } else {
                    return this.domain
                            .allEntities()
                            .findByNamePattern(initiator, argument);
                }
            }
            case FAIL : {
                return this.domain
                        .allEntities()
                        .findByNamePattern(initiator, argument);
            }
            case STOP : {
                return entityFlow;
            }
            default : {
                return valueFlowFail("unknown ValueFlow result.");
            }
        }
    }
    
    private VoidFlow invokeFoundEntity(
            Initiator initiator, NamedEntity entity, ExecutorDefaultCommand command) {
        if ( entity.type().isNotDefined() ) {
            return voidFlowFail(format("...type of '%s' is not defined.", entity.name()));
        }        
        
        invocation: switch ( entity.type() ) {
            case LOCATION : {
                this.ioEngine.report(initiator, "...opening " + asLocation(entity).name());
                asLocation(entity).openAsync(
                        this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                        this.thenDoOnFail(initiator));
                return voidFlowDone();
            }
            case WEBPAGE : {
                this.ioEngine.report(initiator, "...browsing " + asWebPage(entity).name());
                asWebPage(entity).browseAsync(
                        this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                        this.thenDoOnFail(initiator));
                return voidFlowDone();
            }
            case PROGRAM : {
                this.ioEngine.report(initiator, "...running " + asProgram(entity).simpleName());
                asProgram(entity).runAsync(
                        this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                        this.thenDoOnFail(initiator));
                return voidFlowDone();
            }
            case BATCH : {
                this.ioEngine.report(initiator, "...executing " + entity.name());
                this.executeBatchInternally(initiator, asBatch(entity));
                this.asyncSaveCommandIfNecessary(initiator, command.mergeWith(entity));
                return voidFlowDone();
            }
            case UNDEFINED_ENTITY : {
                return voidFlowFail(
                        format("...type of '%s' is not defined.", entity.name()));
            }
            default : {
                return voidFlowFail(
                        format("...cannot do anything with %s '%s'", 
                                entity.type().displayName(), 
                                entity.name()));
            }
        }
    }

    @Override
    public void listLocation(Initiator initiator, ArgumentsCommand command) {
        this.checkExpectedTypeOf(initiator, command.type(), LIST_LOCATION);
        ValueFlow<Location> locationFlow = this.domain
                .locations()
                .findByNamePattern(initiator, command.joinedArguments());
        switch ( locationFlow.result() ) {
            case DONE : {
                if ( locationFlow.asDone().hasValue() ) {
                    Location location = locationFlow.asDone().orThrow();
                    Optional<List<String>> listing = this.fileLister.listContentOf(location, 5);
                    if ( listing.isPresent() && nonEmpty(listing.get()) ) {
                        listing.get().add(0, format("%s content: ", location.name()));
                        this.ioEngine.reportMessage(initiator, info(listing.get()));
                    } else {
                        this.ioEngine.report(
                                initiator, 
                                format("cannot list %s content.", location.name()));
                    }
                } else {
                    this.doWhenListedLocationNotFound(initiator, command.joinedArguments());                    
                }
                break; 
            }
            case FAIL : {
                this.ioEngine.report(initiator, locationFlow.asFail().reason());
                break; 
            }
            case STOP : {
                break; 
            }
            default : {
                this.ioEngine.report(initiator, "undefined operation result.");
                break; 
            }
        }
    }
    
    private void doWhenListedLocationNotFound(Initiator initiator, String pattern) {
        if ( directSubPathManipulationDisabled ) {
            this.ioEngine.report(initiator, format("cannot find '%s'", pattern));
            return;
        }
        
        ValueFlow<LocationSubPath> subPathFlow = this.domain
                .locationSubPaths()
                .findLocationSubPath(initiator, pattern);
        switch ( subPathFlow.result() ) {
            case DONE : {
                if ( subPathFlow.asDone().hasValue() ) {
                    this.listSubPath(initiator, subPathFlow.asDone().orThrow());
                } else {
                    this.ioEngine.report(initiator, format("cannot find '%s'", pattern));
                }
                break; 
            }
            case FAIL : {
                break; 
            }
            case STOP : {
                break; 
            }
            default : {
                break; 
            }
        }        
    }

    private void checkExpectedTypeOf(
            Initiator initiator, CommandType actual, CommandType expected) {
        if ( actual.isNot(expected) ) {
            this.ioEngine.report(initiator,
                    format("command type is %s but expected is %s", actual, expected));
        }
    }

    @Override
    public void listPath(Initiator initiator, ArgumentsCommand command) {
        this.checkExpectedTypeOf(initiator, command.type(), LIST_PATH);
        this.listPathString(initiator, command.joinedArguments());            
    }    

    private void listPathString(Initiator initiator, String path) {
        if ( containsPathSeparator(path) ) {
            this.proceedListPath(initiator, path);
        } else {
            this.ioEngine.report(
                    initiator, 
                    format("unknown case, '%s' is not path", path));
        }
    }    

    private void proceedListPath(Initiator initiator, String path) {
        String locationName = extractLocationFromPath(path);
        ValueFlow<Location> locationFlow = this.domain
                .locations()
                .findByNamePattern(initiator, locationName);
        switch ( locationFlow.result() ) {
            case DONE : {
                String target = extractTargetFromPath(path);
                if ( locationFlow.asDone().hasValue() ) {
                    this.listPathInLocation(
                            initiator, 
                            locationFlow.asDone().orThrow(), 
                            target);
                } else {
                    this.proceedListLocationSubPath(initiator, locationName, target);
                }
                break; 
            }
            case FAIL : {
                this.ioEngine.report(initiator, locationFlow.asFail().reason());
                break; 
            }
            case STOP : {
                break; 
            }
            default : {
                this.ioEngine.report(initiator, "undefined operation result.");
                break; 
            }
        }
    }    
    
    private void proceedListLocationSubPath(
            Initiator initiator, String subPathPattern, String target) {
        if ( directSubPathManipulationDisabled ) {
            this.ioEngine.report(initiator, format("cannot find '%s'", subPathPattern));
            return;
        }
        
        ValueFlow<LocationSubPath> subPathFlow = this.domain
                .locationSubPaths()
                .findLocationSubPath(initiator, subPathPattern);
        
        switch ( subPathFlow.result() ) {
            case DONE : {
                if ( subPathFlow.asDone().hasValue() ) {
                    LocationSubPath locationSubPath = subPathFlow.asDone().orThrow();
                    this.listPathInLocation(initiator, locationSubPath, target);
                } else {                    
                    this.ioEngine.report(initiator, format("cannot find '%s'", subPathPattern));
                }    
                break;
            }    
            case FAIL : {
                this.ioEngine.report(initiator, subPathFlow.asFail().reason());
                break;
            }    
            case STOP : {                
                break;
            }    
            default : {
                this.ioEngine.report(initiator, "undefined operation result.");
                break; 
            }    
        }
    }    
    
    private void listSubPath(Initiator initiator, LocationSubPath subPath) {
        Path realPath = subPath.realPath();
        if ( pathIsDirectory(realPath) ) {
            Optional<List<String>> listing = this.fileLister.listContentOf(realPath, 5);
            if ( listing.isPresent() ) {
                listing.get().add(0, subPath.name() + " content:");
                this.ioEngine.reportMessage(initiator, info(listing.get()));
            } else {
                this.ioEngine.report(initiator, format(
                        "cannot list %s content.", subPath.name()));
            }
        }
    }    
    
    private void listPathInLocation(Initiator initiator, Location location, String subpath) {
        if ( StringUtils.nonEmpty(subpath) ) {
            Path finalListingRoot = joinPathFrom(location.path(), subpath);
            if ( pathIsDirectory(finalListingRoot) ) {
                this.doListing(initiator, location, subpath);                    
            } else {
                this.listPathInLocationByFileWalker(initiator, location, subpath);
            }
        } else {
            this.ioEngine.report(initiator, "subpath is empty.");
        }
    }
    
    private void listPathInLocationByFileWalker(
            Initiator initiator, Location location, String subpath) {
        ValueFlow<String> subpathFlow = this.walker
                .walkToFind(subpath)
                .withMaxDepthOf(5)
                .in(location)
                .by(initiator)
                .andGetResult();
        
        switch ( subpathFlow.result() ) {
            case DONE : {
                ValueFlowDone<String> doneFlow = subpathFlow.asDone();
                if ( doneFlow.hasValue() ) {
                    subpath = doneFlow.orThrow();
                    this.doListing(initiator, location, subpath);
                } else {
                    if ( doneFlow.hasMessage() ) {
                        this.ioEngine.report(initiator, doneFlow.message());
                    } else {
                        this.ioEngine.report(initiator, format(
                                "%s/%s not found.", location.name(), subpath));
                    }                    
                }                
                break;
            }    
            case FAIL : {
                this.ioEngine.report(initiator, subpathFlow.asFail().reason());
                break;
            }    
            case STOP :
            default : {
                // do nothing;
            }
        }
    }
    
    private void doListing(Initiator initiator, Location location, String subpath) {
        Optional<List<String>> listing =
                this.fileLister.listContentOf(joinPathFrom(location.path(), subpath), 5);
        String listingPath = joinToPathFrom(location.name(), subpath);
        if ( listing.isPresent() ) {
            listing.get().add(0, listingPath + " content:");
            this.ioEngine.reportMessage(initiator, info(listing.get()));
        } else {
            this.ioEngine.report(
                    initiator, format("cannot list %s content.", listingPath));
        }
    }

    @Override
    public void executePlugin(Initiator initiator, PluginTaskCommand command) {
        Optional<Plugin> plugin = Optional.ofNullable(this.plugins.get(command.pluginName()));
        if ( plugin.isPresent() ) {
            plugin.get().process(initiator, command);
        } 
    }
    
    @Override
    public void browseWebPanel(Initiator initiator) {
        
    }
}
