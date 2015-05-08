/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.tasks.dao;

import com.drs.beam.tasks.Task;
import com.drs.beam.util.DBManager;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Diarsid
 */
public interface TasksDao {    
    static TasksDao getDao(){
        return DBManager.getTasksDAO();
    }
    
    void saveTask(Task task);
    
    boolean     isDBinitialized();
    void        initTasksTable();    
    
    int             getLastId           ();
    LocalDateTime    getFirstTaskTime     ();    
    ArrayList<Task>  extractFirstTasks    ();
    ArrayList<Task>  extractExpiredTasks  (LocalDateTime fromNow);
    ArrayList<Task>  getTasks            (int isActive);
    ArrayList<Task>  getTasksByTime       (LocalDateTime firstTaskTime);
    
    boolean deleteTaskByText  (String text);
    boolean deleteTasks      (int tasksSort);
}
