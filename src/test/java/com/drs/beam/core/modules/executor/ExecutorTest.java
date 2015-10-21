/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public class ExecutorTest {
    Executor exec;
    
    InnerIOModule io;
    LocationsHandler locHandler;
    CommandsHandler comHandler;
    OS os;
    
    @Test
    @Before
    public void init(){
        
        io = mock(InnerIOModule.class);
        locHandler = mock(LocationsHandler.class);
        comHandler = mock(CommandsHandler.class);
        os = mock(OS.class);
        
        exec = new Executor(io, locHandler, comHandler, os);
    }
    
    @Test
    public void testOpenWithLocation() {
        String[] a = {"open", "location"};
        List<String> commandParams = Arrays.asList(a);
        
        // stub appropriate methods
        Location loc = mock(Location.class);
        when(locHandler.getLocation("location")).thenReturn(loc);
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locHandler).getLocation("location");
        verify(os).openLocation(loc);
    }
    
    @Test
    public void testOpenWithFileInLocation() {
        String[] a = {"open", "file", "in", "location"};
        List<String> commandParams = Arrays.asList(a);
        
        // stub appropriate methods
        Location loc = mock(Location.class);
        when(locHandler.getLocation("location")).thenReturn(loc);
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locHandler).getLocation("location");
        verify(os).openFileInLocation("file", loc);
    }
    
    @Test
    public void testOpenWithFileInLocationWithProgram() {
        String[] a = {"open", "file", "in", "location", "with", "program"};
        List<String> commandParams = Arrays.asList(a);
        
        // stub appropriate methods
        Location loc = mock(Location.class);
        when(locHandler.getLocation("location")).thenReturn(loc);
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locHandler).getLocation("location");
        verify(os).openFileInLocationWithProgram("file", loc, "program");
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
        
        verify(comHandler).getCommand("command1");
        verify(comHandler).getCommand("command2");
    }

    @Test
    public void testNewCommand() {
        String[] a = {"open location", "run program"};
        List<String> commands = Arrays.asList(a);
        
        exec.newCommand(commands, "command");
        
        verify(comHandler).newCommand(commands, "command");
    }

    @Test
    public void testNewLocation() {
        String locPath = "path/to/dir";
        String locName = "dir";
                
        when(os.checkIfDirectoryExists(locPath)).thenReturn(true);
        
        exec.newLocation(locPath, locName);
        
        verify(locHandler).newLocation(locPath, locName);
    }

    @Test
    public void testDeleteCommand() {
        String commandName = "comm_name";
        
        exec.deleteCommand(commandName);
        
        verify(comHandler).deleteCommand(commandName);
    }

    @Test
    public void testDeleteLocation() {
        String locationName = "loc_name";
        
        exec.deleteLocation(locationName);
        
        verify(locHandler).deleteLocation(locationName);
    }

    @Test
    public void testGetAllLocations() {
        exec.getAllLocations();
        
        verify(locHandler).getAllLocations();
    }

    @Test
    public void testGetAllCommands() {
        exec.getAllCommands();
        
        verify(comHandler).getAllCommands();
    }

    @Test
    public void testListLocationContent() {
        String locationName = "loc_name";
        String locationPath = "path/to/some/dir";
        
        List<String> locContent = new ArrayList<>();
        locContent.add("file_1");
        locContent.add("file_2");
        locContent.add("directory");
        
        Location loc = new Location(locationName, locationPath);
        when(locHandler.getLocation(locationName)).thenReturn(loc);
        when(os.getLocationContent(loc)).thenReturn(locContent);
        
        List<String> locContentCopy = exec.listLocationContent(locationName);
        
        Assert.assertEquals(locContentCopy, locContent);
        
        verify(locHandler).getLocation(loc.getName());
        verify(os).getLocationContent(loc);
    }

    @Test
    public void testGetLocations() {
        exec.getLocations("some_loc_name");
        
        verify(locHandler).getLocations("some_loc_name");
    }

    @Test
    public void testGetCommands() {
        exec.getCommands("comm_name");
        
        verify(comHandler).getCommands("comm_name");
    }
    
}
