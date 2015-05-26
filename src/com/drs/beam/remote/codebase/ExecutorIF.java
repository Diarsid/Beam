/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.remote.codebase;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * 
 */
public interface ExecutorIF extends Remote {
    public void execute(String command) throws RemoteException;
    
    public void newCommand(String[] command, String commandName) throws RemoteException;
    public void newLocation(String location) throws RemoteException;
    
    public void deleteCommand(String commandName) throws RemoteException;
    public void deleteLocation(String locationName) throws RemoteException;
}