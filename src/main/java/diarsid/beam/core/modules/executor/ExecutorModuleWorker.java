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

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.ValueFlowFail;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.HelpKey;
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
import diarsid.beam.core.base.os.treewalking.listing.FileLister;
import diarsid.beam.core.base.os.treewalking.search.FileSearcher;
import diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult;
import diarsid.beam.core.base.util.StringUtils;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchPauseCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightStrings;
import static diarsid.beam.core.base.control.flow.FlowResult.COMPLETE;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.FlowResult.STOP;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.linesToMessage;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.View.HIDE_VARIANT_TYPE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.SIMILAR_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMode.ALL;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMode.FOLDERS_ONLY;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.PathUtils.combineAsPathFrom;
import static diarsid.beam.core.base.util.PathUtils.combinePathFrom;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.base.util.PathUtils.extractTargetFromPath;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.domain.entities.Entities.asBatch;
import static diarsid.beam.core.domain.entities.Entities.asLocation;
import static diarsid.beam.core.domain.entities.Entities.asProgram;
import static diarsid.beam.core.domain.entities.Entities.asWebPage;
import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;
import static diarsid.beam.core.domain.entities.NamedEntityType.PROGRAM;
import static diarsid.beam.core.domain.entities.NamedEntityType.WEBPAGE;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorker implements ExecutorModule {
    
    static enum InvocationType {
        EXTERNAL {
            @Override
            boolean isExternal() {
                return true;
            }
        },
        INTERNAL {
            @Override
            boolean isExternal() {
                return false;
            }
        };
        
        abstract boolean isExternal();
    }
    
    private final DomainKeeperModule domain;
    private final InnerIoEngine ioEngine;
    private final FileSearcher fileSearcher;
    private final FileLister fileLister;    
    private final Map<String, Plugin> plugins;
    private final HelpKey chooseMultipleFoldersHelp;
    private final HelpKey chooseTargetToOpenHelp;

    ExecutorModuleWorker(
            InnerIoEngine ioEngine, 
            DomainKeeperModule domain,
            Set<Plugin> plugins,
            FileSearcher fileSearcher,
            FileLister fileLister) {
        this.ioEngine = ioEngine;
        this.fileSearcher = fileSearcher;
        this.domain = domain;
        this.fileLister = fileLister;
        this.plugins = new HashMap<>();
        plugins.forEach(plugin -> this.plugins.put(plugin.name(), plugin));     
        this.chooseMultipleFoldersHelp = this.ioEngine.addToHelpContext(
                "Choose folder to be listed from given variants.",
                "Use:", 
                "   - number of folder to choose it",
                "   - part of folder name to choose it",
                "   - 'n' or 'no' to see another folders, if any",
                "   - dot (.) to reject all variants"
        );
        this.chooseTargetToOpenHelp = this.ioEngine.addToHelpContext(
                "Choose target to be opened from given variants.",
                "Use:", 
                "   - number of target to choose it",
                "   - part of target name to choose it",
                "   - 'n' or 'no' to see another targets, if any",
                "   - dot (.) to reject all variants"
        );        
    }

    private void asyncSaveCommandIfNecessary(
            Initiator initiator, InvocationCommand command) {
        if ( command.wasNotUsedBefore() && command.isTargetFound() ) {
            asyncDo(() -> {
                this.domain.commandsMemory().save(initiator, command);
            });
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
                case FAIL:
                    return valueFlowFail(flow.message());
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
                case COMPLETE : {
                    if ( entityFlow.asComplete().hasValue() ) {
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
            case COMPLETE : {
                if ( entityFlow.asComplete().hasValue() ) {
                    command.argument().setExtended(entityFlow.asComplete().getOrThrow().name());        
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
                case COMPLETE : {
                    if ( entityFlow.asComplete().hasValue() ) {
                        command.setNew().setTargetFound();
                        return entityFlow;
                    } else {
                        this.domain.commandsMemory().remove(initiator, command);
                        command.argument().unextend();
                        command.setTargetNotFound();
                        return valueFlowCompletedEmpty();
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
            return valueFlowCompletedEmpty();
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
        debug("[EXECUTOR] [dispatch] " + command.stringifyOriginal() + ":" + command.stringify());
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
            logError(this.getClass(), e);
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
            case COMPLETE : {
                if ( entityFlow.asComplete().hasValue() ) {
                    NamedEntity entity = entityFlow.asComplete().getOrThrow();
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
        debug("[EXECUTOR] location not found by: " + command.originalArgument());
        debug("[EXECUTOR] try to proceed as OpenLocationTarget... ");
        ValueFlow<InvocationCommand> commandFlow = this.domain
                .commandsMemory()
                .findStoredCommandByPatternAndType(initiator, 
                        command.originalArgument(), 
                        OPEN_LOCATION_TARGET, 
                        HIDE_VARIANT_TYPE);
        switch ( commandFlow.result() ) {
            case COMPLETE : {
                if ( this.isOpenLocationTargetCommand(commandFlow) ) {
                    this.openLocationTarget(
                            initiator, 
                            (OpenLocationTargetCommand) commandFlow.asComplete().getOrThrow());
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
        return commandFlow.asComplete().hasValue() && 
                commandFlow.asComplete().getOrThrow().type().equals(OPEN_LOCATION_TARGET);
    }
    
    private void reportEntityNotFound(
            Initiator initiator, InvocationCommand command) {
        this.ioEngine.report(
                initiator,
                format("cannot find %s by name '%s'", 
                        command.subjectedEntityType().displayName(), 
                        command.argument().get()));
    }

    private void reportEntityNotFound(
            Initiator initiator, InvocationCommand command, ValueFlowFail valueFail) {
        this.ioEngine.report(
                initiator,
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
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    this.doWhenLocationFoundFromExtended(
                            initiator, command, valueFlow.asComplete().getOrThrow());
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
            this.openLocationTargetUsingExtendedArgument(command, initiator);
        } else {
            this.openLocationTargetUsingOriginalArgument(command, initiator);
        }
    }

    private void openLocationTargetUsingOriginalArgument(
            OpenLocationTargetCommand command, Initiator initiator) {
        ValueFlow<Location> locationFlow;        
        debug("[open trarget, NOT extended] " + command.stringifyOriginal());
        command.setNew();
        locationFlow = this.domain
                .locations()
                .findByNamePattern(initiator, command.originalLocation());
        switch ( locationFlow.result() ) {
            case COMPLETE : {
                if ( locationFlow.asComplete().hasValue() ) {
                    Location location = locationFlow.asComplete().getOrThrow();
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
        ValueFlow<LocationSubPath> subPathFlow = this.domain
                .locationSubPaths()
                .findLocationSubPath(initiator, command.originalLocation());
        
        switch ( subPathFlow.result() ) {
            case COMPLETE : {
                if ( subPathFlow.asComplete().hasValue() ) {
                    LocationSubPath subPath = subPathFlow.asComplete().getOrThrow();          
                    Location location = subPath.location();
                    String target = combineAsPathFrom(subPath.subPath(), command.originalTarget());
                    if ( location.has(target) ) {
                        this.openTargetAndExtendCommand(initiator, location, target, command);
                    } else {
                        this.doWhenTargetNotFoundDirectly(initiator, location, target, command);
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

    private void openLocationTargetUsingExtendedArgument(
            OpenLocationTargetCommand command, Initiator initiator) {
        ValueFlow<Location> valueFlow;
        debug("[open trarget, extended] " + command.stringifyOriginal() + " -> " + command.stringify());
        valueFlow = this.domain
                .locations()
                .findByExactName(initiator, command.extendedLocation());
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    this.doWhenLocationFoundFromExtended(
                            initiator, command, valueFlow.asComplete().getOrThrow());
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
        debug("[location found from extended] " + location.name());
        String target;
        target = command.extendedTarget();
        if ( location.has(target) ) {
            command.setTargetFound();
            this.ioEngine.report(initiator, format("...opening %s/%s", location.name(), target));
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
        FileSearchResult result = 
                this.fileSearcher.find(target, location.path(), SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                target = result.success().foundFile();
                this.openTargetAndExtendCommand(initiator, location, target, command);
            } else {
                WeightedVariants weightedStrings = 
                        weightStrings(target, result.success().foundFiles());
                if ( weightedStrings.isEmpty() ) {
                    this.ioEngine.report(
                            initiator, format("'%s' not found in %s", target, location.name()));
                    return;
                }
                Answer answer = this.ioEngine.chooseInWeightedVariants(
                        initiator, weightedStrings, this.chooseTargetToOpenHelp);
                if ( answer.isGiven() ) {
                    target = answer.text();
                    this.openTargetAndExtendCommand(initiator, location, target, command);
                } else if ( answer.variantsAreNotSatisfactory() ) { 
                    this.ioEngine.report(
                            initiator, format("'%s' not found in %s", target, location.name()));
                }
            }
        } else {
            this.ioEngine.report(
                    initiator, format("'%s' not found in %s", target, location.name()));
        }
    }

    private void openTargetAndExtendCommand(
            Initiator initiator, 
            Location location, 
            String target, 
            OpenLocationTargetCommand command) {
        command.argument().setExtended(location.name() + "/" + target);
        command.setTargetFound();
        this.ioEngine.report(initiator, "...opening " + command.extendedArgument());
        location.openAsync(
                target,
                this.thenDoOnSuccess(initiator, command),
                this.thenDoOnFail(initiator, command));
    }

    @Override
    public void runProgram(Initiator initiator, RunProgramCommand command) {
        ValueFlow<NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    NamedEntity entity = valueFlow.asComplete().getOrThrow();
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
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    NamedEntity entity = valueFlow.asComplete().getOrThrow();
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
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    NamedEntity entity = valueFlow.asComplete().getOrThrow();
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
            this.asyncSaveCommandIfNecessary(initiator, command);
        };
    }
    
    private CallbackEmpty thenDoOnSuccess(
            Initiator initiator, Optional<InvocationCommand> command) {
        return () -> {
            if ( command.isPresent() ) {
                this.asyncSaveCommandIfNecessary(initiator, command.get());                
            }
        };
    }

    @Override
    public void executeDefault(Initiator initiator, ExecutorDefaultCommand command) {
        debug("[EXECUTOR] [executeDefault] : " + command.argument());
        debug("[EXECUTOR] [executeDefault] find entity by exact name: " + command.argument());
        ValueFlow<NamedEntity> entityFlow = this.domain
                .allEntities()
                .findByExactName(initiator, command.argument());
        switch ( entityFlow.result() ) {
            case COMPLETE : {
                if ( entityFlow.asComplete().hasValue() ) {
                    debug("[EXECUTOR] [executeDefault] entity found: " + entityFlow.asComplete().getOrThrow().toString());
                    VoidFlow invokeFLow = this.invokeFoundEntity(
                            initiator, entityFlow.asComplete().getOrThrow(), command);
                    switch ( invokeFLow.result() ) {
                        case COMPLETE : {
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
            case COMPLETE : {
                if ( flow.asComplete().hasValue() ) {
                    debug("[EXECUTOR] [executeDefault] execute saved command: " + flow.asComplete().getOrThrow().originalArgument() + ":" + flow.asComplete().getOrThrow().extendedArgument());
                    this.dispatchCommandInternally(initiator, flow.asComplete().getOrThrow());
                } else {
                    debug("[EXECUTOR] [executeDefault] stored command not found by: " + command.argument());
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
        debug("[EXECUTOR] [executeDefault] [find and invoke any entity by] : " + command.argument());
        ValueFlow<NamedEntity> entityFlow = 
                this.findNamedEntityByArgument(initiator, command.argument());
        switch ( entityFlow.result() ) {
            case COMPLETE : {
                if ( entityFlow.asComplete().hasValue() ) {
                    VoidFlow invokeFlow = this.invokeFoundEntity(
                            initiator, entityFlow.asComplete().getOrThrow(), command);
                    switch ( invokeFlow.result() ) {
                        case COMPLETE : 
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
        ValueFlow<NamedEntity> valueFlow = this.domain
                .allEntities()
                .findByExactName(initiator, argument);
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                     return valueFlow;
                } else {
                    debug("[EXECUTOR] [executeDefault] [find by argument] not found by exact " + argument);
                    return this.domain
                            .allEntities()
                            .findByNamePattern(initiator, argument);
                }
            }
            case FAIL : {
                debug("[EXECUTOR] [executeDefault] [find by argument] not found by exact " + argument);
                return this.domain
                        .allEntities()
                        .findByNamePattern(initiator, argument);
            }
            case STOP : {
                return valueFlow;
            }
            default : {
                return valueFlowFail("unknown ValueFlow result.");
            }
        }
    }
    
    private VoidFlow invokeFoundEntity(
            Initiator initiator, NamedEntity entity, ExecutorDefaultCommand command) {
        if ( entity.type().isNotDefined() ) {
            debug("[EXECUTOR] [executeDefault] [find any entity by] not found any : " + command.argument());
            return voidFlowFail(format("...type of '%s' is not defined.", entity.name()));
        }        
        
        invocation: switch ( entity.type() ) {
            case LOCATION : {
                this.ioEngine.report(initiator, "...opening " + asLocation(entity).name());
                asLocation(entity).openAsync(
                        this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                        this.thenDoOnFail(initiator));
                return voidFlowCompleted();
            }
            case WEBPAGE : {
                this.ioEngine.report(initiator, "...browsing " + asWebPage(entity).name());
                asWebPage(entity).browseAsync(
                        this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                        this.thenDoOnFail(initiator));
                return voidFlowCompleted();
            }
            case PROGRAM : {
                this.ioEngine.report(initiator, "...running " + asProgram(entity).simpleName());
                asProgram(entity).runAsync(
                        this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                        this.thenDoOnFail(initiator));
                return voidFlowCompleted();
            }
            case BATCH : {
                this.ioEngine.report(initiator, "...executing " + entity.name());
                this.executeBatchInternally(initiator, asBatch(entity));
                this.asyncSaveCommandIfNecessary(initiator, command.mergeWith(entity));
                return voidFlowCompleted();
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
            case COMPLETE : {
                if ( locationFlow.asComplete().hasValue() ) {
                    Location location = locationFlow.asComplete().getOrThrow();
                    Optional<List<String>> listing = this.fileLister.listContentOf(location, 5);
                    if ( listing.isPresent() && nonEmpty(listing.get()) ) {
                        listing.get().add(0, format("'%s' content: ", location.name()));
                        this.ioEngine.reportMessage(initiator, linesToMessage(listing.get()));
                    } else {
                        this.ioEngine.report(
                                initiator, 
                                format("cannot list '%s' content.", location.name()));
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
        ValueFlow<InvocationCommand> commandFlow = this.domain
                .commandsMemory()
                .findStoredCommandByPatternAndType(initiator, pattern, OPEN_LOCATION_TARGET, HIDE_VARIANT_TYPE);
        switch ( commandFlow.result() ) {
            case COMPLETE : {
                if ( commandFlow.asComplete().hasValue() ) {
                    this.listPathString(
                            initiator, commandFlow.asComplete().getOrThrow().extendedArgument());
                } else {
                    this.ioEngine.report(
                        initiator, format("cannot find '%s'", pattern));
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
            case COMPLETE : {
                String target = extractTargetFromPath(path);
                if ( locationFlow.asComplete().hasValue() ) {
                    this.listPathInLocation(
                            initiator, 
                            locationFlow.asComplete().getOrThrow(), 
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
        ValueFlow<LocationSubPath> subPathFlow = this.domain
                .locationSubPaths()
                .findLocationSubPath(initiator, subPathPattern);
        
        switch ( subPathFlow.result() ) {
            case COMPLETE : {
                if ( subPathFlow.asComplete().hasValue() ) {
                    LocationSubPath subPath = subPathFlow.asComplete().getOrThrow();
                    this.listPathInLocation(
                            initiator, 
                            subPath.location(), 
                            combineAsPathFrom(subPath.subPath(), target));
                } else {                    
                    this.ioEngine.report(
                            initiator, format("cannot find '%s'", subPathPattern));
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
        
    
    private void listPathInLocation(Initiator initiator, Location location, String subpath) {
        if ( StringUtils.nonEmpty(subpath) ) {
            Path finalListingRoot = combinePathFrom(location.path(), subpath);
            if ( pathIsDirectory(finalListingRoot) ) {
                this.doListing(initiator, location.path(), subpath);                    
            } else {
                FileSearchResult result = this.fileSearcher.find(
                        subpath, location.path(), SIMILAR_MATCH, FOLDERS_ONLY);
                if ( result.isOk() ) {
                    if ( result.success().hasSingleFoundFile() ) {
                        this.doListing(initiator, location.path(), result.success().foundFile());
                    } else {
                        this.resolveMultipleFoldersAndDoListing(
                                initiator, location, subpath, result.success().foundFiles());
                    }
                } else {
                    this.ioEngine.report(
                            initiator, format("%s/%s nof found.", location.path(), subpath));
                }
            }
        } else {
            this.ioEngine.report(initiator, "subpath is empty.");
        }
    }
    
    private void resolveMultipleFoldersAndDoListing(
            Initiator initiator, Location location, String subpathPattern, List<String> folders) {
        WeightedVariants variants = weightStrings(subpathPattern, folders);
        if ( variants.isEmpty() ) {
            this.ioEngine.report(initiator, format(
                    "%s/%s nof found.", location.path(), subpathPattern));
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseMultipleFoldersHelp);
        if ( answer.isGiven() ) {
            this.doListing(initiator, location.path(), answer.text());
        }
    }

    private void doListing(Initiator initiator, String location, String subpath) {
        Optional<List<String>> listing =
                this.fileLister.listContentOf(combinePathFrom(location, subpath), 5);
        if ( listing.isPresent() ) {
            this.ioEngine.reportMessage(initiator, linesToMessage(listing.get()));
        } else {
            this.ioEngine.report(
                    initiator, format("cannot list '%s/%s' content.", location, subpath));
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
