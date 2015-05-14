/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.remote.codebase;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface OrgIOIF extends Remote {

    public boolean hasExternalIOProcessor() throws RemoteException;
    public void acceptNewIOProcessor() throws RemoteException;

    public void useExternalShowTaskMethod() throws RemoteException;
    public void useNativeShowTaskMethod() throws RemoteException;

    public void exit() throws RemoteException;
    public void setDefaultIO() throws RemoteException;
}
