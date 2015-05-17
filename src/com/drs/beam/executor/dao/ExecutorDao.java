/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.executor.dao;

import com.drs.beam.util.data.DBManager;

/**
 *
 * @author Diarsid
 */
public interface ExecutorDao {
    
    public void saveNewCommand(String[] command, String commandName);
    public void saveNewLocation(String location);
    
    public void removeCommand(String commandName);
    public void removeLocation(String LocationName);
    
    public static ExecutorDao getDao(){
        return DBManager.getExecutorDao();
    }    
}
