/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data.dao.commands;

import java.util.List;

import com.drs.beam.server.entities.command.StoredExecutorCommand;

/**
 *
 * @author Diarsid
 */
public interface CommandsDao {
    
    public List<StoredExecutorCommand> getCommandsByName(String commandName);
    
    public void saveNewCommand(StoredExecutorCommand command);
    
    public boolean removeCommand(String commandName);
    
    public List<StoredExecutorCommand> getAllCommands();
}
