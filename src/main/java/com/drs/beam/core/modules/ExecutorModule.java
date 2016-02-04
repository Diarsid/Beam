/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import java.util.List;
import java.util.Map;

import com.drs.beam.core.modules.executor.StoredExecutorCommand;
import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ExecutorModule extends GemModule {
        
    void open(List<String> commandParams);
    void run(List<String> commandParams);
    void call(List<String> commandParams);
    void start(List<String> commandParams);
    void stop(List<String> commandParams);
    void openWebPage(List<String> commandParams);
    
    void newCommand(List<String> command, String commandName);
    
    List<String> listLocationContent(String locationName);
    
    boolean checkPath(String path);
    
    List<StoredExecutorCommand> getAllCommands();
    
    List<StoredExecutorCommand> getCommands(String commandName);
    
    boolean deleteCommand(String commandName);
    
    void setIntelligentActive(boolean isActive);    
    boolean deleteMem(String command);    
    void setAskUserToRememberHisChoice(boolean askUser);
    Map<String, String> getAllChoices();
    
    void newNote(List<String> commandParams);
    void openNotes();
    void openNote(List<String> commandParams);
}
