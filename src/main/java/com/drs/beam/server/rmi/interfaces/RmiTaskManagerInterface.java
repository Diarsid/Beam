/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.rmi.interfaces;

import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.entities.task.Task;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RmiTaskManagerInterface extends Remote{      
    
    void createNewTask(String time, String[] task) throws RemoteException;
    
    String           getFirstAlarmTime()  throws RemoteException;
    List<Task>   getFutureTasks()    throws RemoteException;
    List<Task>   getPastTasks()      throws RemoteException;
    List<Task>   getFirstTask()      throws RemoteException;
    
    boolean  deleteTaskByText(String text) throws RemoteException;

    boolean  removeAllTasks()        throws RemoteException;
    boolean  removeAllFutureTasks()   throws RemoteException;
    boolean  removeAllPastTasks()     throws RemoteException;
}
