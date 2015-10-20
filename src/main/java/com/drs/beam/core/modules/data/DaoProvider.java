/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.data;

import com.drs.beam.core.modules.data.base.DataBase;

import java.lang.reflect.Constructor;

import com.drs.beam.core.modules.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.data.dao.commands.CommandsDao;
import com.drs.beam.core.modules.data.dao.locations.LocationsDao;
import com.drs.beam.core.modules.data.dao.tasks.TasksDao;
import com.drs.beam.core.modules.InnerIOModule;

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
        try {
            Constructor cons = this.createDaoConstructor(this.tasksDaoClassName);
            return (TasksDao) cons.newInstance(db);
        } catch (Exception e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DaoProvider: TasksDao instance creation failure.", 
                    "Programm will be closed.");
            throw new ModuleInitializationException();
        }
    }
    
    LocationsDao createLocationsDao(DataBase db){
        try {
            Constructor cons = this.createDaoConstructor(this.locationsDaoClassName);
            return (LocationsDao) cons.newInstance(db);
        } catch (Exception e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DaoProvider: LocationsDao instance creation failure.", 
                    "Programm will be closed.");
            throw new ModuleInitializationException();
        }
    }
    
    CommandsDao createCommandsDao(DataBase db){
        try {
            Constructor cons = this.createDaoConstructor(this.commandsDaoClassName);
            return (CommandsDao) cons.newInstance(db);
        } catch (Exception e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DaoProvider: CommandsDao instance creation failure.", 
                    "Programm will be closed.");
            throw new ModuleInitializationException();
        }
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
            return null;
        } catch (NoSuchMethodException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DaoProvider: Dao constructor creation failure.", 
                    "Programm will be closed.");
            throw new ModuleInitializationException();
        } 
    }
}
