/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;


import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.ValueOperationFail;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.os.listing.FileLister;
import diarsid.beam.core.base.os.search.FileSearcher;
import diarsid.beam.core.base.os.search.result.FileSearchResult;
import diarsid.beam.core.base.util.StringUtils;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchPauseCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

import static diarsid.beam.core.base.control.flow.OperationResult.COMPLETE;
import static diarsid.beam.core.base.control.flow.OperationResult.STOP;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.textToMessage;
import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.os.search.FileSearchMatching.SIMILAR_MATCH;
import static diarsid.beam.core.base.os.search.FileSearchMode.ALL;
import static diarsid.beam.core.base.os.search.FileSearchMode.FOLDERS_ONLY;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
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
import static diarsid.beam.core.domain.patternsanalyze.Analyze.analyzeStrings;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorker implements ExecutorModule {
    
    private final DomainKeeperModule domain;
    private final InnerIoEngine ioEngine;
    private final FileSearcher fileSearcher;
    private final FileLister fileLister;

    ExecutorModuleWorker(
            InnerIoEngine ioEngine, 
            DomainKeeperModule domain,
            FileSearcher fileSearcher,
            FileLister fileLister) {
        this.ioEngine = ioEngine;
        this.fileSearcher = fileSearcher;
        this.domain = domain;
        this.fileLister = fileLister;
    }

    private void saveCommandIfNecessary(
            Initiator initiator, InvocationCommand command) {
        if ( command.wasNotUsedBefore() && command.isTargetFound() ) {
            this.domain.commandsMemory().save(initiator, command);
        }
    }
    
    private void saveCommandIfNecessary(
            Initiator initiator, Optional<InvocationCommand> optCommand) {
        if ( optCommand.isPresent() ) {
            InvocationCommand command = optCommand.get();
            if ( command.wasNotUsedBefore() && command.isTargetFound() ) {
                this.domain.commandsMemory().save(initiator, command);
            }            
        }        
    }

    private void deleteCommandIfNecessary(
            Initiator initiator, InvocationCommand command) {
        if ( command.wasUsedBeforeAndStored() && command.isTargetNotFound() ) {
            this.domain.commandsMemory().remove(initiator, command);
        }
    }
    
    private ValueOperation<? extends NamedEntity> findNamedEntity(
            Initiator initiator, InvocationCommand command) {
        if ( command.argument().isNotExtended() ) {
            this.domain.commandsMemory().tryToExtendCommand(initiator, command); 
        }
        if ( command.argument().isExtended() ) {
            ValueOperation<? extends NamedEntity> valueFlow = this.domain
                    .entitiesOperatedBy(command)
                    .findByExactName(initiator, command.argument().extended());
            switch ( valueFlow.result() ) {
                case COMPLETE : {
                    if ( valueFlow.asComplete().hasValue() ) {
                        command.setTargetFound();
                        return valueFlow;
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
                    return valueFlow;
                }
                default : {
                    return valueOperationFail("Unknown ValueOperation result.");
                }
            }
        } else {
            return this.findNamedEntityByNamePattern(initiator, command);
        }
    }

    private ValueOperation<? extends NamedEntity> findNamedEntityByNamePattern(
            Initiator initiator, InvocationCommand command) {
        command.setNew();
        ValueOperation<? extends NamedEntity> valueFlow = this.domain
                .entitiesOperatedBy(command)
                .findByNamePattern(initiator, command.argument().original());
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    command.argument().setExtended(valueFlow.asComplete().getOrThrow().name());        
                    command.setTargetFound();
                    return valueFlow;
                } else {
                    return this.tryToFindEntityInExtendedCommands(initiator, command);
                }
            }
            case FAIL : {
                return this.tryToFindEntityInExtendedCommands(initiator, command);
            }
            case STOP : {
                return valueFlow;
            }
            default : {
                return valueOperationFail("Unknown ValueOperation result.");
            }            
        }
    }

    private ValueOperation<? extends NamedEntity> tryToFindEntityInExtendedCommands(
            Initiator initiator, InvocationCommand command) {
        this.domain.commandsMemory().tryToExtendCommandByPattern(initiator, command);
        if ( command.argument().isExtended() ) {
            ValueOperation<? extends NamedEntity> valueFlow = this.domain
                    .entitiesOperatedBy(command)
                    .findByExactName(initiator, command.argument().extended());
            switch ( valueFlow.result() ) {
                case COMPLETE : {
                    if ( valueFlow.asComplete().hasValue() ) {
                        command.setNew().setTargetFound();
                        return valueFlow;
                    } else {
                        this.domain.commandsMemory().remove(initiator, command);
                        command.argument().unextend();
                        command.setTargetNotFound();
                        return valueOperationFail("Not found.");
                    }
                }
                case FAIL : {
                    this.domain.commandsMemory().remove(initiator, command);
                    command.argument().unextend();
                    command.setTargetNotFound();
                    return valueFlow;
                }
                case STOP : {
                    return valueFlow;
                }
                default : {
                    return valueOperationFail("Unknown ValueOperation result.");
                }
            }
        } else {
            command.setTargetNotFound();
            return valueOperationFail("Not found in extended.");
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
                break dispatching;
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
        ValueOperation<? extends NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    NamedEntity entity = valueFlow.asComplete().getOrThrow();
                    if ( entity.is(LOCATION) ) {
                        this.ioEngine.report(initiator, "...opening " + asLocation(entity).name());
                        asLocation(entity).openAsync(
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
    
    private void reportEntityNotFound(
            Initiator initiator, InvocationCommand command) {
        this.ioEngine.report(
                initiator,
                format("cannot find %s by name '%s'", 
                        command.subjectedEntityType().displayName(), 
                        command.argument().get()));
    }

    private void reportEntityNotFound(
            Initiator initiator, InvocationCommand command, ValueOperationFail valueFail) {
        this.ioEngine.report(
                initiator,
                format("cannot find %s by name '%s': %s", 
                        command.subjectedEntityType().displayName(), 
                        command.argument().get(), 
                        valueFail.reason()));
    }
    
    private void tryToFindLocationByPattern(
            Initiator initiator, OpenLocationTargetCommand command) {
        ValueOperation<Location> valueFlow = this.domain
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
        this.domain.commandsMemory().tryToExtendCommand(initiator, command);
        if ( command.argument().isExtended() ) {
            this.openTargetUsingExtendedArgument(command, initiator);
        } else {
            this.openTargetUsingOriginalArgument(command, initiator);
        }
    }

    private void openTargetUsingOriginalArgument(OpenLocationTargetCommand command, Initiator initiator) {
        ValueOperation<Location> valueFlow;
        Location location;
        String target;
        debug("[open trarget, NOT extended] " + command.stringifyOriginal());
        command.setNew();
        valueFlow = this.domain
                .locations()
                .findByNamePattern(initiator, command.originalLocation());
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    location = valueFlow.asComplete().getOrThrow();
                    target = command.originalTarget();
                    if ( location.has(target) ) {
                        this.openTargetAndExtendCommand(initiator, location, target, command);
                    } else {
                        this.doWhenTargetNotFoundDirectly(initiator, location, command);
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

    private void openTargetUsingExtendedArgument(
            OpenLocationTargetCommand command, Initiator initiator) {
        ValueOperation<Location> valueFlow;
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
        String target;
        command.setNew();
        target = command.originalTarget();
        FileSearchResult result = 
                this.fileSearcher.find(target, location.path(), SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                target = result.success().foundFile();
                this.openTargetAndExtendCommand(initiator, location, target, command);
            } else {
                Answer answer = this.ioEngine.chooseInWeightedVariants(
                        initiator, analyzeStrings(target, result.success().foundFiles()));
                if ( answer.isGiven() ) {
                    target = answer.text();
                    this.openTargetAndExtendCommand(initiator, location, target, command);
                } else {
                    // do nothing.
                }
            }
        } else {
            this.ioEngine.report(
                    initiator, format("'%s' not found in %s", target, location.name()));
        }
    }

    public void openTargetAndExtendCommand(
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
                this.thenDoOnFail(initiator));
    }

    @Override
    public void runProgram(Initiator initiator, RunProgramCommand command) {
        ValueOperation<? extends NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
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
            Initiator initiator, InvocationCommand command, ValueOperationFail valueFlow) {
        this.reportEntityNotFound(initiator, command, valueFlow);
        this.deleteCommandIfNecessary(initiator, command);
    }

    @Override
    public void callBatch(Initiator initiator, CallBatchCommand command) {
        ValueOperation<? extends NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    NamedEntity entity = valueFlow.asComplete().getOrThrow();
                    if ( entity.is(BATCH) ) {
                        this.ioEngine.report(initiator, "...executing " + asBatch(entity).name());
                        this.executeBatchInternally(initiator, asBatch(entity));
                        this.saveCommandIfNecessary(initiator, command);
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
        ValueOperation<? extends NamedEntity> valueFlow = this.findNamedEntity(initiator, command);
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
            this.saveCommandIfNecessary(initiator, command);
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
        debug("[EXECUTOR] [executeDefault] " + command.argument());
        ValueOperation<InvocationCommand> flow = this.domain
                .commandsMemory()
                .findStoredCommandByExactOriginalOfAnyType(initiator, command.argument());
        switch ( flow.result() ) {
            case COMPLETE : {
                if ( flow.asComplete().hasValue() ) {
                    debug("[EXECUTOR] [executeDefault] [execute saved] " + flow.asComplete().getOrThrow().stringify());
                    this.dispatchCommandInternally(initiator, flow.asComplete().getOrThrow());
                } else {
                    this.proceedDefaultExecution(initiator, command);
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

    private void proceedDefaultExecution(Initiator initiator, ExecutorDefaultCommand command) {
        VoidOperation operation = this.findAndInvokeAnyNamedEntity(initiator, command);
        switch ( operation.result() ) {
            case COMPLETE : {
                debug("[EXECUTOR] [executeDefault] completed." );
                // entity was found and invoked normally.
                return;
            }
            case STOP : {
                // operation has been stopped by user, just retun.
                return;
            }
            case FAIL : {
                debug("[EXECUTOR] [executeDefault] entity not found by: " + command.argument());
                // entity was not found, need to proceed.
                ValueOperation<InvocationCommand> commandFlow = this.domain
                        .commandsMemory()
                        .findStoredCommandByPatternOfAnyType(initiator, command.argument());
                switch ( commandFlow.result() ) {
                    case COMPLETE : {
                        if ( commandFlow.asComplete().hasValue() ) {                            
                            Optional<InvocationCommand> invocation = 
                                    command.mergeWith(commandFlow.asComplete().getOrThrow());
                            if ( invocation.isPresent() ) {
                                debug("[EXECUTOR] [executeDefault] [merging command] " + invocation.get().stringifyOriginal() + ":" + invocation.get().stringify());
                                this.dispatchCommandInternally(initiator, invocation.get());
                            } else {
                                this.ioEngine.report(
                                        initiator, format(
                                                "cannot merge %s command.", 
                                                invocation.get().type().name()));
                            }
                        } else {
                            this.ioEngine.report(initiator, "not found.");
                        }
                        return;
                    }
                    case FAIL : {
                        this.ioEngine.report(initiator, commandFlow.asFail().reason());
                        return;
                    }
                    case STOP : {
                        return; 
                    }
                    default : {
                        this.ioEngine.report(initiator, "unknown ValueOperation result.");
                        return; 
                    }
                }    
            }
            default : {
                this.ioEngine.report(initiator, "unknown ValueOperation result.");    
            }
        }       
    }
    
    private ValueOperation<? extends NamedEntity> findNamedEntityByArgument(
            Initiator initiator, String argument) {
        ValueOperation<? extends NamedEntity> valueFlow = 
                this.domain.allEntities().findByExactName(initiator, argument);
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                     return valueFlow;
                } else {
                    debug("[EXECUTOR] [executeDefault] [find by argument] not found by exact " + argument);
                    return this.domain.allEntities().findByNamePattern(initiator, argument);
                }
            }
            case FAIL : {
                debug("[EXECUTOR] [executeDefault] [find by argument] not found by exact " + argument);
                return this.domain.allEntities().findByNamePattern(initiator, argument);
            }
            case STOP : {
                return valueFlow;
            }
            default : {
                return valueOperationFail("unknown ValueOperation result.");
            }
        }
    }
 
    private VoidOperation findAndInvokeAnyNamedEntity(
            Initiator initiator, ExecutorDefaultCommand command) {
        debug("[EXECUTOR] [executeDefault] [find any entity by] : " + command.argument());
        ValueOperation<? extends NamedEntity> valueFlow = 
                this.findNamedEntityByArgument(initiator, command.argument());
        switch ( valueFlow.result() ) {
            case COMPLETE : {
                if ( valueFlow.asComplete().hasValue() ) {
                    return this.invokeFoundEntity(
                            initiator, valueFlow.asComplete().getOrThrow(), command);
                } else {
                    return voidOperationFail("Not found");
                }
            } 
            case FAIL : {
                return voidOperationFail(valueFlow.asFail().reason());
            }
            case STOP : {
                return voidOperationStopped();
            }
            default : {
                return voidOperationFail("Unkown ValueOperation type.");
            }
        }
    }
    
    private VoidOperation invokeFoundEntity(
            Initiator initiator, NamedEntity entity, ExecutorDefaultCommand command) {
        if ( entity.type().isDefined() ) {
            invocation: switch ( entity.type() ) {
                case LOCATION : {
                    this.ioEngine.report(initiator, "...opening " + asLocation(entity).name());
                    asLocation(entity).openAsync(
                            this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                            this.thenDoOnFail(initiator));
                    return voidCompleted();
                }
                case WEBPAGE : {
                    this.ioEngine.report(initiator, "...browsing " + asWebPage(entity).name());
                    asWebPage(entity).browseAsync(
                            this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                            this.thenDoOnFail(initiator));
                    return voidCompleted();
                }
                case PROGRAM : {
                    this.ioEngine.report(initiator, "...running " + asProgram(entity).simpleName());
                    asProgram(entity).runAsync(
                            this.thenDoOnSuccess(initiator, command.mergeWith(entity)), 
                            this.thenDoOnFail(initiator));
                    return voidCompleted();
                }
                case BATCH : {
                    this.ioEngine.report(initiator, "...executing " + entity.name());
                    this.executeBatchInternally(initiator, asBatch(entity));
                    this.saveCommandIfNecessary(initiator, command.mergeWith(entity));
                    return voidCompleted();
                }
                case UNDEFINED_ENTITY : {
                    return voidOperationFail(
                            format("...type of '%s' is not defined.", entity.name()));
                }
                default : {
                    return voidOperationFail(
                            format("...cannot do anything with %s '%s'", 
                                    entity.type().displayName(), 
                                    entity.name()));
                }
            }
        } else {
            debug("[EXECUTOR] [executeDefault] [find any entity by] not found any : " + command.argument());
            return voidOperationFail(format("...type of '%s' is not defined.", entity.name()));
        }
    }

    @Override
    public void listLocation(Initiator initiator, ArgumentsCommand command) {
        ValueOperation<Location> locationFlow = this.domain
                .locations()
                .findByNamePattern(initiator, command.joinedArguments());
        switch ( locationFlow.result() ) {
            case COMPLETE : {
                if ( locationFlow.asComplete().hasValue() ) {
                    Location location = locationFlow.asComplete().getOrThrow();
                    Optional<List<String>> listing = this.fileLister.listContentOf(location, 5);
                    if ( listing.isPresent() && nonEmpty(listing.get()) ) {
                        this.ioEngine.reportMessage(initiator, textToMessage(listing.get()));
                    } else {
                        this.ioEngine.report(
                                initiator, 
                                format("cannot list '%s' content.", location.name()));
                    }
                } else {
                    this.ioEngine.report(
                        initiator, format("cannot find '%s' Location", command.joinedArguments()));
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

    @Override
    public void listPath(Initiator initiator, ArgumentsCommand command) {
        String path = command.joinedArguments();
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
        ValueOperation<Location> locationFlow = this.domain
                .locations()
                .findByNamePattern(initiator, locationName);
        switch ( locationFlow.result() ) {
            case COMPLETE : {
                if ( locationFlow.asComplete().hasValue() ) {
                    this.listPathInLocation(
                            initiator, 
                            locationFlow.asComplete().getOrThrow(), 
                            extractTargetFromPath(path));
                } else {
                    this.ioEngine.report(
                            initiator, format("cannot find '%s' Location", locationName));
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
        WeightedVariants variants = analyzeStrings(subpathPattern, folders);
        Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
        if ( answer.isGiven() ) {
            this.doListing(initiator, location.path(), answer.text());
        }
    }

    private void doListing(Initiator initiator, String location, String subpath) {
        Optional<List<String>> listing =
                this.fileLister.listContentOf(combinePathFrom(location, subpath), 5);
        if ( listing.isPresent() ) {
            this.ioEngine.reportMessage(initiator, textToMessage(listing.get()));
        } else {
            this.ioEngine.report(
                    initiator, format("cannot list '%s/%s' content.", location, subpath));
        }
    }
}
