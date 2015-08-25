/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.remote.codebase;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/*
 * 
 */
public interface ExecutorInterface extends Remote {
    void open(String command) throws RemoteException;
    void run(String command) throws RemoteException;
    void call(String command) throws RemoteException;
    void start(String command) throws RemoteException;
    void stop(String command) throws RemoteException;
    
    void newCommand(List<String> command, String commandName) throws RemoteException;
    void newLocation(String locationPath, String locationName) throws RemoteException;
    
    Map<String, String>      getLocations() throws RemoteException;
    Map<String, List<String>> getCommands() throws RemoteException;
    
    boolean deleteCommand(String commandName) throws RemoteException;
    boolean deleteLocation(String locationName) throws RemoteException;
}
