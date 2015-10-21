/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.rmi;

import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.entities.Task;
import com.drs.beam.core.modules.TaskManagerModule;

import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author Diarsid
 */
class RmiTaskManagerAdapter implements RmiTaskManagerInterface{
    // Fields =============================================================================
    private final TaskManagerModule taskManagerModule;
    // Constructors =======================================================================
    
    RmiTaskManagerAdapter(TaskManagerModule taskManagerModule){
        this.taskManagerModule = taskManagerModule;
    }

    // Methods ============================================================================
    @Override
    public void createNewTask(String time, String[] task) throws RemoteException {
        this.taskManagerModule.createNewTask(time, task);
    }
    
    @Override
    public String getFirstAlarmTime() throws RemoteException{
        return this.taskManagerModule.getFirstAlarmTime();
    }
    
    @Override
    public List<Task> getFutureTasks() throws RemoteException{
        return this.taskManagerModule.getFutureTasks();
    }
    
    @Override
    public List<Task> getPastTasks() throws RemoteException{
        return this.taskManagerModule.getPastTasks();
    }
    
    @Override
    public List<Task> getFirstTask() throws RemoteException{
        return this.taskManagerModule.getFirstTask();
    }
    
    @Override
    public boolean deleteTaskByText(String text) throws RemoteException{
        return this.taskManagerModule.deleteTaskByText(text);
    }

    @Override
    public boolean removeAllTasks() throws RemoteException{
        return this.taskManagerModule.removeAllTasks();
    }
    
    @Override
    public boolean  removeAllFutureTasks() throws RemoteException{
        return this.taskManagerModule.removeAllFutureTasks();
    }
    
    @Override
    public boolean  removeAllPastTasks() throws RemoteException{
        return this.taskManagerModule.removeAllPastTasks();
    }
}