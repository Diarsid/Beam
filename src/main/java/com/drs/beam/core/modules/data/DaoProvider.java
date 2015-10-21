/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.data;

import java.lang.reflect.Constructor;

import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.data.base.DataBase;
import com.drs.beam.core.modules.data.dao.commands.CommandsDao;
import com.drs.beam.core.modules.data.dao.locations.LocationsDao;
import com.drs.beam.core.modules.data.dao.tasks.TasksDao;
import com.drs.beam.core.modules.exceptions.ModuleInitializationException;

/**
 *
 * @author Diarsid
 */
class DaoProvider {
    // Fields =============================================================================
    
    private final InnerIOModule ioEngine;    
    private final String tasksDaoClassName;
    private final String locationsDaoClassName;
    private final String commandsDaoClassName;
    
    // Constructors =======================================================================
    
    DaoProvider(InnerIOModule io, DataBase dataBase) {
        String dbName = dataBase.getName();
        this.ioEngine = io;
        this.tasksDaoClassName = TasksDao.class.getCanonicalName() + dbName;
        this.locationsDaoClassName = LocationsDao.class.getCanonicalName() + dbName;
        this.commandsDaoClassName = CommandsDao.class.getCanonicalName() + dbName;
    }
    
    // Methods ============================================================================
    
    TasksDao createTasksDao(DataBase db){        
        Constructor cons = this.createDaoConstructor(this.tasksDaoClassName);
        return (TasksDao) this.getNewInstance(cons, db, "TasksDao");        
    }
    
    LocationsDao createLocationsDao(DataBase db){       
        Constructor cons = this.createDaoConstructor(this.locationsDaoClassName);
        return (LocationsDao) this.getNewInstance(cons, db, "LocationsDao");        
    }
    
    CommandsDao createCommandsDao(DataBase db){        
        Constructor cons = this.createDaoConstructor(this.commandsDaoClassName);
        return (CommandsDao) this.getNewInstance(cons, db, "CommandsDao");        
    }   
    
    private Constructor createDaoConstructor(String daoClassName){
        try {
            Class daoClass = Class.forName(daoClassName);
            Constructor daoConstr = daoClass.getConstructor(DataBase.class);
            return daoConstr;
        } catch (ClassNotFoundException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DaoProvider: Dao implementation class not found by its name.",
                    "Programm will be closed.");
            throw new ModuleInitializationException();
        } catch (NoSuchMethodException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DaoProvider: Dao constructor creation failure.", 
                    "Programm will be closed.");
            throw new ModuleInitializationException();
        } 
    }
    
    private Object getNewInstance(Constructor cons, DataBase db, String daoType){
        try{
            return cons.newInstance(db);
        } catch (Exception e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DaoProvider: "+daoType+" instance creation failure.", 
                    "Programm will be closed.");
            throw new ModuleInitializationException();
        }
    }
}
