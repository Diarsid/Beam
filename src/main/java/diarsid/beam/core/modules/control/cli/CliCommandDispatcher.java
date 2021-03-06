/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.console.ConsoleCommandDispatcher;
import diarsid.beam.core.base.control.io.base.console.ConsoleCommandRealProcessor;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.HelpKey;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.PluginTaskCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;

import static diarsid.beam.core.Beam.beamRuntime;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.support.log.Logging.logFor;

import diarsid.beam.core.modules.BeamEnvironmentModule;

/**
 *
 * @author Diarsid
 */
public class CliCommandDispatcher implements ConsoleCommandDispatcher {
    
    private final IoModule ioModule;
    private final ExecutorModule executorModule;
    private final DomainModuleToCliAdapter domainModuleAdapter;
    private final HelpKey exitHelp;
    
    CliCommandDispatcher(
            IoModule ioModule,
            ExecutorModule executorModule,
            DomainModuleToCliAdapter domainModuleAdapter) {
        this.ioModule = ioModule;
        this.executorModule = executorModule;
        this.domainModuleAdapter = domainModuleAdapter;
        this.exitHelp = this.ioModule.getInnerIoEngine().addToHelpContext(
                "Confirm if you want to exit Beam.",
                "Use:",
                "   - y/yes/+ to confirm exiting",
                "   - n/no or any other key to break"
        );
    }
    
    public static ConsoleCommandRealProcessor buildCommandLineProcessor(
            IoModule ioModule, 
            BeamEnvironmentModule appComponentsHolderModule, 
            ExecutorModule executorModule,
            DomainKeeperModule domainModule) {
        Interpreter interpreter = appComponentsHolderModule.interpreter();
        DomainModuleToCliAdapter domainToCliAdapter = 
                new DomainModuleToCliAdapter(domainModule, ioModule.getInnerIoEngine());
        CliCommandDispatcher commandDispatcher = 
                new CliCommandDispatcher(
                        ioModule, executorModule, domainToCliAdapter);
        ConsoleCommandRealProcessor clp = new ConsoleCommandRealProcessor(interpreter, commandDispatcher);
        return clp;
    }
    
