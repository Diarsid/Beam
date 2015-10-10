/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.server.modules.executor.handlers;

import java.util.List;

import com.drs.beam.server.entities.command.StoredExecutorCommand;

/**
 *
 * @author Diarsid
 */
public interface CommandsHandler {
    
    void newCommand(List<String> command, String commandName);
    
    List<StoredExecutorCommand> getAllCommands();
    
    List<StoredExecutorCommand> getCommands(String commandName);
    
    StoredExecutorCommand getCommand(String name);
    
    boolean deleteCommand(String commandName);
}
