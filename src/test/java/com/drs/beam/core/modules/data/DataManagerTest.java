/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.drs.beam.core.modules.data.base.DataBase;
import com.drs.beam.core.modules.data.dao.commands.CommandsDao;
import com.drs.beam.core.modules.data.dao.locations.LocationsDao;
import com.drs.beam.core.modules.data.dao.tasks.TasksDao;

/**
 *
 * @author Diarsid
 */
public class DataManagerTest {
    
    DaoProvider daoProvider;
    DataBase dataBase;
    
    DataManager dataManager;
   
    @Test
    @Before
    public void init() throws SQLException{
        dataBase = mock(DataBase.class);
        when(dataBase.getName()).thenReturn("H2");
        when(dataBase.connect()).thenReturn(mock(Connection.class));
        
        daoProvider = mock(DaoProvider.class);
        when(daoProvider.createCommandsDao(dataBase)).thenReturn(mock(CommandsDao.class));
        when(daoProvider.createTasksDao(dataBase)).thenReturn(mock(TasksDao.class));
        when(daoProvider.createLocationsDao(dataBase)).thenReturn(mock(LocationsDao.class));
        
        dataManager = new DataManager(dataBase, daoProvider);
    }
    
    @Test
    public void testGetTasksDao() {
        dataManager.getTasksDao();
    }

    @Test
    public void testGetLocationsDao() {
        dataManager.getLocationsDao();
    }

    @Test
    public void testGetCommandsDao() {
        dataManager.getCommandsDao();
    }
    
}
