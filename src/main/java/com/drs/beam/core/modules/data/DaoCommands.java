/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data;

import java.util.List;

import com.drs.beam.core.modules.executor.StoredExecutorCommand;

/**
 *
 * @author Diarsid
 */
public interface DaoCommands {
    
    public List<StoredExecutorCommand> getCommandsByName(String commandName);
    
    public void saveNewCommand(StoredExecutorCommand command);
    
    public boolean removeCommand(String commandName);
    
    public List<StoredExecutorCommand> getAllCommands();
}
