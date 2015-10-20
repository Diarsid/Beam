/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import java.util.List;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.entities.StoredExecutorCommand;

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
}
