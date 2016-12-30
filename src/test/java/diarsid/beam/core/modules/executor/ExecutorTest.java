/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor;

import diarsid.beam.core.modules.executor.context.SmartAmbiguityResolver;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import diarsid.beam.core.domain.entities.Location;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.IoInnerModule;

import old.diarsid.beam.core.modules.data.DaoCommandsBatches;

import diarsid.beam.core.modules.data.DaoLocations;

import old.diarsid.beam.core.modules.data.HandlerLocations;
import old.diarsid.beam.core.modules.data.HandlerWebPages;

import diarsid.beam.core.domain.entities.Batch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Diarsid
 */
@Ignore
public class ExecutorTest {
    ExecutorModuleWorker exec;
    
    IoInnerModule io;
    HandlerLocations locHandler;
    DaoCommandsBatches comDao;
    OS os;
    SmartAmbiguityResolver intell;
    
    @Test
    @Before
    public void init(){
        
        io = mock(IoInnerModule.class);        
        DaoLocations dao = mock(DaoLocations.class);
        comDao = mock(DaoCommandsBatches.class);
        locHandler = mock(HandlerLocations.class);
        HandlerWebPages pagesHandler = mock(HandlerWebPages.class);
        DataModule data = mock(DataModule.class);
        when(data.getCommandsDao()).thenReturn(comDao);
        when(data.getWebPagesHandler()).thenReturn(pagesHandler);
        os = mock(OS.class);
        intell = mock(SmartAmbiguityResolver.class);
        Location notes = mock(Location.class);
        
        exec = null;
    }
    
    @Test
    public void testOpenWithLocation() {
        String[] a = {"open", "location"};
        List<String> commandParams = Arrays.asList(a);
        
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locHandler).getLocations("location");
    }
    
    @Test
    public void testOpenWithFileInLocation() {
        String[] a = {"open", "file", "in", "location"};
        List<String> commandParams = Arrays.asList(a);
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locHandler).getLocations("location");
    }
    
    @Test
    public void testOpenWithFileInLocationWithProgram() {
        String[] a = {"open", "file", "in", "location", "with", "program"};
        List<String> commandParams = Arrays.asList(a);
        
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locHandler).getLocations("location");
    }

    @Test
    public void testRun() {
        String[] a = {"run", "program1", "program2"};
        List<String> commandParams = Arrays.asList(a);
        
        // test invocation
        exec.run(commandParams);
        
        verify(os).runProgram("program1");
        verify(os).runProgram("program2");        
    }

    @Test
    public void testStart() {
        String[] a = {"start", "program1"};
        List<String> commandParams = Arrays.asList(a);
        
        exec.start(commandParams);
        
        verify(os).runProgram("program1-start");
    }
    
    @Test
    public void testStartWithUnrecognizableCommandMessage() {
        String[] a = {"start"};
        List<String> commandParams = Arrays.asList(a);
        
        exec.start(commandParams);
        
        verify(io).reportMessage("Unrecognizable command.");
    }

    @Test
    public void testStop() {
        String[] a = {"stop", "program1"};
        List<String> commandParams = Arrays.asList(a);
        
        exec.stop(commandParams);
        
        verify(os).runProgram("program1-stop");
    }
    
    @Test
    public void testStopWithUnrecognizableCommandMessage() {
        String[] a = {"stop"};
        List<String> commandParams = Arrays.asList(a);
        
        exec.start(commandParams);
        
        verify(io).reportMessage("Unrecognizable command.");
    }

    @Test
    public void testCall() {
        String[] a = {"call", "command1", "command2"};
        List<String> commandParams = Arrays.asList(a);
        
        exec.call(commandParams);
        
        verify(comDao).getBatchesByName("command1");
        verify(comDao).getBatchesByName("command2");
    }

    @Test
    public void testNewCommand() {
        String[] a = {"open location", "run program"};
        List<String> commands = Arrays.asList(a);
        
        Batch command = new Batch("command", commands);
        exec.newBatch(commands, "command");
        
        verify(comDao).saveNewBatch(command);
    }


    @Test
    public void testDeleteCommand() {
        String commandName = "comm_name";
        
        exec.deleteBatch(commandName);
        
        verify(comDao).removeBatch(commandName);
    }

    @Test
    public void testGetAllCommands() {
        exec.getAllBatches();
        
        verify(comDao).getAllBatches();
    }

    @Test
    public void testGetCommands() {
        exec.getBathesByName("comm_name");
        
        verify(comDao).getBatchesByName("comm_name");
    }
    
}
