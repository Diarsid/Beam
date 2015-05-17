/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.executor.dao;

import com.drs.beam.util.data.DataBase;

/**
 *
 * @author Diarsid
 */
public class ExecutorDaoH2 implements ExecutorDao{
    // Fields =============================================================================
    private final DataBase data;
    
    // Constructors =======================================================================
    public ExecutorDaoH2(DataBase data) {
        this.data = data;
    }
    
    // Methods ============================================================================
    
    @Override
    public void saveNewCommand(String[] command, String commandName){
        
    }
    
    @Override
    public void saveNewLocation(String location){
        
    }
    
    @Override
    public void removeCommand(String commandName){
        
    }
    
    @Override
    public void removeLocation(String LocationName){
        
    }

}
