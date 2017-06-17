/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.domain.entities.BatchPauseCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CLOSE_CONSOLE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXIT;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.MULTICOMMAND;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_TARGET_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.domain.entities.TimePeriod.MINUTES;
import static diarsid.beam.core.domain.entities.TimePeriod.SECONDS;

/**
 *
 * @author Diarsid
 */
public class InterpreterTest {
    
    public static Interpreter interpreter;

    public InterpreterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        interpreter = new Interpreter();
    }
    
    
    @Test
    public void testInterprete_pause_number_seconds() {
        Command c = interpreter.interprete("pause 3 sec");
        assertEquals(BATCH_PAUSE, c.type());
        
        BatchPauseCommand c1 = (BatchPauseCommand) c;
        assertTrue(c1.isValid());
        assertEquals(3, c1.duration());
        assertEquals(SECONDS, c1.timePeriod());
    }    
    
    @Test
    public void testInterprete_pause_minutes_number() {
        Command c = interpreter.interprete("pause min 10");
        assertEquals(BATCH_PAUSE, c.type());
        
        BatchPauseCommand c1 = (BatchPauseCommand) c;
        assertTrue(c1.isValid());
        assertEquals(10, c1.duration());
        assertEquals(MINUTES, c1.timePeriod());
    }

    /**
     * Test of interprete method, of class Interpreter.
     */
    
    @Test
    public void testInterprete_controlPrefix_openLocation() {
        Command c = interpreter.interprete("/books");
        assertEquals(OPEN_LOCATION, c.type());
        
        OpenLocationCommand c1 = (OpenLocationCommand) c;
        assertEquals("books", c1.originalArgument());
    }
    
    @Test
    public void testInterprete_controlPrefix_l_openLocation() {
        Command c = interpreter.interprete("l/books");
        assertEquals(OPEN_LOCATION, c.type());
        
        OpenLocationCommand c1 = (OpenLocationCommand) c;
        assertEquals("books", c1.originalArgument());
    }
    
    @Test
    public void testInterprete_controlPrefix_openPathInLocation() {
        Command c = interpreter.interprete("/books/fan/tolkien");
        assertEquals(OPEN_LOCATION_TARGET, c.type());
        
        OpenLocationTargetCommand c1 = (OpenLocationTargetCommand) c;
        assertEquals("books", c1.originalLocation());
        assertEquals("fan/tolkien", c1.originalTarget());
    }
    
    @Test
    public void testInterprete_controlPrefix_l_openPathInLocation() {
        Command c = interpreter.interprete("l/books/fan/tolkien");
        assertEquals(OPEN_LOCATION_TARGET, c.type());
        
        OpenLocationTargetCommand c1 = (OpenLocationTargetCommand) c;
        assertEquals("books", c1.originalLocation());
        assertEquals("fan/tolkien", c1.originalTarget());
    }
    
    @Test
    public void testInterprete_controlPrefix_w_seePage() {
        Command c = interpreter.interprete("w/google");
        assertEquals(BROWSE_WEBPAGE, c.type());
        
        BrowsePageCommand c1 = (BrowsePageCommand) c;
        assertEquals("google", c1.argument().original());
    }
    
    @Test
    public void testInterprete_controlPrefix_i_seePage() {
        Command c = interpreter.interprete("i/google");
        assertEquals(BROWSE_WEBPAGE, c.type());
        
        BrowsePageCommand c1 = (BrowsePageCommand) c;
        assertEquals("google", c1.argument().original());
    }
    
    @Test
    public void testInterprete_controlPrefix_b_callBatch() {
        Command c = interpreter.interprete("b/env");
        assertEquals(CALL_BATCH, c.type());
        
        CallBatchCommand c1 = (CallBatchCommand) c;
        assertEquals("env", c1.argument().original());
    }
    
    @Test
    public void testInterprete_controlPrefix_r_runProgram() {
        Command c = interpreter.interprete("r/prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("prog", c1.argument().original());
    }
    
    @Test
    public void testInterprete_seePage() {
        Command c = interpreter.interprete("see google");
        assertEquals(BROWSE_WEBPAGE, c.type());
        
        BrowsePageCommand c1 = (BrowsePageCommand) c;
        assertEquals("google", c1.argument().original());
    }   
    
    
    @Test
    public void testInterprete_callBatch() {
        Command c = interpreter.interprete("call environment");
        assertEquals(CALL_BATCH, c.type());
        
        CallBatchCommand c1 = (CallBatchCommand) c;
        assertEquals("environment", c1.argument().original());
    }   
    
    
    @Test
    public void testInterprete_runProgram() {
        Command c = interpreter.interprete("run prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("prog", c1.argument().original());
    }
    
    @Test
    public void testInterprete_runSubpathProgram() {
        Command c = interpreter.interprete("run util/prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("util/prog", c1.argument().original());
    }
    
    @Test
    public void testInterprete_startProgram() {
        Command c = interpreter.interprete("START Prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("Progstart", c1.argument().original());
    }
    
    @Test
    public void testInterprete_startSubpathProgram() {
        Command c = interpreter.interprete("START util/Prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("util/Progstart", c1.argument().original());
    }
    
    @Test
    public void testInterprete_stopProgram() {
        Command c = interpreter.interprete("stop prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("progstop", c1.argument().original());
    }
    
    @Test
    public void testInterprete_stopSubpathProgram() {
        Command c = interpreter.interprete("stop util/prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("util/progstop", c1.argument().original());
    }
    
    @Test
    public void testInterprete_openLocation() {
        Command c = interpreter.interprete("open books");
        assertEquals(OPEN_LOCATION, c.type());
        
        OpenLocationCommand c1 = (OpenLocationCommand) c;
        assertEquals("books", c1.originalArgument());
    }
    
    @Test
    public void testInterprete_openPath() {
        Command c = interpreter.interprete("open books/tolkien");
        assertEquals(OPEN_LOCATION_TARGET, c.type());
        
        OpenLocationTargetCommand c1 = (OpenLocationTargetCommand) c;
        assertEquals("books/tolkien", c1.originalArgument());
    }
    
    @Test
    public void testInterprete_openPath_improperPath() {
        Command c = interpreter.interprete("open books/tolk%ien");
        assertEquals(MULTICOMMAND, c.type());
    }
    
    @Test
    public void testInterprete_openPath_improperPath_toShort() {
        Command c = interpreter.interprete("open b/tolkien");
        assertEquals(MULTICOMMAND, c.type());
    }
    
    @Test
    public void testInterprete_openNotes() {
        Command c1 = interpreter.interprete("n ");
        assertEquals(OPEN_NOTES, c1.type());
        
        Command c2 = interpreter.interprete("note ");
        assertEquals(OPEN_NOTES, c2.type());
    }
    
    @Test
    public void testInterprete_notes_as_openLocation() {
        Command c = interpreter.interprete("note todo");
        assertEquals(OPEN_TARGET_IN_NOTES, c.type());
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("todo", c1.joinedArguments());
    }
    
    @Test
    public void testInterprete_notes_as_openPath() {
        Command c = interpreter.interprete("n project/todo");
        assertEquals(OPEN_PATH_IN_NOTES, c.type());
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("project/todo", c1.joinedArguments());
    }
    
    @Test
    public void testInterprete_default_as_openLocation() {
        Command c = interpreter.interprete("books");
        assertEquals(EXECUTOR_DEFAULT, c.type());
        
        ExecutorDefaultCommand c1 = (ExecutorDefaultCommand) c;
        assertEquals("books", c1.argument());
    }
    
    @Test
    public void testInterprete_default_as_openPath() {
        Command c = interpreter.interprete("books/fant/tolkien");
        assertEquals(OPEN_LOCATION_TARGET, c.type());
        
        OpenLocationTargetCommand c1 = (OpenLocationTargetCommand) c;
        assertEquals("books/fant/tolkien", c1.originalArgument());
        
        assertEquals("books", c1.originalLocation());
        assertEquals("fant/tolkien", c1.originalTarget());
    }
    
    @Test
    public void testInterprete_deleteTask() {
        Command c = interpreter.interprete("del task my text");
        assertEquals(DELETE_TASK, c.type());
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("my text", c1.joinedArguments());
    }
    
    @Test
    public void testInterprete_deleteLocation_withName() {
        Command c = interpreter.interprete("del loc name asasd");
        assertEquals(DELETE_LOCATION, c.type());
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("name asasd", c1.joinedArguments());
    }
    
    @Test
    public void testInterprete_deleteLocation_withoutName() {
        Command c = interpreter.interprete("del loc");
        assertEquals(DELETE_LOCATION, c.type());
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertFalse(c1.hasArguments());
    }
    
    @Test
    public void testInterprete_createLocation_pathAndName_straightOrder() {
        Command c1 = interpreter.interprete("+ loc books C:/path/to/my_books");
        assertEquals(CREATE_LOCATION, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("books C:/path/to/my_books", c1casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_createLocation_pathAndName() {
        Command c1 = interpreter.interprete("+ loc C:/path/to/my_books books ");
        assertEquals(CREATE_LOCATION, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("C:/path/to/my_books books", c1casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_createLocation_onlyName() {
        Command c1 = interpreter.interprete("+ loc books ");
        assertEquals(CREATE_LOCATION, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("books", c1casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_createLocation_noPathNoName() {
        Command c1 = interpreter.interprete("+ loc");
        assertEquals(CREATE_LOCATION, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertFalse(c1casted.hasArguments());
    }
        
    
    @Test
    public void testInterprete_createPage_pathAndName_straightOrder() {
        Command c1 = interpreter.interprete("+ page google https://google.com");
        assertEquals(CREATE_PAGE, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("google https://google.com", c1casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_createPage_pathAndName() {
        Command c1 = interpreter.interprete("+ page https://google.com google");
        assertEquals(CREATE_PAGE, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("https://google.com google", c1casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_createPage_pathAndNameAndPlace() {
        Command c1 = interpreter.interprete("+ page https://google.com google panel");
        assertEquals(CREATE_PAGE, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("https://google.com google panel", c1casted.joinedArguments());
    }    
        
    @Test
    public void testInterprete_createPage_noPathNoName() {
        Command c1 = interpreter.interprete("+ page");
        assertEquals(CREATE_PAGE, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertFalse(c1casted.hasArguments());
    }
    
    @Test
    public void testInterprete_createTask_timeAndTask() {
        Command c1 = interpreter.interprete("+ task 10 22:00 to do someth");
        assertEquals(CREATE_TASK, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("10 22:00 to do someth", c1casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_createTask_dayHourMinute() {
        Command c1 = interpreter.interprete("+ task 10 22:00");
        assertEquals(CREATE_TASK, c1.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        assertEquals("10 22:00", c1casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_editBatch_undefined() {
        Command c = interpreter.interprete("edit batch");
        assertEquals(EDIT_BATCH, c.type());
        
        ArgumentsCommand command = (ArgumentsCommand) c;
        assertFalse(command.hasArguments());
    }
    
    @Test
    public void testInterprete_editBatch_name() {
        Command c = interpreter.interprete("edit batch name");
        assertEquals(EDIT_BATCH, c.type());
        
        ArgumentsCommand command = (ArgumentsCommand) c;
        assertTrue(command.hasArguments());
        assertEquals("name", command.joinedArguments());
    }
    
    @Test
    public void testInterprete_createPageDir() {
        Command c1 = interpreter.interprete("+ page dir devtools bookm");
        Command c2 = interpreter.interprete("+ page dir panel devtools");
        
        assertEquals(CREATE_WEB_DIR, c1.type());
        assertEquals(CREATE_WEB_DIR, c2.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        ArgumentsCommand c2casted = (ArgumentsCommand) c2;
        
        assertEquals("devtools bookm", c1casted.joinedArguments());
        assertEquals("panel devtools", c2casted.joinedArguments());
    }
    
    @Test
    public void testInterprete_createDir() {
        Command c1 = interpreter.interprete("+ dir devtools bookm");
        Command c2 = interpreter.interprete("+ dir panel devtools");
        
        assertEquals(CREATE_WEB_DIR, c1.type());
        assertEquals(CREATE_WEB_DIR, c2.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        ArgumentsCommand c2casted = (ArgumentsCommand) c2;
        
        assertEquals("devtools bookm", c1casted.joinedArguments());
        assertEquals("panel devtools", c2casted.joinedArguments());
        
    }
    
    @Test
    public void testInterprete_createDir_noPlaceNoName() {
        Command c1 = interpreter.interprete("+ page dir");
        Command c2 = interpreter.interprete("+ dir");
        
        assertEquals(CREATE_WEB_DIR, c1.type());
        assertEquals(CREATE_WEB_DIR, c2.type());
        
        ArgumentsCommand c1casted = (ArgumentsCommand) c1;
        ArgumentsCommand c2casted = (ArgumentsCommand) c2;
        
        assertFalse(c1casted.hasArguments());
        assertFalse(c2casted.hasArguments()); 
    }
    
    @Test
    public void testInterprete_editPage() {
        Command c = interpreter.interprete("edit page ");
        assertEquals(EDIT_PAGE, c.type());
        ArgumentsCommand com = (ArgumentsCommand) c;
        assertFalse(com.hasArguments());
    }
    
    @Test
    public void testInterprete_editPage_name_target() {
        Command c = interpreter.interprete("edit page google name");
        assertEquals(EDIT_PAGE, c.type());
        
        ArgumentsCommand com = (ArgumentsCommand) c;
        assertEquals("google name", com.joinedArguments());
    }
    
    @Test
    public void testInterprete_editDir_1() {
        Command c = interpreter.interprete("edit page dir");
        assertEquals(EDIT_WEB_DIR, c.type());
        ArgumentsCommand com = (ArgumentsCommand) c;
        assertFalse(com.hasArguments());
    }
    
    @Test
    public void testInterprete_editDir_2() {
        Command c = interpreter.interprete("edit dir");
        assertEquals(EDIT_WEB_DIR, c.type());
        ArgumentsCommand com = (ArgumentsCommand) c;
        assertFalse(com.hasArguments());
    }
    
    @Test
    public void testInterprete_editDir_name_target() {
        Command c = interpreter.interprete("edit page dir common name");
        Command c1 = interpreter.interprete("edit directory common place");
        assertEquals(EDIT_WEB_DIR, c.type());
        assertEquals(EDIT_WEB_DIR, c1.type());
        
        ArgumentsCommand com = (ArgumentsCommand) c;
        ArgumentsCommand com1 = (ArgumentsCommand) c1;
        
        assertEquals("common name", com.joinedArguments());
        assertEquals("common place", com1.joinedArguments());
    }
    
    @Test
    public void testInterprete_editLocation_name_targetName() {
        Command c = interpreter.interprete("edit location boo name");        
        assertEquals(EDIT_LOCATION, c.type());      
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("boo name", c1.joinedArguments());
    }  
    
    @Test
    public void testInterprete_editLocation_name_targetPath() {
        Command c = interpreter.interprete("edit location boo path");        
        assertEquals(EDIT_LOCATION, c.type());      
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("boo path", c1.joinedArguments());
    }
    
    @Test
    public void testInterprete_editTask_textAndTime() {
        Command c = interpreter.interprete("edit task 10:10 task text");        
        assertEquals(EDIT_TASK, c.type());        
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("10:10 task text", c1.joinedArguments());
    }
    
    @Test
    public void testInterprete_editTask_textAndTime_1() {
        Command c = interpreter.interprete("edit task ");        
        assertEquals(EDIT_TASK, c.type());        
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertFalse(c1.hasArguments());
    }
        
    @Test
    public void testInterprete_close() {
        Command c = interpreter.interprete("close");
        assertEquals(CLOSE_CONSOLE, c.type());   
    }
    
    @Test
    public void testInterprete_exit() {
        Command c = interpreter.interprete("exit");
        assertEquals(EXIT, c.type());   
    }
    
    @Test
    public void testInterprete_listLocation() {
        Command c = interpreter.interprete("list books");
        assertEquals(LIST_LOCATION, c.type()); 
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("books", c1.getFirstArg());
    }  
    
    @Test
    public void testInterprete_listPath() {
        Command c = interpreter.interprete("list books/tech/java");
        assertEquals(LIST_PATH, c.type()); 
        
        ArgumentsCommand c1 = (ArgumentsCommand) c;
        assertEquals("books/tech/java", c1.getFirstArg());
    }  
}