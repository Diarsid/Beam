/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.data;

import java.lang.reflect.Constructor;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.data.base.DataBase;
import com.drs.beam.core.modules.exceptions.ModuleInitializationException;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
class DataBaseProvider {
    // Fields =============================================================================
    
    private final InnerIOModule ioEngine;
    private final ConfigModule config;
    
    // Constructors =======================================================================
    DataBaseProvider(InnerIOModule io, ConfigModule config){    
        this.ioEngine = io;
        this.config = config;
    }

    // Methods ============================================================================
    DataBase getDataBase() {        
        this.loadDriver();
        return this.dataBaseInstantiation();
    }
    
    private void loadDriver(){
        try {
            Class.forName(this.config.getParameter(ConfigParam.CORE_DB_DRIVER));
        } catch (Exception e) {
            // If there is any problem during database driver loading program can not 
            // work further and must be finished.
            this.handleExceptionAndExit(e, "Data Base Driver loading failure.");
            throw new ModuleInitializationException();
        }
    }
    
    private DataBase dataBaseInstantiation(){
        try {
            String concreteDataBaseName = this.config.getParameter(ConfigParam.CORE_DB_NAME);
            String url = this.config.getParameter(ConfigParam.CORE_DB_URL);
            String dbImplementationName = 
                    DataBase.class.getCanonicalName() +
                    concreteDataBaseName;
            Class dbImplementationClass = Class.forName(dbImplementationName);
            Constructor dbImplementationConstructor = 
                    dbImplementationClass.getDeclaredConstructor(String.class);
            dbImplementationConstructor.setAccessible(true);
            return (DataBase) dbImplementationConstructor.newInstance(url);
        } catch (ClassNotFoundException e) {
            this.handleExceptionAndExit(e, "DataBase implementation class not found."); 
            throw new ModuleInitializationException();
        } catch (NoSuchMethodException e) {
            this.handleExceptionAndExit(e, "Invalid DataBase implementation constructor signature."); 
            throw new ModuleInitializationException();
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            String message = e.getClass().getSimpleName() + " during connection attempt with DataBase.";
            this.handleExceptionAndExit(e, message);
            throw new ModuleInitializationException();
        } 
    }
    
    private void handleExceptionAndExit(Exception e, String problemDescription){
        try{
            Thread.sleep(1000);
        } catch(InterruptedException ie){
        }
        this.ioEngine.reportExceptionAndExitLater(e, problemDescription, "Program will be closed.");
    }
}
