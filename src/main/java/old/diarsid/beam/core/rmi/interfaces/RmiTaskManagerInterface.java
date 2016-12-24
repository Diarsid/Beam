/*
 * project: Beam
 * author: Diarsid
 */
package old.diarsid.beam.core.rmi.interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import diarsid.beam.core.modules.tasks.TimeMessage;
import diarsid.beam.core.modules.tasks.TaskType;

public interface RmiTaskManagerInterface extends Remote{      
 
    boolean createNewTask(TaskType type, String time, String[] task, 
            Set<Integer> days, Set<Integer> hours)
            throws RemoteException;
    
    String           getFirstAlarmTime()  throws RemoteException;
    List<TimeMessage>   getFutureTasks()    throws RemoteException;
    List<TimeMessage>   getPastTasks()      throws RemoteException;
    List<TimeMessage>   getFirstTask()      throws RemoteException;
    
    List<TimeMessage>   getScheduledReminders()    throws RemoteException;
    List<TimeMessage>   getScheduledEvents()    throws RemoteException;
    
    boolean  deleteTaskByText(String text) throws RemoteException;

    boolean  removeAllTasks()        throws RemoteException;
    boolean  removeAllFutureTasks()   throws RemoteException;
    boolean  removeAllPastTasks()     throws RemoteException;
    
    /*
    boolean suspendTask(String text) throws RemoteException;
    boolean activateSuspendedTask(String text) throws RemoteException;
    */
}
