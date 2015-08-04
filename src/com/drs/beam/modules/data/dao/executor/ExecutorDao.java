/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.data.dao.executor;

import com.drs.beam.modules.data.DBManager;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Diarsid
 */
public interface ExecutorDao {
    
    public Map<String, String> getLocationsByName(String locationName);
    public Map<String, String> getLocationsByNameParts(String[] locationNameParts);
    public Map<String, List<String>> getCommandsByName(String commandName);
    
    public void saveNewCommand(List<String> command, String commandName);
    public void saveNewLocation(String locationPath, String locationName);
    
    public boolean removeCommand(String commandName);
    public boolean removeLocation(String LocationName);
        
    public Map<String, String> getLocations();
    public Map<String, List<String>> getCommands();
    
    public static ExecutorDao getDao(){
        return DBManager.getExecutorDao();
    }    
}
