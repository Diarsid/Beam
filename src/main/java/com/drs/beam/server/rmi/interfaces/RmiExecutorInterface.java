/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.entities.command.StoredExecutorCommand;

/*
 * 
 */
public interface RmiExecutorInterface extends Remote {
    
    void open(List<String> commandParams) throws RemoteException;
    void run(List<String> commandParams) throws RemoteException;
    void call(List<String> commandParams) throws RemoteException;
    void start(List<String> commandParams) throws RemoteException;
    void stop(List<String> commandParams) throws RemoteException;
    
    void newCommand(List<String> command, String commandName) throws RemoteException;
    void newLocation(String locationPath, String locationName) throws RemoteException;
    
    List<String> listLocationContent(String locationName) throws RemoteException;
    
    List<Location> getAllLocations() throws RemoteException;
    List<StoredExecutorCommand> getAllCommands() throws RemoteException;
    
    List<Location> getLocation(String locationName) throws RemoteException;
    List<StoredExecutorCommand> getCommand(String commandName)  throws RemoteException;
    
    boolean deleteCommand(String commandName) throws RemoteException;
    boolean deleteLocation(String locationName) throws RemoteException;
}
