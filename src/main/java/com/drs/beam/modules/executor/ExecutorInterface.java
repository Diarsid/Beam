/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.executor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/*
 * 
 */
public interface ExecutorInterface extends Remote {
    
    void open(List<String> commandParams) throws RemoteException;
    void run(List<String> commandParams) throws RemoteException;
    void call(List<String> commandParams) throws RemoteException;
    void start(List<String> commandParams) throws RemoteException;
    void stop(List<String> commandParams) throws RemoteException;
    
    void newCommand(List<String> command, String commandName) throws RemoteException;
    void newLocation(String locationPath, String locationName) throws RemoteException;
    
    List<String> listLocationContent(String locationName) throws RemoteException;
    
    Map<String, String>      getAllLocations() throws RemoteException;
    Map<String, List<String>> getAllCommands() throws RemoteException;
    
    String      getLocation(String locationName) throws RemoteException;
    List<String> getCommand(String commandName)  throws RemoteException;
    
    boolean deleteCommand(String commandName) throws RemoteException;
    boolean deleteLocation(String locationName) throws RemoteException;
}
