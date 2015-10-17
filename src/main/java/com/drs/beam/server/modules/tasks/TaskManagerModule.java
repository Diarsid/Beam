/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.server.modules.tasks;

import java.util.List;

import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.modules.Module;
import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public interface TaskManagerModule extends Module {
        
    void createNewTask(String time, String[] task);
    
    String           getFirstAlarmTime();
    List<Task>   getFutureTasks();
    List<Task>   getPastTasks();
    List<Task>   getFirstTask();
    
    boolean  deleteTaskByText(String text);

    boolean  removeAllTasks();
    boolean  removeAllFutureTasks();
    boolean  removeAllPastTasks();
    
    static String getModuleName(){
        return "Task Manager Module";
    }
    
    static TaskManagerModule buildModule(InnerIOModule ioModule, DataManagerModule dataModule){
        return new TaskManager(ioModule, dataModule);
    }
}
