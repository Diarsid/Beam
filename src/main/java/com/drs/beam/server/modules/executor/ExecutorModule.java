/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.server.modules.executor;

import java.util.List;

import com.drs.beam.server.entities.command.StoredExecutorCommand;
import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.modules.Module;
import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.executor.handlers.CommandsHandler;
import com.drs.beam.server.modules.executor.handlers.HandlersBuilder;
import com.drs.beam.server.modules.executor.handlers.LocationsHandler;
import com.drs.beam.server.modules.executor.os.OS;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public interface ExecutorModule extends Module {
        
    void open(List<String> commandParams);
    void run(List<String> commandParams);
    void call(List<String> commandParams);
    void start(List<String> commandParams);
    void stop(List<String> commandParams);
    
    void newCommand(List<String> command, String commandName);
    void newLocation(String locationPath, String locationName);
    
    List<String> listLocationContent(String locationName);
    
    List<Location> getAllLocations();
    List<StoredExecutorCommand> getAllCommands();
    
    List<Location> getLocations(String locationName);
    List<StoredExecutorCommand> getCommands(String commandName);
    
    boolean deleteCommand(String commandName);
    boolean deleteLocation(String locationName);
    
    static String getModuleName(){
        return "Executor Module";
    }
    
    static ExecutorModule buildModule(InnerIOModule ioModule, DataManagerModule dataModule){
        HandlersBuilder handlersBuilder = new HandlersBuilder();
        CommandsHandler commandsHandler = handlersBuilder.buildCommandsHandler(ioModule, dataModule);
        LocationsHandler locationsHandler = handlersBuilder.buildLocationsHandler(ioModule, dataModule);
        OS os = OS.getOS(ioModule);
        
        return new Executor(ioModule, locationsHandler, commandsHandler, os);
    }
}
