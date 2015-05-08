package com.drs.beam.remote.codebase;

import com.drs.beam.tasks.Task;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Org by Diarsid
 * Time: 13:07 - 25.02.15
 * IDE: IntelliJ IDEA 12
 */

public interface TaskManagerIF extends Remote {      
    
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
