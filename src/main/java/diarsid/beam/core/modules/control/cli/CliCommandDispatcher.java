/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenPathCommand;
import diarsid.beam.core.base.control.io.interpreter.CommandDispatcher;
import diarsid.beam.core.modules.IoModule;

import static diarsid.beam.core.Beam.exitBeamCoreNow;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
class CliCommandDispatcher implements CommandDispatcher {
    
    private final IoModule ioModule;
    private final DomainModuleToCliAdapter domainModuleAdapter;
    
    CliCommandDispatcher(
            IoModule ioModule,
            DomainModuleToCliAdapter domainModuleAdapter) {
        this.ioModule = ioModule;
        this.domainModuleAdapter = domainModuleAdapter;
    }
    
    @Override
    public void dispatch(Initiator initiator, Command command) {
        debug("initiator:" + initiator.getId() + " commandType: " + command.type());
        try {
            switch ( command.type() ) {
                case OPEN_LOCATION: {
                    OpenLocationCommand c = (OpenLocationCommand) command;

                    break;
                }    
                case OPEN_PATH: {
                    OpenPathCommand c = (OpenPathCommand) command;

                    break;
                }    
                case RUN_PROGRAM:

                case START_PROGRAM:
                    break;
                case STOP_PROGRAM:
                    break;
                case BATCH_PAUSE:
                    break; 
                case CALL_BATCH:

                    break;
                case SEE_WEBPAGE:

                    break;
                case EXECUTOR_DEFAULT:

                    break;
                case OPEN_NOTES:
                    break;
                case OPEN_TARGET_IN_NOTE:
                    break;
                case OPEN_PATH_IN_NOTE:
                    break;
                case DELETE_MEM:
                    break;
                case DELETE_PAGE:
                    break;
                case CREATE_PAGE:
                    break;
                case EDIT_PAGE:
                    break;
                case DELETE_PAGE_DIR:
                    break;
                case CREATE_PAGE_DIR:
                    break;
                case EDIT_PAGE_DIR:
                    break;
                case DELETE_LOCATION: {
                    this.domainModuleAdapter
                            .getLocationsAdapter()
                            .removeLocationAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                }                
                case CREATE_LOCATION: {
                    this.domainModuleAdapter
                            .getLocationsAdapter()
                            .createLocationAndReport(initiator, 
                                    (ArgumentsCommand) command);
                    break;
                } case EDIT_LOCATION: {
                    this.domainModuleAdapter
                            .getLocationsAdapter()
                            .editLocationAndReport(initiator, 
                                    (ArgumentsCommand) command);
                    break;
                }                
                case DELETE_TASK : {
                    this.domainModuleAdapter
                            .getTasksAdapter()
                            .removeTaskAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                }                
                case CREATE_TASK : {
                    this.domainModuleAdapter
                            .getTasksAdapter()
                            .createTaskAndReport(initiator, 
                                    (ArgumentsCommand) command);
                    break;
                }                
                case EDIT_TASK : {
                    this.domainModuleAdapter
                            .getTasksAdapter()
                            .editTaskAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                }                
                case DELETE_BATCH:
                    this.domainModuleAdapter
                            .getBatchesAdapter()
                            .removeBatchAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                case CREATE_BATCH:
                    this.domainModuleAdapter
                            .getBatchesAdapter()
                            .createBatchAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                case EDIT_BATCH:
                    this.domainModuleAdapter
                            .getBatchesAdapter()
                            .editBatchAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                case LIST_LOCATION:
                    break;
                case LIST_PATH:
                    break;
                case FIND_LOCATION: {
                    this.domainModuleAdapter
                            .getLocationsAdapter()
                            .findLocationAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                }
                case FIND_PROGRAM : {
                    this.domainModuleAdapter
                            .getProgramsAdapter()
                            .findProgramAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                }
                case FIND_TASK : {
                    this.domainModuleAdapter
                            .getTasksAdapter()
                            .findTasksAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                }                
                case FIND_PAGE:
                    break;
                case FIND_WEBDIRECTORY:
                    break;
                case FIND_MEM:
                    break;
                case FIND_BATCH:
                    this.domainModuleAdapter
                            .getBatchesAdapter()
                            .findBatchAndReport(
                                    initiator, 
                                    (ArgumentsCommand) command);
                    break;
                case EXIT : {
                    this.ioModule.unregisterIoEngine(initiator);
                    asyncDoIndependently(() -> exitBeamCoreNow());
                    break;
                }
                case CLOSE_CONSOLE : {
                    this.ioModule.unregisterIoEngine(initiator);
                    break;
                }
                case UNDEFINED : {
                    break;
                }
                default : {
                    this.ioModule.getInnerIoEngine().report(initiator, "unknown command type.");
                }
            }
        } catch (ClassCastException cce) {
            logError(this.getClass(), cce);
            this.ioModule.getInnerIoEngine().report(initiator, "command type casting failed.");
        }
    }
}
