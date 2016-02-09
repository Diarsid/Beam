/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import com.drs.beam.core.modules.tasks.TaskMessage;
import com.drs.beam.core.modules.tasks.TaskType;
import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface TaskManagerModule extends GemModule {
    
    boolean createNewTask(TaskType type, String time, String[] task, 
            Set<Integer> days, Set<Integer> hours);
    
    String           getFirstAlarmTime();
    List<TaskMessage>   getFutureTasks();
    List<TaskMessage>   getPastTasks();
    List<TaskMessage>   getFirstTask();
    
    boolean  deleteTaskByText(String text);

    boolean  removeAllTasks();
    boolean  removeAllFutureTasks();
    boolean  removeAllPastTasks();
    
    /*
    boolean suspendTask(String text);
    boolean activateSuspendedTask(String text);
    */
}
