/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.domain.entities.SchedulableType;
import diarsid.beam.core.domain.entities.Task;

/**
 *
 * @author Diarsid
 */
public interface DaoTasks {
    
    Optional<LocalDateTime> getTimeOfFirstActiveTask(
            Initiator initiator);
    
    List<Task> getActiveTasksOfTypeBetweenDates(
            Initiator initiator, LocalDateTime from, LocalDateTime to, SchedulableType... type);
    
    List<Task> getExpiredTasks(
            Initiator initiator, LocalDateTime fromNow);
    
    List<Task> getFirstActiveTasks(
            Initiator initiator);
    
    Optional<LocalDateTime> updateTasks(
            Initiator initiator, List<Task> tasks);
}
