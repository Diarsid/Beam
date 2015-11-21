/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.drs.beam.core.modules.tasks.Task;

/**
 *
 * @author Diarsid
 */
public interface DaoTasks {  
    void saveTask(Task task); 
    
    boolean deleteTaskByText(String text); 
    
    boolean deleteAllTasks();
    boolean deleteActualTasks();
    boolean deleteNonActualTasks();
        
    List<Task> extractFirstTasks (); 
    List<Task> extractExpiredTasks (LocalDateTime fromNow); 
   
    LocalDateTime getFirstTaskTime (); 
    
    List<Task> getAllTasks ();
    List<Task> getNonActualTasks ();
    List<Task> getActualTasks ();
    
    List<Task> getTasksByTime (LocalDateTime firstTaskTime); 
}
