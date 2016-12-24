/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.control.io.commands.executor.OpenPathCommand;
import diarsid.beam.core.control.io.interpreter.CommandDispatcher;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.IoModule;

import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
public class CoreCommandDispatcher implements CommandDispatcher {
    
    private final IoModule ioModule;
    private CoreControlModule coreControlModule;
    
    public CoreCommandDispatcher(
            IoModule ioModule) {
        this.ioModule = ioModule;
    }
    
    void setCoreControl(CoreControlModule coreControlModule) {
        this.coreControlModule = coreControlModule;
    }
    
    @Override
    public void dispatch(Initiator initiator, Command command) {
        debug("initiator:" + initiator.getId() + " commandType: " + command.getType());
        switch ( command.getType() ) {
            case OPEN_LOCATION: {
                OpenLocationCommand c = (OpenLocationCommand) command;
                
                break;
            }    
            case OPEN_PATH: {
                OpenPathCommand c = (OpenPathCommand) command;
                
                break;
            }    
            case RUN_PROGRAM:
                break;
            case RUN_MARKED_PROGRAM:
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
            case DELETE_LOCATION:
                break;
            case CREATE_LOCATION:
                break;
            case EDIT_LOCATION:
                break;
            case DELETE_TASK:
                break;
            case CREATE_TASK:
                break;
            case EDIT_TASK:
                break;
            case DELETE_REMINDER:
                break;
            case CREATE_REMINDER:
                break;
            case EDIT_REMINDER:
                break;
            case DELETE_EVENT:
                break;
            case CREATE_EVENT:
                break;
            case EDIT_EVENT:
                break;
            case DELETE_BATCH:
                break;
            case CREATE_BATCH:
                break;
            case EDIT_BATCH:
                break;
            case LIST_LOCATION:
                break;
            case LIST_PATH:
                break;
            case FIND_LOCATION:
                break;
            case FIND_TASK:
                break;
            case FIND_EVENT:
                break;
            case FIND_REMINDER:
                break;
            case FIND_PAGE:
                break;
            case FIND_WEBDIRECTORY:
                break;
            case FIND_MEM:
                break;
            case FIND_BATCH:
                break;
            case EXIT : {
                this.coreControlModule.exitBeam();
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
                throw new AssertionError(command.getType().name());
            }
        }
    }
}
