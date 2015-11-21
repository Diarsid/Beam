/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.tasks;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoTasks;

/**
 *
 * @author Diarsid
 */
public class TaskManagerTest {
    
    TaskManagerModuleWorker taskManager;

    @Before
    @Test
    public void testTaskManagerCreation() throws SQLException{
        IoInnerModule io = mock(IoInnerModule.class);
        DataModule data = mock(DataModule.class);
        DaoTasks dao = mock(DaoTasks.class);
        
        when(data.getTasksDao()).thenReturn(dao);
        LocalDateTime future = LocalDateTime.of(2017, 12, 30, 23, 59, 59);
        when(dao.getFirstTaskTime()).thenReturn(future);
        
        taskManager = new TaskManagerModuleWorker(io, data);
        
        assertEquals(future, taskManager.getFirstTaskTime());
    }
    
    @Test
    public void testGetFirstTaskTime() {
        assertNotNull(taskManager.getFirstTaskTime());
    }

    @Test
    public void testIsAnyTasks() {
    }

    @Test
    public void testPerformFirstTask() {
    }

    @Test
    public void testCreateNewTask() {
    }

    @Test
    public void testGetFirstAlarmTime() {
    }

    @Test
    public void testGetFutureTasks() {
    }

    @Test
    public void testGetPastTasks() {
    }

    @Test
    public void testGetFirstTask() {
    }

    @Test
    public void testDeleteTaskByText() {
    }

    @Test
    public void testRemoveAllTasks() {
    }

    @Test
    public void testRemoveAllFutureTasks() {
    }

    @Test
    public void testRemoveAllPastTasks() {
    }

}