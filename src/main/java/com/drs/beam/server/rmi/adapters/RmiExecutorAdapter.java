/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.rmi.adapters;

import com.drs.beam.server.modules.executor.ExecutorModule;
import com.drs.beam.server.rmi.interfaces.RmiExecutorInterface;

import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.entities.command.StoredExecutorCommand;

/**
 *
 * @author Diarsid
 */
public class RmiExecutorAdapter implements RmiExecutorInterface {
    // Fields =============================================================================
    
    private final ExecutorModule executorModule;
    
    // Constructors =======================================================================

    public RmiExecutorAdapter(ExecutorModule executorModule) {
        this.executorModule = executorModule;
    }
    
    // Methods ============================================================================
    
    @Override
    public void open(List<String> commandParams) throws RemoteException{
        this.executorModule.open(commandParams);
    }
    
    @Override
    public void run(List<String> commandParams) throws RemoteException{
        this.executorModule.run(commandParams);
    }
    
    @Override
    public void call(List<String> commandParams) throws RemoteException{
        this.executorModule.call(commandParams);
    }
    
    @Override
    public void start(List<String> commandParams) throws RemoteException{
        this.executorModule.start(commandParams);
    }
    
    @Override
    public void stop(List<String> commandParams) throws RemoteException{
        this.executorModule.stop(commandParams);
    }
    
    @Override
    public void newCommand(List<String> command, String commandName) throws RemoteException{
        this.executorModule.newCommand(command, commandName);
    }
    
    @Override
    public void newLocation(String locationPath, String locationName) throws RemoteException{
        this.executorModule.newLocation(locationPath, locationName);
    }
    
    @Override
    public List<String> listLocationContent(String locationName) throws RemoteException{
        return this.executorModule.listLocationContent(locationName);
    }
    
    @Override
    public List<Location> getAllLocations() throws RemoteException{
        return this.executorModule.getAllLocations();
    }
    
    @Override
    public List<StoredExecutorCommand> getAllCommands() throws RemoteException{
        return this.executorModule.getAllCommands();
    }
    
    @Override
    public List<Location> getLocation(String locationName) throws RemoteException{
        return this.executorModule.getLocation(locationName);
    }
    
    @Override
    public List<StoredExecutorCommand> getCommand(String commandName)  throws RemoteException{
        return this.executorModule.getCommand(commandName);
    }
    
    @Override
    public boolean deleteCommand(String commandName) throws RemoteException{
        return this.executorModule.deleteCommand(commandName);
    }
    
    @Override
    public boolean deleteLocation(String locationName) throws RemoteException{
        return this.executorModule.deleteLocation(locationName);
    }
}
