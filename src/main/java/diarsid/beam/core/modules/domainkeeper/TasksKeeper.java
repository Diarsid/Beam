/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.base.control.flow.ValueOperation;

/**
 *
 * @author Diarsid
 */
public interface TasksKeeper {
    
    List<Task> getPastActiveTasks(
            Initiator initiator);
    
    List<TaskMessage> getCalendarTasksForNextMonth(
            Initiator initiator, LocalDateTime nextMonthBeginning);
    
    List<TaskMessage> getCalendarTasksForNextWeek(
            Initiator initiator, LocalDateTime nextWeekBeginning);
    
    Optional<Long> getInactivePeriodMinutes(
            Initiator initiator);
    
    boolean updateTasks(
            Initiator initiator, List<Task> tasks);
    
    Optional<LocalDateTime> getTimeOfFirstTask(
            Initiator initiator);
    
    VoidOperation createTask(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation deleteTask(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation editTask(
            Initiator initiator, ArgumentsCommand command);
    
    ValueOperation<List<Task>> findTasks(
            Initiator initiator, ArgumentsCommand command);
}
