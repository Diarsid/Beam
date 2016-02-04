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
import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoCommands;
import com.drs.beam.core.modules.data.DaoLocations;

/**
 *
 * @author Diarsid
 */
public class ExecutorTest {
    ExecutorModuleWorker exec;
    
    IoInnerModule io;
    DaoLocations locDao;
    DaoCommands comDao;
    OS os;
    IntelligentResolver intell;
    
    @Test
    @Before
    public void init(){
        
        io = mock(IoInnerModule.class);        
        locDao = mock(DaoLocations.class);
        comDao = mock(DaoCommands.class);
        DataModule data = mock(DataModule.class);
        when(data.getCommandsDao()).thenReturn(comDao);
        when(data.getLocationsDao()).thenReturn(locDao);
        os = mock(OS.class);
        intell = mock(IntelligentResolver.class);
        Location notes = mock(Location.class);
        
        exec = new ExecutorModuleWorker(io, data, intell, os, notes);
    }
    
    @Test
    public void testOpenWithLocation() {
        String[] a = {"open", "location"};
        List<String> commandParams = Arrays.asList(a);
        
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locDao).getLocationsByName("location");
    }
    
    @Test
    public void testOpenWithFileInLocation() {
        String[] a = {"open", "file", "in", "location"};
        List<String> commandParams = Arrays.asList(a);
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locDao).getLocationsByName("location");
    }
    
    @Test
    public void testOpenWithFileInLocationWithProgram() {
        String[] a = {"open", "file", "in", "location", "with", "program"};
        List<String> commandParams = Arrays.asList(a);
        
        
        // test invocation 
        exec.open(commandParams);
        
        // verify workflow
        verify(locDao).getLocationsByName("location");
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
        
        verify(comDao).getCommandsByName("command1");
        verify(comDao).getCommandsByName("command2");
    }

    @Test
    public void testNewCommand() {
        String[] a = {"open location", "run program"};
        List<String> commands = Arrays.asList(a);
        
        StoredExecutorCommand command = new StoredExecutorCommand("command", commands);
        exec.newCommand(commands, "command");
        
        verify(comDao).saveNewCommand(command);
    }


    @Test
    public void testDeleteCommand() {
        String commandName = "comm_name";
        
        exec.deleteCommand(commandName);
        
        verify(comDao).removeCommand(commandName);
    }

    @Test
    public void testGetAllCommands() {
        exec.getAllCommands();
        
        verify(comDao).getAllCommands();
    }

    @Test
    public void testGetCommands() {
        exec.getCommands("comm_name");
        
        verify(comDao).getCommandsByName("comm_name");
    }
    
}
