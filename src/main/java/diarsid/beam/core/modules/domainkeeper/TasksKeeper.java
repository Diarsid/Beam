/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.TimeMessage;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
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
    
    OperationFlow createTask(
            Initiator initiator, MultiStringCommand command);
    
    OperationFlow deleteTask(
            Initiator initiator, SingleStringCommand command);
    
    OperationFlow editTask(
            Initiator initiator, SingleStringCommand command);
    
    List<Task> findTasks(
            Initiator initiator, SingleStringCommand findEntityCommand);
}
