/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.executor.dao;

import com.drs.beam.util.data.DBManager;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Diarsid
 */
public interface ExecutorDao {
    
    public String getLocationByName(String location);
    public List<String> getCommandsByNames(List<String> commandsNames);
    
    public void saveNewCommand(List<String> command, String commandName);
    public void saveNewLocation(String locationPath, String locationName);
    
    public void removeCommand(String commandName);
    public void removeLocation(String LocationName);
        
    public Map<String, String> viewLocations();
    public Map<String, List<String>> viewCommands();
    
    public static ExecutorDao getDao(){
        return DBManager.getExecutorDao();
    }    
}
