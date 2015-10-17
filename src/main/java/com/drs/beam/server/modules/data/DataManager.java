/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data;

import com.drs.beam.server.modules.data.base.DataBase;
import com.drs.beam.server.modules.data.dao.commands.CommandsDao;
import com.drs.beam.server.modules.data.dao.locations.LocationsDao;
import com.drs.beam.server.modules.data.dao.tasks.TasksDao;

/*
 * 
 */
class DataManager implements DataManagerModule{
    // Fields =============================================================================
    
    private final DataBase dataBase;
    private final DaoProvider daoProvider;
    
    // Constructor ========================================================================
    DataManager(DataBase data, DaoProvider daoProvider) {
        this.dataBase = data;
        this.daoProvider = daoProvider;
    }
        
    // Methods ============================================================================
        
    @Override
    public TasksDao getTasksDao(){
        return this.daoProvider.createTasksDao(this.dataBase);
    }    
    
    @Override
    public LocationsDao getLocationsDao(){        
        return this.daoProvider.createLocationsDao(this.dataBase);
    }
    
    @Override
    public CommandsDao getCommandsDao(){
        return this.daoProvider.createCommandsDao(this.dataBase);
    }
}
