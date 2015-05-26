/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.tasks.dao;

import com.drs.beam.tasks.Task;
import com.drs.beam.util.data.DBManager;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Diarsid
 */
public interface TasksDao {    
    void        saveTask(Task task);
    
    boolean     isDBinitialized();
    void        initTasksTable(); 
    
    boolean     deleteTaskByText  (String text);
    boolean     deleteTasks      (int tasksSort);    
        
    ArrayList<Task>  extractFirstTasks    ();
    ArrayList<Task>  extractExpiredTasks  (LocalDateTime fromNow);
    
    int             getLastId           ();
    LocalDateTime    getFirstTaskTime     ();
    ArrayList<Task>  getTasks            (int isActive);
    ArrayList<Task>  getTasksByTime       (LocalDateTime firstTaskTime);
        
        
    static TasksDao getDao(){
        return DBManager.getTasksDAO();
    }
}
