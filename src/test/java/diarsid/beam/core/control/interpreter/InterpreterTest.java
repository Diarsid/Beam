/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import diarsid.beam.core.control.io.interpreter.Interpreter;

import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.SingleStringCommand;
import diarsid.beam.core.control.io.commands.TimeEntityEditCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.control.io.commands.creation.CreateTaskCommand;
import diarsid.beam.core.control.io.commands.creation.CreateWebDirectoryCommand;
import diarsid.beam.core.control.io.commands.creation.CreateWebPageCommand;
import diarsid.beam.core.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.control.io.commands.executor.OpenPathCommand;
import diarsid.beam.core.control.io.commands.executor.RunMarkedProgramCommand;
import diarsid.beam.core.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.control.io.commands.executor.SeePageCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static diarsid.beam.core.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.CLOSE_CONSOLE;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_PAGE;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_PAGE_DIR;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_TASK;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_PAGE_DIR;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_TASK;
import static diarsid.beam.core.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.control.io.commands.CommandType.EXIT;
import static diarsid.beam.core.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_PATH;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_PATH_IN_NOTE;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_TARGET_IN_NOTE;
import static diarsid.beam.core.control.io.commands.CommandType.RUN_MARKED_PROGRAM;
import static diarsid.beam.core.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.control.io.commands.CommandType.SEE_WEBPAGE;
import static diarsid.beam.core.control.io.commands.CommandType.UNDEFINED;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_COMMANDS;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_NAME;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_ORDER;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_PATH;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_PLACE;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_UNDEFINED;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_URL;
import static diarsid.beam.core.domain.entities.WebPlacement.BOOKMARKS;
import static diarsid.beam.core.domain.entities.WebPlacement.WEBPANEL;

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

    /**
     * Test of interprete method, of class Interpreter.
     */
    
    @Test
    public void testInterprete_controlPrefix_openLocation() {
        Command c = interpreter.interprete("/books");
        assertEquals(OPEN_LOCATION, c.type());
        
        OpenLocationCommand c1 = (OpenLocationCommand) c;
        assertEquals("books", c1.stringifyOriginal());
    }
    
    @Test
    public void testInterprete_controlPrefix_l_openLocation() {
        Command c = interpreter.interprete("l/books");
        assertEquals(OPEN_LOCATION, c.type());
        
        OpenLocationCommand c1 = (OpenLocationCommand) c;
        assertEquals("books", c1.stringifyOriginal());
    }
    
    @Test
    public void testInterprete_controlPrefix_openPathInLocation() {
        Command c = interpreter.interprete("/books/fan/tolkien");
        assertEquals(OPEN_PATH, c.type());
        
        OpenPathCommand c1 = (OpenPathCommand) c;
        assertEquals("books", c1.location().getOriginal());
        assertEquals("fan/tolkien", c1.target().getOriginal());
    }
    
    @Test
    public void testInterprete_controlPrefix_l_openPathInLocation() {
        Command c = interpreter.interprete("l/books/fan/tolkien");
        assertEquals(OPEN_PATH, c.type());
        
        OpenPathCommand c1 = (OpenPathCommand) c;
        assertEquals("books", c1.location().getOriginal());
        assertEquals("fan/tolkien", c1.target().getOriginal());
    }
    
    @Test
    public void testInterprete_controlPrefix_w_seePage() {
        Command c = interpreter.interprete("w/google");
        assertEquals(SEE_WEBPAGE, c.type());
        
        SeePageCommand c1 = (SeePageCommand) c;
        assertEquals("google", c1.page().getOriginal());
    }
    
    @Test
    public void testInterprete_controlPrefix_i_seePage() {
        Command c = interpreter.interprete("i/google");
        assertEquals(SEE_WEBPAGE, c.type());
        
        SeePageCommand c1 = (SeePageCommand) c;
        assertEquals("google", c1.page().getOriginal());
    }
    
    @Test
    public void testInterprete_controlPrefix_b_callBatch() {
        Command c = interpreter.interprete("b/env");
        assertEquals(CALL_BATCH, c.type());
        
        CallBatchCommand c1 = (CallBatchCommand) c;
        assertEquals("env", c1.argument().getOriginal());
    }
    
    @Test
    public void testInterprete_controlPrefix_r_runProgram() {
        Command c = interpreter.interprete("r/prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("prog", c1.argument().getOriginal());
    }
    
    @Test
    public void testInterprete_seePage() {
        Command c = interpreter.interprete("see google");
        assertEquals(SEE_WEBPAGE, c.type());
        
        SeePageCommand c1 = (SeePageCommand) c;
        assertEquals("google", c1.page().getOriginal());
    }   
    
    
    @Test
    public void testInterprete_callBatch() {
        Command c = interpreter.interprete("call environment");
        assertEquals(CALL_BATCH, c.type());
        
        CallBatchCommand c1 = (CallBatchCommand) c;
        assertEquals("environment", c1.argument().getOriginal());
    }   
    
    
    @Test
    public void testInterprete_runProgram() {
        Command c = interpreter.interprete("run prog");
        assertEquals(RUN_PROGRAM, c.type());
        
        RunProgramCommand c1 = (RunProgramCommand) c;
        assertEquals("prog", c1.argument().getOriginal());
    }
    
    @Test
    public void testInterprete_startProgram() {
        Command c = interpreter.interprete("START Prog");
        assertEquals(RUN_MARKED_PROGRAM, c.type());
        
        RunMarkedProgramCommand c1 = (RunMarkedProgramCommand) c;
        assertEquals("Prog", c1.program().getOriginal());
        assertEquals("start", c1.getMark());
    }
    
    @Test
    public void testInterprete_stopProgram() {
        Command c = interpreter.interprete("stop prog");
        assertEquals(RUN_MARKED_PROGRAM, c.type());
        
        RunMarkedProgramCommand c1 = (RunMarkedProgramCommand) c;
        assertEquals("prog", c1.program().getOriginal());
        assertEquals("stop", c1.getMark());
    }
    
    @Test
    public void testInterprete_openLocation() {
        Command c = interpreter.interprete("open books");
        assertEquals(OPEN_LOCATION, c.type());
        
        OpenLocationCommand c1 = (OpenLocationCommand) c;
        assertEquals("books", c1.stringifyOriginal());
    }
    
    @Test
    public void testInterprete_openPath() {
        Command c = interpreter.interprete("open books/tolkien");
        assertEquals(OPEN_PATH, c.type());
        
        OpenPathCommand c1 = (OpenPathCommand) c;
        assertEquals("books/tolkien", c1.stringifyOriginal());
    }
    
    @Test
    public void testInterprete_openPath_improperPath() {
        Command c = interpreter.interprete("open books/tolk%ien");
        assertEquals(UNDEFINED, c.type());
    }
    
    @Test
    public void testInterprete_openPath_improperPath_toShort() {
        Command c = interpreter.interprete("open b/tolkien");
        assertEquals(UNDEFINED, c.type());
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
        assertEquals(OPEN_TARGET_IN_NOTE, c.type());
        
        SingleStringCommand c1 = (SingleStringCommand) c;
        assertEquals("todo", c1.getArg());
    }
    
    @Test
    public void testInterprete_notes_as_openPath() {
        Command c = interpreter.interprete("n project/todo");
        assertEquals(OPEN_PATH_IN_NOTE, c.type());
        
        SingleStringCommand c1 = (SingleStringCommand) c;
        assertEquals("project/todo", c1.getArg());
    }
    
    @Test
    public void testInterprete_default_as_openLocation() {
        Command c = interpreter.interprete("books");
        assertEquals(EXECUTOR_DEFAULT, c.type());
        
        ExecutorDefaultCommand c1 = (ExecutorDefaultCommand) c;
        assertEquals("books", c1.stringifyOriginal());
    }
    
    @Test
    public void testInterprete_default_as_openPath() {
        Command c = interpreter.interprete("books/fant/tolkien");
        assertEquals(OPEN_PATH, c.type());
        
        OpenPathCommand c1 = (OpenPathCommand) c;
        assertEquals("books/fant/tolkien", c1.stringifyOriginal());
        
        assertEquals("books", c1.location().getOriginal());
        assertEquals("fant/tolkien", c1.target().getOriginal());
    }
    
    @Test
    public void testInterprete_deleteTask() {
        Command c = interpreter.interprete("del task my text");
        assertEquals(DELETE_TASK, c.type());
        
        SingleStringCommand c1 = (SingleStringCommand) c;
        assertEquals("my text", c1.getArg());
    }
    
    @Test
    public void testInterprete_deleteLocation_withName() {
        Command c = interpreter.interprete("del loc name asasd");
        assertEquals(DELETE_LOCATION, c.type());
        
        SingleStringCommand c1 = (SingleStringCommand) c;
        assertEquals("name", c1.getArg());
    }
    
    @Test
    public void testInterprete_deleteLocation_withoutName() {
        Command c = interpreter.interprete("del loc");
        assertEquals(DELETE_LOCATION, c.type());
        
        SingleStringCommand c1 = (SingleStringCommand) c;
        assertFalse(c1.hasArg());
    }
    
    @Test
    public void testInterprete_createLocation_pathAndName_straightOrder() {
        Command c1 = interpreter.interprete("+ loc books C:/path/to/my_books");
        assertEquals(CREATE_LOCATION, c1.type());
        
        CreateLocationCommand c1casted = (CreateLocationCommand) c1;
        assertEquals("books", c1casted.getName());
        assertEquals("C:/path/to/my_books", c1casted.getPath());
    }
    
    @Test
    public void testInterprete_createLocation_pathAndName() {
        Command c1 = interpreter.interprete("+ loc C:/path/to/my_books books ");
        assertEquals(CREATE_LOCATION, c1.type());
        
        CreateLocationCommand c1casted = (CreateLocationCommand) c1;
        assertEquals("books", c1casted.getName());
        assertEquals("C:/path/to/my_books", c1casted.getPath());
    }
    
    @Test
    public void testInterprete_createLocation_onlyPath() {
        Command c1 = interpreter.interprete("+ loc C:/path/to/my_books ");
        assertEquals(CREATE_LOCATION, c1.type());
        
        CreateLocationCommand c1casted = (CreateLocationCommand) c1;
        assertFalse(c1casted.hasName());
        assertEquals("C:/path/to/my_books", c1casted.getPath());
    }
    
    @Test
    public void testInterprete_createLocation_onlyName() {
        Command c1 = interpreter.interprete("+ loc books ");
        assertEquals(CREATE_LOCATION, c1.type());
        
        CreateLocationCommand c1casted = (CreateLocationCommand) c1;
        assertEquals("books", c1casted.getName());
        assertFalse(c1casted.hasPath());
    }
    
    @Test
    public void testInterprete_createLocation_noPathNoName() {
        Command c1 = interpreter.interprete("+ loc");
        assertEquals(CREATE_LOCATION, c1.type());
        
        CreateLocationCommand c1casted = (CreateLocationCommand) c1;
        assertFalse(c1casted.hasName());
        assertFalse(c1casted.hasPath());
    }
        
    
    @Test
    public void testInterprete_createPage_pathAndName_straightOrder() {
        Command c1 = interpreter.interprete("+ page google https://google.com");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertEquals("google", c1casted.getName());
        assertEquals("https://google.com", c1casted.getUrl());
    }
    
    @Test
    public void testInterprete_createPage_pathAndName() {
        Command c1 = interpreter.interprete("+ page https://google.com google");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertEquals("google", c1casted.getName());
        assertEquals("https://google.com", c1casted.getUrl());
    }
    
    @Test
    public void testInterprete_createPage_pathAndNameAndPlace() {
        Command c1 = interpreter.interprete("+ page https://google.com google panel");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertEquals("google", c1casted.getName());
        assertEquals("https://google.com", c1casted.getUrl());
        assertEquals(WEBPANEL.name(), c1casted.getPlace());
    }    
    
    @Test
    public void testInterprete_createPage_onlyPath() {
        Command c1 = interpreter.interprete("+ page https://google.com ");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertFalse(c1casted.hasName());
        assertEquals("https://google.com", c1casted.getUrl());
    }
    
    @Test
    public void testInterprete_createPage_pathPlace() {
        Command c1 = interpreter.interprete("+ page https://google.com panel");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertFalse(c1casted.hasName());
        assertEquals("https://google.com", c1casted.getUrl());
        assertEquals(WEBPANEL.name(), c1casted.getPlace());
    }
    
    @Test
    public void testInterprete_createPage_onlyName() {
        Command c1 = interpreter.interprete("+ page google ");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertEquals("google", c1casted.getName());
        assertFalse(c1casted.hasUrl());
    }
    
    @Test
    public void testInterprete_createPage_namePlace() {
        Command c1 = interpreter.interprete("+ page google bookm");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertEquals("google", c1casted.getName());
        assertEquals(BOOKMARKS.name(), c1casted.getPlace());
        assertFalse(c1casted.hasUrl());
    }
    
    @Test
    public void testInterprete_createPage_noPathNoName() {
        Command c1 = interpreter.interprete("+ page");
        assertEquals(CREATE_PAGE, c1.type());
        
        CreateWebPageCommand c1casted = (CreateWebPageCommand) c1;
        assertFalse(c1casted.hasName());
        assertFalse(c1casted.hasUrl());
    }
    
    @Test
    public void testInterprete_createTask_timeAndTask() {
        Command c1 = interpreter.interprete("+ task 10 22:00 to do someth");
        assertEquals(CREATE_TASK, c1.type());
        
        CreateTaskCommand c1casted = (CreateTaskCommand) c1;
        assertEquals("10 22:00", c1casted.getTimeString());
        assertEquals("to do someth", c1casted.getTaskString());
    }
    
    @Test
    public void testInterprete_createTask_time() {
        Command c1 = interpreter.interprete("+ task 10 22:00");
        assertEquals(CREATE_TASK, c1.type());
        
        CreateTaskCommand c1casted = (CreateTaskCommand) c1;
        assertEquals("10 22:00", c1casted.getTimeString());
        assertFalse(c1casted.hasTask());
    }
    
    @Test
    public void testInterprete_createTask_plusTimeAndTask() {
        Command c1 = interpreter.interprete("+ task +10 to do someth");
        assertEquals(CREATE_TASK, c1.type());
        
        CreateTaskCommand c1casted = (CreateTaskCommand) c1;
        assertEquals("+10", c1casted.getTimeString());
        assertEquals("to do someth", c1casted.getTaskString());
    }
    
    @Test
    public void testInterprete_createTask_plusTime() {
        Command c1 = interpreter.interprete("+ task +10");
        assertEquals(CREATE_TASK, c1.type());
        
        CreateTaskCommand c1casted = (CreateTaskCommand) c1;
        assertEquals("+10", c1casted.getTimeString());
        assertFalse(c1casted.hasTask());
    }
    
    @Test
    public void testInterprete_createTask_task() {
        Command c1 = interpreter.interprete("+ task to do someth");
        assertEquals(CREATE_TASK, c1.type());
        
        CreateTaskCommand c1casted = (CreateTaskCommand) c1;
        assertFalse(c1casted.hasTime());
        assertEquals("to do someth", c1casted.getTaskString());
    }
    
    @Test
    public void testInterprete_createTask_empty() {
        Command c1 = interpreter.interprete("+ task ");
        assertEquals(CREATE_TASK, c1.type());
        
        CreateTaskCommand c1casted = (CreateTaskCommand) c1;
        assertFalse(c1casted.hasTime());
        assertFalse(c1casted.hasTask());
    }
    
    @Test
    public void testInterprete_editBatch_undefined() {
        Command c = interpreter.interprete("edit batch");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_UNDEFINED, command.getTarget());
        assertFalse(command.isTargetDefined());
        assertFalse(command.hasName());
    }
    
    @Test
    public void testInterprete_editBatch_name() {
        Command c = interpreter.interprete("edit batch name");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_NAME, command.getTarget());
        assertTrue(command.isTargetDefined());
        assertFalse(command.hasName());
    }
    
    @Test
    public void testInterprete_editBatch_commands() {
        Command c = interpreter.interprete("change batch comm");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_COMMANDS, command.getTarget());
        assertTrue(command.isTargetDefined());
        assertFalse(command.hasName());
    }
    
    @Test
    public void testInterprete_editBatch_undefined_hasName() {
        Command c = interpreter.interprete("edit batch mysql");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_UNDEFINED, command.getTarget());
        assertFalse(command.isTargetDefined());
        assertTrue(command.hasName());
        assertEquals("mysql", command.getName());
    }
    
    @Test
    public void testInterprete_editBatch_name_hasName_v1() {
        Command c = interpreter.interprete("edit batch mysql name");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_NAME, command.getTarget());
        assertTrue(command.isTargetDefined());
        assertTrue(command.hasName());
        assertEquals("mysql", command.getName());
    }
    
    @Test
    public void testInterprete_editBatch_name_hasName_v2() {
        Command c = interpreter.interprete("edit batch name mysql");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_NAME, command.getTarget());
        assertTrue(command.isTargetDefined());
        assertTrue(command.hasName());
        assertEquals("mysql", command.getName());
    }
    
    
    @Test
    public void testInterprete_editBatch_commands_hasName_v1() {
        Command c = interpreter.interprete("change batch mysql comm");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_COMMANDS, command.getTarget());
        assertTrue(command.isTargetDefined());
        assertTrue(command.hasName());
        assertEquals("mysql", command.getName());
    }
    
    @Test
    public void testInterprete_editBatch_commands_hasName_v2() {
        Command c = interpreter.interprete("change batch comm mysql ");
        assertEquals(EDIT_BATCH, c.type());
        
        EditEntityCommand command = (EditEntityCommand) c;
        assertEquals(TARGET_COMMANDS, command.getTarget());
        assertTrue(command.isTargetDefined());
        assertTrue(command.hasName());
        assertEquals("mysql", command.getName());
    }
    
    @Test
    public void testInterprete_createPageDir() {
        Command c1 = interpreter.interprete("+ page dir devtools bookm");
        Command c2 = interpreter.interprete("+ page dir panel devtools");
        
        assertEquals(CREATE_PAGE_DIR, c1.type());
        assertEquals(CREATE_PAGE_DIR, c2.type());
        
        CreateWebDirectoryCommand c1casted = (CreateWebDirectoryCommand) c1;
        CreateWebDirectoryCommand c2casted = (CreateWebDirectoryCommand) c2;
        
        assertEquals("devtools", c1casted.getName());
        assertEquals("devtools", c2casted.getName());
        
        assertEquals(BOOKMARKS, c1casted.getPlacement());
        assertEquals(WEBPANEL, c2casted.getPlacement());
        
    }
    
    @Test
    public void testInterprete_createDir() {
        Command c1 = interpreter.interprete("+ dir devtools bookm");
        Command c2 = interpreter.interprete("+ dir panel devtools");
        
        assertEquals(CREATE_PAGE_DIR, c1.type());
        assertEquals(CREATE_PAGE_DIR, c2.type());
        
        CreateWebDirectoryCommand c1casted = (CreateWebDirectoryCommand) c1;
        CreateWebDirectoryCommand c2casted = (CreateWebDirectoryCommand) c2;
        
        assertEquals("devtools", c1casted.getName());
        assertEquals("devtools", c2casted.getName());
        
        assertEquals(BOOKMARKS, c1casted.getPlacement());
        assertEquals(WEBPANEL, c2casted.getPlacement());
        
    }
    
    @Test
    public void testInterprete_createDir_onlyName() {
        Command c1 = interpreter.interprete("+ page dir devtools");
        Command c2 = interpreter.interprete("+ dir devtools");
        
        assertEquals(CREATE_PAGE_DIR, c1.type());
        assertEquals(CREATE_PAGE_DIR, c2.type());
        
        CreateWebDirectoryCommand c1casted = (CreateWebDirectoryCommand) c1;
        CreateWebDirectoryCommand c2casted = (CreateWebDirectoryCommand) c2;
        
        assertEquals("devtools", c1casted.getName());
        assertEquals("devtools", c2casted.getName());
        
        assertFalse(c1casted.hasPlace());
        assertFalse(c2casted.hasPlace());
        
    }
    
    @Test
    public void testInterprete_createDir_onlyPlace() {
        Command c1 = interpreter.interprete("+ page dir bookmark");
        Command c2 = interpreter.interprete("+ dir panel");
        
        assertEquals(CREATE_PAGE_DIR, c1.type());
        assertEquals(CREATE_PAGE_DIR, c2.type());
        
        CreateWebDirectoryCommand c1casted = (CreateWebDirectoryCommand) c1;
        CreateWebDirectoryCommand c2casted = (CreateWebDirectoryCommand) c2;
        
        assertEquals(BOOKMARKS, c1casted.getPlacement());
        assertEquals(WEBPANEL, c2casted.getPlacement());
        
        assertFalse(c1casted.hasName());
        assertFalse(c2casted.hasName());        
    }
    
    @Test
    public void testInterprete_createDir_noPlaceNoName() {
        Command c1 = interpreter.interprete("+ page dir");
        Command c2 = interpreter.interprete("+ dir");
        
        assertEquals(CREATE_PAGE_DIR, c1.type());
        assertEquals(CREATE_PAGE_DIR, c2.type());
        
        CreateWebDirectoryCommand c1casted = (CreateWebDirectoryCommand) c1;
        CreateWebDirectoryCommand c2casted = (CreateWebDirectoryCommand) c2;
        
        assertFalse(c1casted.hasPlace());
        assertFalse(c2casted.hasPlace());
        
        assertFalse(c1casted.hasName());
        assertFalse(c2casted.hasName());        
    }
    
    @Test
    public void testInterprete_editPage() {
        Command c = interpreter.interprete("edit page ");
        assertEquals(EDIT_PAGE, c.type());
        EditEntityCommand com = (EditEntityCommand) c;
        assertFalse(com.hasName());
        assertFalse(com.isTargetDefined());
    }
    
    @Test
    public void testInterprete_editPage_name_target() {
        Command c = interpreter.interprete("edit page google name");
        assertEquals(EDIT_PAGE, c.type());
        
        EditEntityCommand com = (EditEntityCommand) c;
        assertEquals("google", com.getName());
        assertEquals(TARGET_NAME, com.getTarget());
    }
    
    @Test
    public void testInterprete_editPage_nameOnly() {
        Command c = interpreter.interprete("edit page google");
        assertEquals(EDIT_PAGE, c.type());
        
        EditEntityCommand com = (EditEntityCommand) c;
        assertEquals("google", com.getName());
        assertFalse(com.isTargetDefined());
    }
    
    @Test
    public void testInterprete_editPage_targetOnly() {
        Command c = interpreter.interprete("edit page url");
        assertEquals(EDIT_PAGE, c.type());
        
        EditEntityCommand com = (EditEntityCommand) c;
        assertEquals(TARGET_URL, com.getTarget());
        assertFalse(com.hasName());
    }
    
    @Test
    public void testInterprete_editDir_1() {
        Command c = interpreter.interprete("edit page dir");
        assertEquals(EDIT_PAGE_DIR, c.type());
        EditEntityCommand com = (EditEntityCommand) c;
        assertFalse(com.hasName());
        assertFalse(com.isTargetDefined());
    }
    
    @Test
    public void testInterprete_editDir_2() {
        Command c = interpreter.interprete("edit dir");
        assertEquals(EDIT_PAGE_DIR, c.type());
        EditEntityCommand com = (EditEntityCommand) c;
        assertFalse(com.hasName());
        assertFalse(com.isTargetDefined());
    }
    
    @Test
    public void testInterprete_editDir_name_target() {
        Command c = interpreter.interprete("edit page dir common name");
        Command c1 = interpreter.interprete("edit directory common place");
        assertEquals(EDIT_PAGE_DIR, c.type());
        assertEquals(EDIT_PAGE_DIR, c1.type());
        
        EditEntityCommand com = (EditEntityCommand) c;
        EditEntityCommand com1 = (EditEntityCommand) c1;
        
        assertEquals("common", com.getName());
        assertEquals(TARGET_NAME, com.getTarget());
        assertEquals(TARGET_PLACE, com1.getTarget());
    }
    
    @Test
    public void testInterprete_editDir_name_target_1() {
        Command c1 = interpreter.interprete("edit directory order common");
        assertEquals(EDIT_PAGE_DIR, c1.type());
        EditEntityCommand com1 = (EditEntityCommand) c1;
        assertEquals("common", com1.getName());
        assertEquals(TARGET_ORDER, com1.getTarget());
    }
        
    @Test
    public void testInterprete_editDir_nameOnly() {
        Command c = interpreter.interprete("edit page dir place");        
        assertEquals(EDIT_PAGE_DIR, c.type());        
        
        EditEntityCommand com = (EditEntityCommand) c;
        assertFalse(com.hasName());
        assertEquals(TARGET_PLACE, com.getTarget());
    }
    
    @Test
    public void testInterprete_editLocation_name_targetName() {
        Command c = interpreter.interprete("edit location boo name");        
        assertEquals(EDIT_LOCATION, c.type());      
        
        EditEntityCommand c1 = (EditEntityCommand) c;
        assertEquals("boo", c1.getName());
        assertEquals(TARGET_NAME, c1.getTarget());
    }  
    
    @Test
    public void testInterprete_editLocation_name_targetPath() {
        Command c = interpreter.interprete("edit location boo path");        
        assertEquals(EDIT_LOCATION, c.type());      
        
        EditEntityCommand c1 = (EditEntityCommand) c;
        assertEquals("boo", c1.getName());
        assertEquals(TARGET_PATH, c1.getTarget());
    }  
    
    @Test
    public void testInterprete_editLocation_onlyTargetName() {
        Command c = interpreter.interprete("edit location name");        
        assertEquals(EDIT_LOCATION, c.type());      
        
        EditEntityCommand c1 = (EditEntityCommand) c;
        assertFalse(c1.hasName());
        assertEquals(TARGET_NAME, c1.getTarget());
    }  
    
    @Test
    public void testInterprete_editLocation_onlyTargetPath() {
        Command c = interpreter.interprete("edit location path");        
        assertEquals(EDIT_LOCATION, c.type());      
        
        EditEntityCommand c1 = (EditEntityCommand) c;
        assertFalse(c1.hasName());
        assertEquals(TARGET_PATH, c1.getTarget());
    }  
    
    @Test
    public void testInterprete_editLocation_onlyName() {
        Command c = interpreter.interprete("edit location books");        
        assertEquals(EDIT_LOCATION, c.type());      
        
        EditEntityCommand c1 = (EditEntityCommand) c;
        assertFalse(c1.isTargetDefined());
        assertEquals("books", c1.getName());
    } 
    
    @Test
    public void testInterprete_editTask_textAndTime() {
        Command c = interpreter.interprete("edit task 10:10 task text");        
        assertEquals(EDIT_TASK, c.type());        
        
        TimeEntityEditCommand c1 = (TimeEntityEditCommand) c;
        assertEquals("10:10", c1.getTime());
        assertEquals("task text", c1.getText());
    }
    
    @Test
    public void testInterprete_editTask_textAndTime_1() {
        Command c = interpreter.interprete("edit task 12 10:15 task text");        
        assertEquals(EDIT_TASK, c.type());        
        
        TimeEntityEditCommand c1 = (TimeEntityEditCommand) c;
        assertEquals("12 10:15", c1.getTime());
        assertEquals("task text", c1.getText());
    }
    
    @Test
    public void testInterprete_editTask_text() {
        Command c = interpreter.interprete("edit task task text");        
        assertEquals(EDIT_TASK, c.type());        
        
        TimeEntityEditCommand c1 = (TimeEntityEditCommand) c;
        assertFalse(c1.hasTime());
        assertEquals("task text", c1.getText());
    }
    
    @Test
    public void testInterprete_editTask_timet() {
        Command c = interpreter.interprete("edit task 10:15");        
        assertEquals(EDIT_TASK, c.type());        
        
        TimeEntityEditCommand c1 = (TimeEntityEditCommand) c;
        assertFalse(c1.hasText());
        assertEquals("10:15", c1.getTime());
    }
    
    @Test
    public void testInterprete_editTask_empty() {
        Command c = interpreter.interprete("edit task");        
        assertEquals(EDIT_TASK, c.type());        
        
        TimeEntityEditCommand c1 = (TimeEntityEditCommand) c;
        assertFalse(c1.hasText());
        assertFalse(c1.hasTime());
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
        
        SingleStringCommand c1 = (SingleStringCommand) c;
        assertEquals("books", c1.getArg());
    }  
    
    @Test
    public void testInterprete_listPath() {
        Command c = interpreter.interprete("list books/tech/java");
        assertEquals(LIST_PATH, c.type()); 
        
        SingleStringCommand c1 = (SingleStringCommand) c;
        assertEquals("books/tech/java", c1.getArg());
    }  
}