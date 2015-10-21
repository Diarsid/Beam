/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.data.base.DataBase;
import com.drs.beam.core.modules.data.dao.commands.CommandsDao;
import com.drs.beam.core.modules.data.dao.locations.LocationsDao;
import com.drs.beam.core.modules.data.dao.tasks.TasksDao;
import com.drs.beam.core.modules.exceptions.ModuleInitializationException;

/**
 *
 * @author Diarsid
 */
public class DaoProviderTest {
    
    InnerIOModule io;
    DataBase db;
    DataBase falseDb;
    
    DaoProvider daoProvider;
    DaoProvider falseDaoProvider;
    
    public DaoProviderTest() {
    }
    
    @Test
    @Before
    public void init() throws Exception{
        io = mock(InnerIOModule.class);
        
        db = mock(DataBase.class);
        when(db.getName()).thenReturn("H2");
        when(db.connect()).thenReturn(mock(Connection.class));
        
        falseDb = mock(DataBase.class);
        when(falseDb.getName()).thenReturn("false_DB_name");
        when(db.connect()).thenReturn(null);
    }
    
    @Test
    @Before
    public void testDaoProviderCreation(){
        daoProvider = new DaoProvider(io, db);
        falseDaoProvider = new DaoProvider(io, falseDb);
    }
    
    @Test
    public void testCreateTasksDao() {
        TasksDao tasksDao = daoProvider.createTasksDao(db);
    }

    @Test
    public void testCreateLocationsDao() {
        LocationsDao locationsDao = daoProvider.createLocationsDao(db);
    }

    @Test
    public void testCreateCommandsDao() {
        CommandsDao commandsDao = daoProvider.createCommandsDao(db);
    }
    
    @Test
    public void testFalseCreateTasksDao(){
        try{
            TasksDao tasksDao = falseDaoProvider.createTasksDao(falseDb);
            fail("Expected: ModuleInitializationException");
        } catch (ModuleInitializationException mie){
            verify(io).reportExceptionAndExitLater(Matchers.isA(ClassNotFoundException.class), 
                    Matchers.eq("DaoProvider: Dao implementation class not found by its name."),
                    Matchers.eq("Programm will be closed."));
        }
        
    }
    
    @Test 
    public void testFalseCreateCommandsDao(){        
        try{
            LocationsDao locationsDao = falseDaoProvider.createLocationsDao(falseDb);
            fail("Expected: ModuleInitializationException");
        } catch (ModuleInitializationException mie){
            verify(io).reportExceptionAndExitLater(Matchers.isA(ClassNotFoundException.class), 
                    Matchers.eq("DaoProvider: Dao implementation class not found by its name."),
                    Matchers.eq("Programm will be closed."));
        }
    }
    
    @Test 
    public void testFalseCreateLocationsDao(){        
        try{
            CommandsDao commandsDao = falseDaoProvider.createCommandsDao(falseDb);
            fail("Expected: ModuleInitializationException");
        } catch (ModuleInitializationException mie){
            verify(io).reportExceptionAndExitLater(Matchers.isA(ClassNotFoundException.class), 
                    Matchers.eq("DaoProvider: Dao implementation class not found by its name."),
                    Matchers.eq("Programm will be closed."));
        }
    }
}
