/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import old.diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;

import diarsid.beam.core.modules.tasks.TimeMessage;

import old.diarsid.beam.core.modules.TaskManagerModule;

import diarsid.beam.core.modules.tasks.TaskType;

/**
 *
 * @author Diarsid
 */
class RmiAdapterForTaskManager implements RmiTaskManagerInterface {
    
    private final TaskManagerModule taskManagerModule;
        
    RmiAdapterForTaskManager(TaskManagerModule taskManagerModule) {
        this.taskManagerModule = taskManagerModule;
    }
    
    @Override
    public boolean createNewTask(TaskType type, String time, String[] task, 
            Set<Integer> days, Set<Integer> hours) 
            throws RemoteException {
        
        return this.taskManagerModule.createNewTask(type, time, task, days, hours);
    }
    
    @Override
    public String getFirstAlarmTime() throws RemoteException {
        return this.taskManagerModule.getFirstAlarmTime();
    }
    
    @Override
    public List<TimeMessage> getFutureTasks() throws RemoteException {
        return this.taskManagerModule.getFutureTasks();
    }
    
    @Override
    public List<TimeMessage> getPastTasks() throws RemoteException {
        return this.taskManagerModule.getPastTasks();
    }
    
    @Override
    public List<TimeMessage> getFirstTask() throws RemoteException {
        return this.taskManagerModule.getFirstTask();
    }
    
    @Override
    public List<TimeMessage> getScheduledReminders() throws RemoteException {
        return this.taskManagerModule.getActualReminders();
    }
    @Override
    public List<TimeMessage> getScheduledEvents() throws RemoteException {
        return this.taskManagerModule.getActualEvents();
    }
    
    @Override
    public boolean deleteTaskByText(String text) throws RemoteException {
        return this.taskManagerModule.deleteTaskByText(text);
    }

    @Override
    public boolean removeAllTasks() throws RemoteException {
        return this.taskManagerModule.removeAllTasks();
    }
    
    @Override
    public boolean  removeAllFutureTasks() throws RemoteException {
        return this.taskManagerModule.removeAllFutureTasks();
    }
    
    @Override
    public boolean  removeAllPastTasks() throws RemoteException {
        return this.taskManagerModule.removeAllPastTasks();
    }
    
    /*
    @Override
    public boolean suspendTask(String text) throws RemoteException {
        return this.taskManagerModule.suspendTask(text);
    }
    
    @Override
    public boolean activateSuspendedTask(String text) throws RemoteException {
        return this.taskManagerModule.activateSuspendedTask(text);
    }
    */
}
