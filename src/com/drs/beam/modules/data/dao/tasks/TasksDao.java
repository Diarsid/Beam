/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.data.dao.tasks;

import com.drs.beam.modules.tasks.Task;
import com.drs.beam.modules.data.DataManager;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Diarsid
 */
public interface TasksDao {    
    void        saveTask(Task task); 
    
    boolean     deleteTaskByText  (String text);
    boolean     deleteTasks      (int tasksSort);    
        
    ArrayList<Task>  extractFirstTasks    ();
    ArrayList<Task>  extractExpiredTasks  (LocalDateTime fromNow);
    
    int             getLastId           ();
    LocalDateTime    getFirstTaskTime     ();
    ArrayList<Task>  getTasks            (int isActive);
    ArrayList<Task>  getTasksByTime       (LocalDateTime firstTaskTime);
}
