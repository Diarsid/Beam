/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.Tasks;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriodsParser;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.Integer.MIN_VALUE;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.domain.entities.TaskRepeat.HOURLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.NO_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.YEARLY_REPEAT;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.allowedTimePeriodsParser;

/**
 *
 * @author Diarsid
 */
public class H2DaoTasksV2Test {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoTasksV2Test.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;
    private static DaoTasks dao;
    private static Initiator initiator;

    public H2DaoTasksV2Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        initiator = new Initiator(23, IN_MACHINE);
        dataBase = new H2TestDataBase();
        ioEngine = mock(InnerIoEngine.class);
        dao = new H2DaoTasksV2(dataBase, ioEngine);
        
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
        
        List<String> reports = actuator.actuateAndGetReport();
        reports.stream().forEach(report -> logger.info(report));
        assertEquals(reports.size(), ((SqlDataBaseModel) dataBaseModel).objects().size());
    }
    
    @Before
    public void setupCase() {
        List<Task> tasks = new ArrayList<>();
        
        Task t1 = Tasks.restoreTask(MIN_VALUE, 
                NO_REPEAT, 
                now().minusDays(1).withMinute(0).withSecond(0).withNano(0), 
                false, 
                "", 
                "", 
                "line 1 \\ line 2 \\ line 3");
        
        Task t8 = Tasks.restoreTask(MIN_VALUE, 
                NO_REPEAT, 
                now().withSecond(0).withNano(0), 
                true, 
                "", 
                "", 
                "task to invoke right now!");
        
        Task t2 = Tasks.restoreTask(MIN_VALUE, 
                NO_REPEAT, 
                now().minusHours(3).withMinute(0).withSecond(0).withNano(0), 
                false, 
                "", 
                "", 
                "line 1 \\ line 2 \\ line 3");
        
        Task t3 = Tasks.restoreTask(MIN_VALUE, 
                NO_REPEAT, 
                now().plusHours(1).withMinute(0).withSecond(0).withNano(0), 
                true, 
                "", 
                "", 
                "to do something");
        
        Task t4 = Tasks.restoreTask(
                MIN_VALUE, 
                HOURLY_REPEAT, 
                now().plusHours(2).withMinute(0).withSecond(0).withNano(0), 
                true, 
                "1 2 3 4 5", 
                "9 10 11 12 13 14 15 16 17", 
                "task to perform every working hour \\ to remember something");
        
        Task t5  = Tasks.restoreTask(
                MIN_VALUE, 
                YEARLY_REPEAT, 
                now().plusWeeks(2).withMinute(0).withSecond(0).withNano(0), 
                true, 
                "", 
                "", 
                "John's birthday in 5 days");
        
        Task t6  = Tasks.restoreTask(MIN_VALUE, 
                NO_REPEAT, 
                now().plusWeeks(2).plusDays(1).withMinute(0).withSecond(0).withNano(0), 
                true, 
                "", 
                "", 
                "Single event");
        
        Task t7  = Tasks.restoreTask(
                MIN_VALUE, 
                YEARLY_REPEAT, 
                now().plusMonths(2).withMinute(0).withSecond(0).withNano(0), 
                true, 
                "", 
                "", 
                "some other event");
        
        tasks.addAll(asList(t1, t2, t3, t4, t5, t6, t7, t8));
        
        tasks.forEach(task -> dao.saveTask(initiator, task));
        assertEquals(8, dataBase.countRowsInTable("tasks"));
    }
    
    @After
    public void clearCase() {
        try {
            dataBase.transactionFactory()
                    .createDisposableTransaction()
                    .doUpdate("DELETE FROM tasks");
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logger.error("clearing case error", ex);
        }
    }

    /**
     * Test of getTimeOfFirstActiveTask method, of class H2DaoTasks.
     */
    @Test
    public void testGetTimeOfFirstActiveTask() {
        LocalDateTime expected = now().withSecond(0).withNano(0);
        Optional<LocalDateTime> optTime = dao.getTimeOfFirstActiveTask(initiator);
        assertTrue(optTime.isPresent());
        assertEquals(expected, optTime.get());
    }

    /**
     * Test of getActiveTasksOfTypeBetweenDates method, of class H2DaoTasks.
     */
    @Test
    public void testGetActiveTasksOfTypeBetweenDates() {
        LocalDateTime from = now().plusWeeks(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime to = now().plusWeeks(3).withMinute(0).withSecond(0).withNano(0);
        List<Task> tasks = dao.getActiveTasksOfTypeBetweenDates(initiator, from, to, YEARLY_REPEAT);
        assertEquals(1, tasks.size());
        assertEquals("", tasks.get(0).days());
    }
    
    @Test
    public void testGetActiveTasksOfTypeBetweenDates_2() {
        LocalDateTime from = now().plusWeeks(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime to = now().plusWeeks(3).withMinute(0).withSecond(0).withNano(0);
        List<Task> tasks = dao.getActiveTasksOfTypeBetweenDates(initiator, from, to, YEARLY_REPEAT, NO_REPEAT, HOURLY_REPEAT);
        assertEquals(2, tasks.size());
    }

    /**
     * Test of getActiveTasksBeforeTime method, of class H2DaoTasks.
     */
    @Test
    public void testGetActiveTasksBeforeTime() {
        List<Task> tasks = dao.getActiveTasksBeforeTime(initiator, now());
        assertEquals(1, tasks.size());
    }

    /**
     * Test of updateTasks method, of class H2DaoTasks.
     */
    @Test
    public void testUpdateTasks() {
        List<Task> tasks = dao.getActiveTasksBeforeTime(initiator, now());
        assertEquals(1, tasks.size());
        tasks.stream().forEach(Task::switchTime);
        
        boolean updated = dao.updateTasks(initiator, tasks);
        assertTrue(updated);
        
        LocalDateTime expected = now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
        Optional<LocalDateTime> newTime = dao.getTimeOfFirstActiveTask(initiator);
        assertTrue(newTime.isPresent());
        assertEquals(expected, newTime.get());
    }

    /**
     * Test of saveTask method, of class H2DaoTasks.
     */
    @Test
    public void testSaveTask() {
        // 
    }

    /**
     * Test of deleteTaskById method, of class H2DaoTasks.
     */
    @Test
    public void testDeleteTaskById() {
        List<Task> tasks1 = dao.findTasksByTextPattern(initiator, "to invoke ri");
        assertEquals(1, tasks1.size());
        boolean deleted = dao.deleteTaskById(initiator, tasks1.get(0).id());
        assertTrue(deleted);
        assertEquals(8 - 1, dataBase.countRowsInTable("tasks"));
    }

    /**
     * Test of editTaskText method, of class H2DaoTasks.
     */
    @Test
    public void testEditTaskText() {
        List<Task> tasks = dao.findTasksByTextPattern(initiator, "workremember");
        assertEquals(1, tasks.size());
        
        List<String> newText = asList("new text", "instead of the old one");
        
        dao.editTaskText(initiator, tasks.get(0).id(), newText);
        
        List<Task> tasksAfter = dao.findTasksByTextPattern(initiator, "instol");
        assertEquals(5, tasksAfter.size());
    }

    /**
     * Test of editTaskTime method, of class H2DaoTasks.
     */
    @Test
    public void testEditTaskTime_3args() {
        List<Task> tasks = dao.findTasksByTextPattern(initiator, "johnbirth");
        assertEquals(2, tasks.size());
        LocalDateTime newTime = now().plusWeeks(2).plusDays(2).withMinute(0).withSecond(0).withNano(0);
        
        dao.editTaskTime(initiator, tasks.get(0).id(), newTime);
        
        List<Task> tasksAfter = dao.findTasksByTextPattern(initiator, "johnbirth");
        assertEquals(2, tasksAfter.size());
        assertEquals(newTime, tasksAfter.get(0).time());
    }

    /**
     * Test of editTaskTime method, of class H2DaoTasks.
     */
    @Test
    public void testEditTaskTime_4args() {
        List<Task> tasks = dao.findTasksByTextPattern(initiator, "everywork");
        assertEquals(1, tasks.size());
        LocalDateTime newTime = now().plusWeeks(2).plusDays(2).withMinute(0).withSecond(0).withNano(0);
        AllowedTimePeriodsParser parser = allowedTimePeriodsParser();
        AllowedTimePeriod timePeriod = parser.parseAllowedDays("1-4");
        timePeriod.merge(parser.parseAllowedHours("9-16"));
        
        dao.editTaskTime(initiator, tasks.get(0).id(), newTime, timePeriod);
        
        List<Task> tasksAfter = dao.findTasksByTextPattern(initiator, "everywork");
        assertEquals(tasks.get(0).id(), tasksAfter.get(0).id());
        assertEquals(1, tasksAfter.size());
        assertEquals(newTime, tasksAfter.get(0).time());
        assertEquals("1 2 3 4", tasksAfter.get(0).days());
        assertEquals("9 10 11 12 13 14 15", tasksAfter.get(0).hours());
    }

    /**
     * Test of findTasksByTextPattern method, of class H2DaoTasks.
     */
    @Test
    public void testFindTasksByTextPattern() {
        List<Task> tasks1 = dao.findTasksByTextPattern(initiator, "to invoke ri");
        assertEquals(1, tasks1.size());
        
        List<Task> tasks2 = dao.findTasksByTextPattern(initiator, "workremember");
        assertEquals(1, tasks2.size());
        assertEquals("1 2 3 4 5", tasks2.get(0).days());
        
        List<Task> tasks3 = dao.findTasksByTextPattern(initiator, "somthing");
        assertEquals(3, tasks3.size());
    }

}