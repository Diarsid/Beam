/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.data.dao.commands;

import com.drs.beam.modules.data.dao.locations.*;
import com.drs.beam.modules.data.DataManager;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Diarsid
 */
public interface CommandsDao {
    
    public Map<String, List<String>> getCommandsByName(String commandName);
    
    public void saveNewCommand(List<String> command, String commandName);
    
    public boolean removeCommand(String commandName);
    
    public Map<String, List<String>> getCommands();
}
