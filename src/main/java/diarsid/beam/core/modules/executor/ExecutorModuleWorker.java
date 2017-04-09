/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;


import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EntityInvocationCommand;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.SeePageCommand;
import diarsid.beam.core.base.os.listing.FileLister;
import diarsid.beam.core.base.os.search.FileSearcher;
import diarsid.beam.core.base.util.StringUtils;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchPauseCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

import static diarsid.beam.core.base.control.flow.OperationResult.OK;
import static diarsid.beam.core.base.control.flow.Operations.asFail;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.textToMessage;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
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
            Initiator initiator, EntityInvocationCommand command) {
        if ( command.wasNotUsedBefore() && command.isTargetFound() ) {
            this.domain.commandsMemory().save(initiator, command);
        }
    }
    
    private void saveCommandIfNecessary(
            Initiator initiator, Optional<EntityInvocationCommand> command) {
        if ( command.isPresent() && 
                command.get().wasNotUsedBefore() && 
                command.get().isTargetFound() ) {
            this.domain.commandsMemory().save(initiator, command.get());
        }
    }

    private void deleteCommandIfNecessary(
            Initiator initiator, EntityInvocationCommand command) {
        if ( command.wasUsedBeforeAndStored() && command.isTargetNotFound() ) {
            this.domain.commandsMemory().remove(initiator, command);
        }
    }
    
    private Optional<? extends NamedEntity> findNamedEntity(
            Initiator initiator, EntityInvocationCommand command) {
        this.domain.commandsMemory().tryToExtendCommand(initiator, command);
        if ( command.argument().hasExtended() ) {
            Optional<? extends NamedEntity> entity = this.domain
                    .entitiesOperatedBy(command)
                    .findByExactName(initiator, command.argument().extended());
            if ( entity.isPresent() ) {
                command.setTargetFound();
                return entity;
            } else {
                return this.findNamedEntityByNamePattern(initiator, command);
            }
        } else {
            return this.findNamedEntityByNamePattern(initiator, command);
        }
    }

    private Optional<? extends NamedEntity> findNamedEntityByNamePattern(
            Initiator initiator, EntityInvocationCommand command) {
        command.setNew();
        Optional<? extends NamedEntity> entity = this.domain
                .entitiesOperatedBy(command)
                .findByNamePattern(initiator, command.argument().original());
        if ( entity.isPresent() ) {
            command.argument().setExtended(entity.get().name());        
            command.setTargetFound();
        } else {
            command.setTargetNotFound();
        }
        return entity;
    }
    
    private void executeBatchInternally(Initiator initiator, Batch batch) {
        batch.batchedCommands()
                .stream()
                .map(batchedCommand -> batchedCommand.unwrap())
                .filter(command -> command.type().isNot(CALL_BATCH))
                .forEach(command -> this.dispatchCommandInternally(initiator, command));         
    }
    
    private void dispatchCommandInternally(Initiator initiator, ExtendableCommand command) {
        dispatching: switch ( command.type() ) {
            case EXECUTOR_DEFAULT : {
                this.executeDefault(initiator, (ExecutorDefaultCommand) command);
                break dispatching;
            }
            case OPEN_LOCATION : {
                this.openLocation(initiator, (OpenLocationCommand) command);
                break dispatching;
            }
            case OPEN_LOCATION_TARGET : {
                this.openLocationTarget(initiator, (OpenLocationTargetCommand) command);
                break dispatching;
            }
            case SEE_WEBPAGE : {
                this.browsePage(initiator, (SeePageCommand) command);
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
                    sleep( 1000 * pause.duration() );
                    break suspending;                
                }
                case MINUTES : {
                    sleep( 1000 * 60 * pause.duration() );
                    break suspending;                
                }
                case HOURS : {
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
        Optional<? extends NamedEntity> entity = this.findNamedEntity(initiator, command);
        if ( entity.isPresent() && entity.get().is(LOCATION) ) {
            asLocation(entity).openAsync(
                    this.doOnSuccess(initiator, command), 
                    this.doOnFail(initiator, command));
        } else {
            this.doWhenNotFound(initiator, command);
        }
    }

    private void reportEntityNotFound(
            Initiator initiator, EntityInvocationCommand command) {
        this.ioEngine.report(
                initiator,
                format("cannot find %s by name '%s'", 
                        command.subjectedEntityType().displayName(), 
                        command.argument().get()));
    }

    @Override
    public void openLocationTarget(Initiator initiator, OpenLocationTargetCommand command) {
        
    }

    @Override
    public void runProgram(Initiator initiator, RunProgramCommand command) {
        Optional<? extends NamedEntity> entity = this.findNamedEntity(initiator, command);
        if ( entity.isPresent() && entity.get().is(PROGRAM) ) {
            asProgram(entity).runAsync(
                    this.doOnSuccess(initiator, command), 
                    this.doOnFail(initiator, command));
        } else {
            this.doWhenNotFound(initiator, command);
        }
    }

    private void doWhenNotFound(Initiator initiator, EntityInvocationCommand command) {
        this.reportEntityNotFound(initiator, command);
        this.deleteCommandIfNecessary(initiator, command);
    }

    @Override
    public void callBatch(Initiator initiator, CallBatchCommand command) {
        Optional<? extends NamedEntity> entity = this.findNamedEntity(initiator, command);
        if ( entity.isPresent() && entity.get().is(BATCH) ) {
            this.ioEngine.report(initiator, "...executing " + entity.get().name());
            this.executeBatchInternally(initiator, asBatch(entity));
            this.saveCommandIfNecessary(initiator, command);   
        } else {
            this.doWhenNotFound(initiator, command);
        }
    }

    @Override
    public void browsePage(Initiator initiator, SeePageCommand command) {
        Optional<? extends NamedEntity> entity = this.findNamedEntity(initiator, command);
        if ( entity.isPresent() && entity.get().is(WEBPAGE) ) {
            asWebPage(entity).browseAsync(
                    this.doOnSuccess(initiator, command), 
                    this.doOnFail(initiator, command));
        } else {
            this.doWhenNotFound(initiator, command);
        }
    }

    private CallbackEvent doOnFail(Initiator initiator, EntityInvocationCommand command) {
        return (fail) -> {
            this.ioEngine.report(initiator, fail);
            this.deleteCommandIfNecessary(initiator, command);
        };
    }
    
    private CallbackEvent doOnFail(Initiator initiator) {
        return (fail) -> {
            this.ioEngine.report(initiator, fail);
        };
    }

    private CallbackEvent doOnSuccess(Initiator initiator, EntityInvocationCommand command) {
        return (sucess) -> {
            this.ioEngine.report(initiator, sucess);
            this.saveCommandIfNecessary(initiator, command);
        };
    }
    
    private CallbackEvent doOnSuccess(
            Initiator initiator, Optional<EntityInvocationCommand> command) {
        return (sucess) -> {
            this.ioEngine.report(initiator, sucess);
            if ( command.isPresent() ) {
                this.saveCommandIfNecessary(initiator, command.get());                
            }
        };
    }

    @Override
    public void executeDefault(Initiator initiator, ExecutorDefaultCommand command) {
        Optional<ExtendableCommand> savedCommand = this.domain
                .commandsMemory()
                .findStoredCommandByExactOriginalOfAnyType(initiator, command.originalArgument());
        if ( savedCommand.isPresent() && savedCommand.get().type().isNot(EXECUTOR_DEFAULT) ) {
            this.dispatchCommandInternally(initiator, savedCommand.get());
        } else {
            VoidOperation operation = this.findAndInvokeAnyNamedEntity(initiator, command);
            switch ( operation.result() ) {
                case OK : {
                    // entity was found and invoked normally.
                    return;
                }    
                case STOP : {
                    // entity was not found, need to proceed.
                    Optional<ExtendableCommand> mergedCommand = 
                            this.findSimilarCommandByPatternAndMergeWithOriginal(initiator, command);
                    if ( mergedCommand.isPresent() ) {
                        this.dispatchCommandInternally(initiator, mergedCommand.get());
                    }
                    return;
                }    
                case FAIL : {
                    // entity was found, but could not be processed due to some reason
                    this.ioEngine.report(initiator, asFail(operation).reason());
                    this.deleteCommandIfNecessary(initiator, command);
                    return;
                }    
                default : {
                    this.ioEngine.report(initiator, "...unknown operation result.");
                }    
            }
        }        
    }

    private VoidOperation findAndInvokeAnyNamedEntity(
            Initiator initiator, ExecutorDefaultCommand command) {
        Optional<? extends NamedEntity> entity = this.findNamedEntity(initiator, command);
        if ( entity.isPresent() && entity.get().type().isDefined() ) {
            invocation: switch ( entity.get().type() ) {
                case LOCATION : {
                    asLocation(entity).openAsync(
                            this.doOnSuccess(initiator, command.mergeWithEntity(entity)), 
                            this.doOnFail(initiator));
                    return voidCompleted();
                }
                case WEBPAGE : {
                    asWebPage(entity).browseAsync(
                            this.doOnSuccess(initiator, command.mergeWithEntity(entity)), 
                            this.doOnFail(initiator));
                    return voidCompleted();
                }
                case PROGRAM : {
                    asProgram(entity).runAsync(
                            this.doOnSuccess(initiator, command.mergeWithEntity(entity)), 
                            this.doOnFail(initiator));
                    return voidCompleted();
                }
                case BATCH : {
                    this.ioEngine.report(initiator, "...executing " + entity.get().name());
                    this.executeBatchInternally(initiator, asBatch(entity));
                    this.saveCommandIfNecessary(initiator, command.mergeWithEntity(entity));
                    return voidCompleted();
                }
                case UNDEFINED_ENTITY : {
                    return voidOperationFail(
                            format("...type of '%s' is not defined.", entity.get().name()));
                }
                default : {
                    return voidOperationFail(
                            format("...cannot do anything with %s '%s'", 
                                    entity.get().type().displayName(), 
                                    entity.get().name()));
                }
            }
        } else {
            return voidOperationStopped();
        }
    }
    
    private Optional<ExtendableCommand> findSimilarCommandByPatternAndMergeWithOriginal(
            Initiator initiator, ExecutorDefaultCommand command) {
        Optional<ExtendableCommand> storedCommand = this.domain
                .commandsMemory()
                .findStoredCommandByPatternOfAnyType(initiator, command.originalArgument());
        if ( storedCommand.isPresent() && storedCommand.get().type().isNot(EXECUTOR_DEFAULT) ) {
            return command.mergeWithCommand(storedCommand);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void listLocation(Initiator initiator, ArgumentsCommand command) {
        Optional<Location> location = this.domain
                .locations()
                .findByNamePattern(initiator, command.joinedArguments());
        if ( location.isPresent() ) {
            Optional<List<String>> listing = this.fileLister.listContentOf(location.get(), 5);
            if ( listing.isPresent() && nonEmpty(listing.get()) ) {
                this.ioEngine.reportMessage(initiator, textToMessage(listing.get()));
            } else {
                this.ioEngine.report(
                        initiator, 
                        format("cannot list '%s' content.", location.get().name()));
            }
        } else {
            this.ioEngine.report(
                    initiator, 
                    format("cannot find '%s' Location", command.joinedArguments()));
        }
    }

    @Override
    public void listPath(Initiator initiator, ArgumentsCommand command) {
        String path = command.joinedArguments();
        if ( containsPathSeparator(path) ) {            
            String locationName = extractLocationFromPath(path);
            Optional<Location> location = this.domain
                    .locations()
                    .findByNamePattern(initiator, locationName);
            if ( location.isPresent() ) {
                String subpath = extractTargetFromPath(path);
                if ( StringUtils.nonEmpty(subpath) ) {
                    Path finalListingRoot = combinePathFrom(location.get().path(), subpath);
                    if ( pathIsDirectory(finalListingRoot) ) {
                        Optional<List<String>> listing = 
                                this.fileLister.listContentOf(finalListingRoot, 5);
                        if ( listing.isPresent() ) {
                            this.ioEngine.reportMessage(initiator, textToMessage(listing.get()));
                        } else {
                            this.ioEngine.report(
                                    initiator, 
                                    format("cannot list '%s/%s' content.", 
                                            location.get().name(), 
                                            subpath));
                        }
                    } else {
                        this.ioEngine.report(
                                initiator, 
                                format("'%s/%s' is not a directory.", 
                                        location.get().name(), 
                                        subpath));
                    }                    
                } else {
                    this.ioEngine.report(initiator, "subpath is empty.");
                }
            } else {
                this.ioEngine.report(
                        initiator, 
                        format("cannot find '%s' Location", locationName));
            }
        } else {
            this.ioEngine.report(
                    initiator, 
                    format("unknown case, '%s' is not path", path));
        }
    }    
}
