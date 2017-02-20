/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;

/**
 *
 * @author Diarsid
 */
public class CliCommandDispatcherTest {
    
    private static final Interpreter interpreter = new Interpreter();
    private static final Initiator initiator = new Initiator();
    private static CliCommandDispatcher dispatcher;

    public CliCommandDispatcherTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of dispatch method, of class CliCommandDispatcher.
     */
    @Test
    public void testDispatch() {
        Command command = interpreter.interprete("+ page google");
        dispatcher.dispatch(initiator, command);
    }
    
    public void dispatch(Command command) {
        switch ( command.type() ) {
            case OPEN_LOCATION:
                break;
            case OPEN_PATH:
                break;
            case RUN_PROGRAM:
                break;
            case START_PROGRAM:
                break;
            case STOP_PROGRAM:
                break;
            case CALL_BATCH:
                break;
            case BATCH_PAUSE:
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
            case FIND_PROGRAM:
                break;
            case FIND_TASK:
                break;
            case FIND_PAGE:
                break;
            case FIND_WEBDIRECTORY:
                break;
            case FIND_MEM:
                break;
            case FIND_BATCH:
                break;
            case EXIT:
                break;
            case CLOSE_CONSOLE:
                break;
            case UNDEFINED:
                break;
            default:
                throw new AssertionError(command.type().name());
            
        }
    }

}