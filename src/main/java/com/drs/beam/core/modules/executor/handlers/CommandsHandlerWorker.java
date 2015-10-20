/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.executor.handlers;

import com.drs.beam.core.modules.executor.CommandsHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.drs.beam.core.entities.StoredExecutorCommand;
import com.drs.beam.core.modules.data.dao.commands.CommandsDao;
import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
class CommandsHandlerWorker implements CommandsHandler{
    // Fields =============================================================================
    
    private final InnerIOModule ioEngine;
    private final CommandsDao dao;        

    // Constructors =======================================================================

    CommandsHandlerWorker(CommandsDao dao, InnerIOModule io) {
        this.ioEngine = io;
        this.dao = dao;
    }
    
    // Methods ============================================================================
    
    @Override
    public void newCommand(List<String> command, String commandName){
        for(int i = 0; i < command.size(); i++){
            String s = command.get(i).trim().toLowerCase();
            if (s.contains("call")){
                this.ioEngine.reportMessage(
                        "'call' is not permitted to use.",
                        "It can cause cyclical execution.");
                return;
            }
            command.set(i, s);
        }
        commandName = commandName.trim().toLowerCase();
        try{
            this.dao.saveNewCommand(new StoredExecutorCommand(commandName, command));
        } catch (SQLException e){
            if (e.getSQLState().startsWith("23")){
                this.ioEngine.reportMessage("Such command name already exists.");
            } else {
                this.ioEngine.reportException(e, "SQLException: save command.");
            }
        }
    }
    
    @Override
    public List<StoredExecutorCommand> getAllCommands(){
        try{
            return this.dao.getAllCommands();
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get commands.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<StoredExecutorCommand> getCommands(String commandName){
        commandName = commandName.trim().toLowerCase();
        try {
            return this.dao.getCommandsByName(commandName);
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get commands by name.");
            return Collections.emptyList();
        }        
    }
    
    @Override
    public boolean deleteCommand(String commandName){
        commandName = commandName.trim().toLowerCase();
        try {
            return this.dao.removeCommand(commandName);
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: remove command.");
            return false;
        }
    }
    
    @Override
    public StoredExecutorCommand getCommand(String name){
        name = name.trim().toLowerCase();
        try {
            List<StoredExecutorCommand> commands = this.dao.getCommandsByName(name);
            return this.resolveMultipleCommands(commands);
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get commands by name.");
            return null;
        }        
    }
    
    private StoredExecutorCommand resolveMultipleCommands(List<StoredExecutorCommand> commands){        

        if (commands.size() < 1){
            this.ioEngine.reportMessage("Couldn`t find such command.");
            return null;
        } else if (commands.size() == 1){
            return commands.get(0);
        } else {
            List<String> commandNames = new ArrayList<>();
            for (StoredExecutorCommand c : commands){
                commandNames.add(c.getName());
            }
            int variant = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several commands:", 
                    commandNames
            );
            
            if (variant < 0){
                return null;
            } else {
                return commands.get(variant-1);
            }            
        }
    }
}
