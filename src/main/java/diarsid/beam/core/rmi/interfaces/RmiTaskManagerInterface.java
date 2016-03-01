/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.rmi.interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import diarsid.beam.core.modules.tasks.TaskMessage;
import diarsid.beam.core.modules.tasks.TaskType;

public interface RmiTaskManagerInterface extends Remote{      
 
    boolean createNewTask(TaskType type, String time, String[] task, 
            Set<Integer> days, Set<Integer> hours)
            throws RemoteException;
    
    String           getFirstAlarmTime()  throws RemoteException;
    List<TaskMessage>   getFutureTasks()    throws RemoteException;
    List<TaskMessage>   getPastTasks()      throws RemoteException;
    List<TaskMessage>   getFirstTask()      throws RemoteException;
    
    List<TaskMessage>   getScheduledReminders()    throws RemoteException;
    List<TaskMessage>   getScheduledEvents()    throws RemoteException;
    
    boolean  deleteTaskByText(String text) throws RemoteException;

    boolean  removeAllTasks()        throws RemoteException;
    boolean  removeAllFutureTasks()   throws RemoteException;
    boolean  removeAllPastTasks()     throws RemoteException;
    
    /*
    boolean suspendTask(String text) throws RemoteException;
    boolean activateSuspendedTask(String text) throws RemoteException;
    */
}