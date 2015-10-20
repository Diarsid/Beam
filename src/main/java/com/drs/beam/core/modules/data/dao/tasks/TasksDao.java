/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data.dao.tasks;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.drs.beam.core.entities.Task;

/**
 *
 * @author Diarsid
 */
public interface TasksDao {  
    void saveTask(Task task) throws SQLException; 
    
    boolean deleteTaskByText(String text) throws SQLException; 
    boolean deleteTasks(int tasksSort) throws SQLException; 
        
    List<Task> extractFirstTasks () throws SQLException; 
    List<Task> extractExpiredTasks (LocalDateTime fromNow) throws SQLException; 
   
    LocalDateTime getFirstTaskTime () throws SQLException; 
    List<Task> getTasks (int isActive) throws SQLException; 
    List<Task> getTasksByTime (LocalDateTime firstTaskTime) throws SQLException; 
}
