/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.TimeMessage;
import diarsid.beam.core.control.io.commands.CreateEntityCommand;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateTaskCommand;
import diarsid.beam.core.domain.entities.Task;

/**
 *
 * @author Diarsid
 */
public interface TasksKeeper {
    
    List<Task> getExpiredTasks(
            Initiator initiator);
    
    List<Task> getFirstTasks(
            Initiator initiator);
    
    List<TimeMessage> getCalendarTasksForNextMonth(
            Initiator initiator, LocalDateTime nextMonthBeginning);
    
    List<TimeMessage> getCalendarTasksForNextWeek(
            Initiator initiator, LocalDateTime nextWeekBeginning);
    
    Optional<Long> getInactivePeriodMinutes(
            Initiator initiator);
    
    boolean updateTasks(
            Initiator initiator, List<Task> tasks);
    
    Optional<LocalDateTime> getTimeOfFirstTask(
            Initiator initiator);
    
    boolean createTask(
            Initiator initiator, CreateTaskCommand command);
    
    boolean createReminder(
            Initiator initiator, CreateEntityCommand command);
    
    boolean createEvent(
            Initiator initiator, CreateEntityCommand command);
    
    boolean deleteTask(
            Initiator initiator, RemoveEntityCommand command);
    
    boolean editTask(
            Initiator initiator, EditEntityCommand command);
    
    List<Task> findTasks(
            Initiator initiator, FindEntityCommand findEntityCommand);
}
