/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.server.modules.data;

import com.drs.beam.server.modules.Module;
import com.drs.beam.server.modules.data.base.DataBase;
import com.drs.beam.server.modules.data.dao.commands.CommandsDao;
import com.drs.beam.server.modules.data.dao.locations.LocationsDao;
import com.drs.beam.server.modules.data.dao.tasks.TasksDao;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public interface DataManagerModule extends Module {
    
    TasksDao getTasksDao();    
    LocationsDao getLocationsDao();  
    CommandsDao getCommandsDao(); 
    
    static String getModuleName(){
        return "Data Manager Module";
    }
    
    static DataManagerModule buildModule(InnerIOModule ioModule){        
        DataBaseInitializer initializer = new DataBaseInitializer(ioModule);
        DataBaseModel dataModel = new DataBaseModel();
        DataBaseVerifier verifier = new DataBaseVerifier(ioModule, initializer, dataModel);
        DataBaseProvider provider = new DataBaseProvider(ioModule);
        
        DataBase dataBase = provider.getDataBase();
        verifier.verifyDataBase(dataBase);
        
        DaoProvider daoProvider = new DaoProvider(ioModule, dataBase);
        
        DataManagerModule dataModule = new DataManager(dataBase, daoProvider);
        return dataModule;
    }
}
