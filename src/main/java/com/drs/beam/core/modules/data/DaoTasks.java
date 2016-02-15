/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.drs.beam.core.modules.tasks.Task;
import com.drs.beam.core.modules.tasks.TaskMessage;

/**
 *
 * @author Diarsid
 */
public interface DaoTasks { 
    
    String DB_TIME_PATTERN = "uuuu-MM-dd HH:mm";
    
    LocalDateTime addTask(Task task); 
    
    LocalDateTime deleteTaskByText(String text); 
    
    LocalDateTime deleteAllTasks();
    LocalDateTime deleteActualTasks();
    LocalDateTime deleteNonActualTasks();
        
    List<Task> getFirstTasks(); 
    List<Task> getExpiredTasks(LocalDateTime fromNow); 
    LocalDateTime updateTasksAndGetNextFirstTime(List<Task> tasksToUpdate);
   
    LocalDateTime getFirstTaskTime(); 
    
    List<TaskMessage> getAllTasks();
    List<TaskMessage> getNonActualTasks();
    List<TaskMessage> getActualTasks();
    
    List<TaskMessage> getTasksByTime(LocalDateTime firstTaskTime); 
    
    List<TaskMessage> getCalendarTasksBetweenDates(LocalDateTime from, LocalDateTime to);
}
