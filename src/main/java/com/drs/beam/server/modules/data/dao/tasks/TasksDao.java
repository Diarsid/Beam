/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data.dao.tasks;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.drs.beam.server.entities.task.Task;

/**
 *
 * @author Diarsid
 */
public interface TasksDao {  
    void saveTask(Task task) throws SQLException; 
    
    boolean deleteTaskByText(String text) throws SQLException; 
    boolean deleteTasks(int tasksSort) throws SQLException; 
        
    ArrayList<Task>  extractFirstTasks    () throws SQLException; 
    ArrayList<Task>  extractExpiredTasks  (LocalDateTime fromNow) throws SQLException; 
   
    LocalDateTime    getFirstTaskTime     () throws SQLException; 
    ArrayList<Task>  getTasks            (int isActive) throws SQLException; 
    ArrayList<Task>  getTasksByTime       (LocalDateTime firstTaskTime) throws SQLException; 
}
