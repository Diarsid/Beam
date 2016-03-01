/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

import diarsid.beam.core.modules.tasks.TaskManagerModuleWorker;
import diarsid.beam.core.modules.tasks.TaskTimeFormatter;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DaoTasks;

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
        TaskTimeFormatter formatter = mock(TaskTimeFormatter.class);
        Object execLock = mock(Object.class);
        Object notifyLock = mock(Object.class);
        ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);
        
        when(data.getTasksDao()).thenReturn(dao);
        LocalDateTime future = LocalDateTime.of(2017, 12, 30, 23, 59, 59);
        when(dao.getFirstTaskTime()).thenReturn(future);
        
        taskManager = new TaskManagerModuleWorker(io, dao, formatter, execLock, notifyLock, executor);
        
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