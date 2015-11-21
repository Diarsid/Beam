/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.core.Module;

import java.util.List;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.modules.executor.StoredExecutorCommand;

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
    void openWebPage(List<String> commandParams);
    
    void newCommand(List<String> command, String commandName);
    
    List<String> listLocationContent(String locationName);
    
    List<StoredExecutorCommand> getAllCommands();
    
    List<StoredExecutorCommand> getCommands(String commandName);
    
    boolean deleteCommand(String commandName);
    
    static String getModuleName(){
        return "Executor Module";
    }    
}
