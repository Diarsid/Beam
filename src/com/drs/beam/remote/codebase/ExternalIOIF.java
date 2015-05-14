/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.remote.codebase;

import com.drs.beam.tasks.Task;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ExternalIOIF extends Remote {
    void showTask           (Task task)     throws RemoteException;
    void informAboutError   (String error)  throws RemoteException;
    void informAbout        (String info)   throws RemoteException;
    void isActive           ()              throws RemoteException;
}
