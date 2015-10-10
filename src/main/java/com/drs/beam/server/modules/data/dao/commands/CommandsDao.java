/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data.dao.commands;

import java.sql.SQLException;
import java.util.List;

import com.drs.beam.server.entities.command.StoredExecutorCommand;

/**
 *
 * @author Diarsid
 */
public interface CommandsDao {
    
    public List<StoredExecutorCommand> getCommandsByName(String commandName) throws SQLException;
    
    public void saveNewCommand(StoredExecutorCommand command) throws SQLException;;
    
    public boolean removeCommand(String commandName) throws SQLException;
    
    public List<StoredExecutorCommand> getAllCommands() throws SQLException;
}
