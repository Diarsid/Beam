/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.modules.data;

import java.lang.reflect.Constructor;

import com.drs.beam.server.modules.data.base.DataBase;
import com.drs.beam.server.modules.io.InnerIOModule;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
class DataBaseProvider {
    // Fields =============================================================================
    
    private final InnerIOModule ioEngine;
    // Constructors =======================================================================
    DataBaseProvider(InnerIOModule io){    
        this.ioEngine = io;
    }

    // Methods ============================================================================
    DataBase getDataBase(InnerIOModule io) {        
        this.loadDriver();
        return this.dataBaseInstantiation();
    }
    
    private void loadDriver(){
        try {
            Class.forName(ConfigContainer.getParam(ConfigParam.CORE_DB_DRIVER));
        } catch (Exception e) {
            // If there is any problem during database driver loading program can not 
            // work further and must be finished.
            this.ioEngine.reportExceptionAndExit(e, 
                    "Data Base Driver loading failure.", 
                    "Programm will be closed.");
        }
    }
    
    private DataBase dataBaseInstantiation(){
        try {
            String concreteDataBaseName = ConfigContainer.getParam(ConfigParam.CORE_DB_NAME);
            String url = ConfigContainer.getParam(ConfigParam.CORE_DB_URL);
            String dbImplementationName = 
                    DataBase.class.getCanonicalName() +
                    concreteDataBaseName;
            Class dbImplementationClass = Class.forName(dbImplementationName);
            Constructor dbImplementationConstructor = 
                    dbImplementationClass.getConstructor(String.class);
            return (DataBase) dbImplementationConstructor.newInstance(url);
        } catch (ClassNotFoundException e) {
            this.ioEngine.reportErrorAndExit("DataBase implementation class not found.");  
            return null;
        } catch (NoSuchMethodException e) {
            this.ioEngine.reportErrorAndExit("Invalid DataBase implementation constructor signature.");
            return null;
        } catch (Exception e){
            String message = e.getClass().getSimpleName() + " during connection attempt with DataBase.";
            this.ioEngine.reportExceptionAndExit(e, 
                    message, 
                    "Programm will be closed.");
            return null;
        } 
    }
}
