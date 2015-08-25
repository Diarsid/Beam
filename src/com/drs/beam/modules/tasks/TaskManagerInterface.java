/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.tasks;

import com.drs.beam.modules.Module;
import com.drs.beam.modules.tasks.Task;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface TaskManagerInterface extends Remote, Module {      
    
    public void     createNewTask(String time, String[] task) throws RemoteException;
    
    public String           getFirstAlarmTime()  throws RemoteException;
    public ArrayList<Task>   getFutureTasks()    throws RemoteException;
    public ArrayList<Task>   getPastTasks()      throws RemoteException;
    public ArrayList<Task>   getFirstTask()      throws RemoteException;
    
    public boolean  deleteTaskByText(String text) throws RemoteException;

    public boolean  removeAllTasks()        throws RemoteException;
    public boolean  removeAllFutureTasks()   throws RemoteException;
    public boolean  removeAllPastTasks()     throws RemoteException;
}
