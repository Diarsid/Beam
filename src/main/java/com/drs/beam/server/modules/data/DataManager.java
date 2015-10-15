/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data;

import com.drs.beam.server.modules.Modules;
import com.drs.beam.server.modules.data.base.DataBase;
import com.drs.beam.server.modules.data.dao.commands.CommandsDao;
import com.drs.beam.server.modules.data.dao.locations.LocationsDao;
import com.drs.beam.server.modules.data.dao.tasks.TasksDao;
import com.drs.beam.server.modules.io.InnerIOModule;

/*
 * 
 */
public class DataManager implements DataManagerModule{
    // Fields =============================================================================
    private static DataManager dataManager;
    
    private final DataBase dataBase;
    private final DaoProvider daoProvider;
    
    // Constructor ========================================================================
    private DataManager(DataBase data, InnerIOModule io) {
        this.dataBase = data;
        this.daoProvider = new DaoProvider(io, data.getName());
    }
        
    // Methods ============================================================================
    
    public static void initAndRegister(InnerIOModule io){
        if (dataManager == null){
            DataBaseProvider provider = new DataBaseProvider(io);
            DataBaseVerifier verifier = new DataBaseVerifier(io);
            
            DataBase db = provider.getDataBase();
            verifier.verifyDataBase(db);
            
            dataManager = new DataManager(db, io);
            Modules.registerModule(DataManagerModule.getModuleName(), dataManager);
        }
    }
    
    
    
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
