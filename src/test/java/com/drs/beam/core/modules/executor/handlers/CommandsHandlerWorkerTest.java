/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.executor.handlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.drs.beam.core.entities.StoredExecutorCommand;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.data.dao.commands.CommandsDao;

/**
 *
 * @author Diarsid ssds
 */
public class CommandsHandlerWorkerTest {
    InnerIOModule io;
    CommandsDao dao;
    List<StoredExecutorCommand> commands = new ArrayList<>();
    List<StoredExecutorCommand> oneCommand = new ArrayList<>();
    
    CommandsHandlerWorker handler;

    @Before
    @Test
    public void init() throws SQLException{
        StoredExecutorCommand comm1 = mock(StoredExecutorCommand.class);
        StoredExecutorCommand comm2 = mock(StoredExecutorCommand.class);
        StoredExecutorCommand comm3 = mock(StoredExecutorCommand.class);
        commands.add(comm1);
        commands.add(comm2);
        commands.add(comm3);
        oneCommand.add(comm1);
        
        io = mock(InnerIOModule.class);
        dao = mock(CommandsDao.class);
        
        when(dao.getAllCommands()).thenReturn(commands);
        when(dao.removeCommand("any_command")).thenReturn(true);
        when(dao.getCommandsByName("comm1")).thenReturn(oneCommand);
        when(dao.getCommandsByName("any_name")).thenReturn(new ArrayList<>());  
        
        handler = new CommandsHandlerWorker(dao, io);
    }

    @Test
    public void testNewCommand() {
        String[] commLines = {"saasds", "sfgsd", "dsds"};
        handler.newCommand(Arrays.asList(commLines), "commX");
    }
    
    @Test
    public void testNewCommandForbiddenInput() {
        String[] commLines = {"saasds", "call", "dsds"};
        handler.newCommand(Arrays.asList(commLines), "commX");
        verify(io).reportMessage("'call' is not permitted to use.",
                        "It can cause cyclical execution.");
    }

    @Test
    public void testGetAllCommands() {
        handler.getAllCommands();
    }

    @Test
    public void testGetCommands() {
        handler.getCommands("comm1");
    }

    @Test
    public void testGetCommand() {
        handler.getCommand("comm1");
    }

}