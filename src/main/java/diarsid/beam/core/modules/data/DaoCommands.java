/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.modules.executor.StoredExecutorCommand;

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
