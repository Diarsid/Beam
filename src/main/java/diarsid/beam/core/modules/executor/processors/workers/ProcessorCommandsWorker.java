/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.processors.ProcessorCommands;
import diarsid.beam.core.modules.executor.entities.StoredExecutorCommand;

/**
 *
 * @author Diarsid
 */
class ProcessorCommandsWorker implements ProcessorCommands {
    
    private final IoInnerModule ioEngine;
    private final DaoCommands commandsDao;
    private final IntelligentExecutorCommandContext intellContext;
    
    ProcessorCommandsWorker(
            IoInnerModule io, 
            DaoCommands dao, 
            IntelligentExecutorCommandContext intell) {
        
        this.ioEngine = io;
        this.commandsDao = dao;
        this.intellContext = intell;
    }    
    
    @Override
    public StoredExecutorCommand getCommand(String name) {        
        name = name.trim().toLowerCase();
        List<StoredExecutorCommand> commands = this.commandsDao.getCommandsByName(name);    
        
        if ( commands.size() < 1 ) {
            this.ioEngine.reportMessage("Couldn`t find such command.");
            return null;
        } else if ( commands.size() == 1 ) {
            return commands.get(0);
        } else {
            List<String> commandNames = new ArrayList<>();
            for (StoredExecutorCommand c : commands) {
                commandNames.add(c.getName());
            }
            int variant = this.intellContext.resolve(
                    "There are several commands:", 
                    name, 
                    commandNames);
            //int variant = this.ioEngine.resolveVariantsWithExternalIO(
            //        "There are several commands:", 
            //        commandNames
            //);
            
            if ( variant < 0 ) {
                return null;
            } else {
                return commands.get(variant-1);
            }            
        }
    }       
    
    @Override
    public void newCommand(List<String> commands, String commandName) {
        for(int i = 0; i < commands.size(); i++) {
            String s = commands.get(i).trim().toLowerCase();
            if ( s.equals("call") || s.equals("exe") ) {
                this.ioEngine.reportMessage(
                        "'call' and 'exe' is not permitted to use.",
                        "It can cause cyclical execution.");
                return;
            }
            commands.set(i, s);
        }
        commandName = commandName.trim().toLowerCase();
        this.commandsDao.saveNewCommand(new StoredExecutorCommand(commandName, commands));
    }    
        
    @Override
    public boolean deleteCommand(String commandName) {
        commandName = commandName.trim().toLowerCase();
        return this.commandsDao.removeCommand(commandName);
    }    
    
    @Override
    public List<StoredExecutorCommand> getAllCommands() {
        return this.commandsDao.getAllCommands();
    }    
    
    @Override
    public List<StoredExecutorCommand> getCommands(String commandName) {
        commandName = commandName.trim().toLowerCase();
        return this.commandsDao.getCommandsByName(commandName);
    }    
}
