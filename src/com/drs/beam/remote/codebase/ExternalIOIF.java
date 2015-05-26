/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.remote.codebase;

import com.drs.beam.tasks.Task;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface describes 
 */
public interface ExternalIOIF extends Remote {    
    void isActive            ()              throws RemoteException;
    void showTask            (Task task)      throws RemoteException;
    void informAbout         (String info)     throws RemoteException;    
    void informAboutError     (String error, boolean isCritical)   throws RemoteException;
    void informAboutException (Exception e, boolean isCritical) throws RemoteException;
}