    @Override
    public void dispatch(Initiator initiator, Command command) {
        logFor(this).info("initiator:" + initiator.identity() + " commandType: " + command.type());
        try {
            switch ( command.type() ) {
                case OPEN_LOCATION: {
                    this.executorModule
                            .openLocation(initiator, (OpenLocationCommand) command);
                    break;
                }    
                case OPEN_LOCATION_TARGET: {
                    this.executorModule
                            .openLocationTarget(initiator, (OpenLocationTargetCommand) command);
                    break;
                }    
                case RUN_PROGRAM : {
                    this.executorModule
                            .runProgram(initiator, (RunProgramCommand) command);
                    break;
                }
                case BATCH_PAUSE : {
                    this.ioModule
                            .getInnerIoEngine()
                            .report(initiator, "is available only in batch.");
                    break; 
                }    
                case CALL_BATCH : {
                    this.executorModule
                            .callBatch(initiator, (CallBatchCommand) command);
                    break;
                }    
                case BROWSE_WEBPAGE : {
                    this.executorModule
                            .browsePage(initiator, (BrowsePageCommand) command);
                    break;
                }    
                case EXECUTOR_DEFAULT : {
                    this.executorModule
                            .executeDefault(initiator, (ExecutorDefaultCommand) command);
                    break;
                }    
                case PLUGIN_TASK : {
                    this.executorModule
                            .executePlugin(initiator, (PluginTaskCommand) command);
                    break;
                }
                case CREATE_NOTE : {
                    this.domainModuleAdapter
                            .notesAdaper()
                            .createNoteAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }
                case OPEN_NOTES : {
                    this.domainModuleAdapter
                            .notesAdaper()
                            .openNotesAndReport(initiator, (EmptyCommand) command);
                    break;
                }    
                case OPEN_TARGET_IN_NOTES : {
                    this.domainModuleAdapter
                            .notesAdaper()
                            .openTargetInNotesAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case OPEN_PATH_IN_NOTES : {
                    this.domainModuleAdapter
                            .notesAdaper()
                            .openPathInNotesAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case DELETE_MEM : {
                    this.domainModuleAdapter
                            .commandsMemoryAdapter()
                            .deleteMemAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case DELETE_PAGE : {
                    this.domainModuleAdapter
                            .webPagesAdapter()
                            .deleteWebPageAndReport(initiator, (ArgumentsCommand) command);
                    break;                    
                }
                case CREATE_PAGE : {
                    this.domainModuleAdapter
                            .webPagesAdapter()
                            .createWebPageAndReport(initiator, (ArgumentsCommand) command);
                    break;                    
                }
                case EDIT_PAGE : {
                    this.domainModuleAdapter
                            .webPagesAdapter()
                            .editWebPageAndReport(initiator, (ArgumentsCommand) command);
                    break;                    
                }
                case DELETE_WEB_DIR : {
                    this.domainModuleAdapter
                            .webDirectoriesAdapter()
                            .removeWebDirectoryAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case CREATE_WEB_DIR : {
                    this.domainModuleAdapter
                            .webDirectoriesAdapter()
                            .createWebDirectoryAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case EDIT_WEB_DIR : {
                    this.domainModuleAdapter
                            .webDirectoriesAdapter()
                            .editWebDirectoryAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case DELETE_LOCATION : {
                    this.domainModuleAdapter
                            .locationsAdapter()
                            .removeLocationAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }                
                case CREATE_LOCATION : {
                    this.domainModuleAdapter
                            .locationsAdapter()
                            .createLocationAndReport(initiator, (ArgumentsCommand) command);
                    break;
                } 
                case EDIT_LOCATION : {
                    this.domainModuleAdapter
                            .locationsAdapter()
                            .editLocationAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }                
                case DELETE_TASK : {
                    this.domainModuleAdapter
                            .tasksAdapter()
                            .removeTaskAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }                
                case CREATE_TASK : {
                    this.domainModuleAdapter
                            .tasksAdapter()
                            .createTaskAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }                
                case EDIT_TASK : {
                    this.domainModuleAdapter
                            .tasksAdapter()
                            .editTaskAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }                
                case DELETE_BATCH : {
                    this.domainModuleAdapter
                            .batchesAdapter()
                            .removeBatchAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case CREATE_BATCH : {
                    this.domainModuleAdapter
                            .batchesAdapter()
                            .createBatchAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case EDIT_BATCH : {
                    this.domainModuleAdapter
                            .batchesAdapter()
                            .editBatchAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case LIST_LOCATION : {
                    this.executorModule.listLocation(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case LIST_PATH : {
                    this.executorModule.listPath(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case FIND_LOCATION : {
                    this.domainModuleAdapter
                            .locationsAdapter()
                            .findLocationAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }
                case FIND_PROGRAM : {
                    this.domainModuleAdapter
                            .programsAdapter()
                            .findProgramAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }
                case FIND_TASK : {
                    this.domainModuleAdapter
                            .tasksAdapter()
                            .findTasksAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }                
                case FIND_WEBPAGE : {
                    this.domainModuleAdapter
                            .webPagesAdapter()
                            .findWebPageAndReport(initiator, (ArgumentsCommand) command);
                    break;                    
                }
                case FIND_WEBDIRECTORY : {
                    this.domainModuleAdapter
                            .webDirectoriesAdapter()
                            .findWebDirectoryAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case FIND_MEM : {
                    this.domainModuleAdapter
                            .commandsMemoryAdapter()
                            .findCommandAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }    
                case FIND_BATCH : {
                    this.domainModuleAdapter
                            .batchesAdapter()
                            .findBatchAndReport(initiator, (ArgumentsCommand) command);
                    break;
                }   
                case FIND_ALL : {
                    this.domainModuleAdapter
                            .allAdapter()
                            .findAll(initiator, (ArgumentsCommand) command);
                    break;
                }
                case SHOW_WEBPANEL : 
                case SHOW_BOOKMARKS : {
                    this.domainModuleAdapter
                            .webPagesAdapter()
                            .showWebPlace(initiator, (EmptyCommand) command);
                    break;
                }
                case EXIT : {
                    Choice choice = this.ioModule
                            .getInnerIoEngine()
                            .ask(initiator, "are you sure", this.exitHelp);
                    if ( choice.isPositive() ) {
                        asyncDoIndependently(
                                "Beam exit Thread", 
                                () -> beamRuntime().exitBeamCoreNow());
                    }                    
                    break;
                }
                case CLOSE_CONSOLE : {
                    this.ioModule.onIoEngineClosingRequest(initiator);
                    break;
                }
                case BROWSE_WEBPANEL : {
                    this.executorModule.browseWebPanel(initiator);
                    break;
                }
                case CAPTURE_PAGE_IMAGE : {
                    this.domainModuleAdapter
                            .webPagesAdapter()
                            .captureWebPageImage(initiator, (ArgumentsCommand) command);
                    break;
                }
                case SHOW_ALL_LOCATIONS : {
                    this.domainModuleAdapter
                            .locationsAdapter()
                            .showAllLocations(initiator);
                    break;
                }
                case SHOW_ALL_PROGRAMS : {
                    this.domainModuleAdapter
                            .programsAdapter()
                            .showAllPrograms(initiator);
                    break;
                }
                case SHOW_ALL_TASKS : {
                    break;
                }
                case SHOW_ALL_WEBPAGES : {
                    this.domainModuleAdapter
                            .webPagesAdapter()
                            .showAllWebPages(initiator);
                    break;
                }
                case SHOW_ALL_WEBDIRECTORIES : {
                    this.domainModuleAdapter
                            .webDirectoriesAdapter()
                            .showAllWebDirectories(initiator);
                    break;
                }
                case SHOW_ALL_BATCHES : {
                    this.domainModuleAdapter
                            .batchesAdapter()
                            .showAllBatches(initiator);
                    break;
                }
//                case MULTICOMMAND :
                case INCORRECT : 
                case UNDEFINED : {
                    break;
                }
                default : {
                    this.ioModule
                            .getInnerIoEngine()
                            .report(initiator, "undispatchable command type.");
                }
            }
        } catch (ClassCastException cce) {
            logFor(this).error(cce.getMessage(), cce);
            this.ioModule
                    .getInnerIoEngine()
                    .report(initiator, "command type casting failed.");
        } catch (Exception e) {
            logFor(this).error("Exception occured", e);
            this.ioModule
                    .getInnerIoEngine()
                    .report(initiator, "Error occured, execution failed.");
        }
    }
}
