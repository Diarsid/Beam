/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.base.builder;

import java.lang.reflect.Constructor;

import diarsid.beam.core.exceptions.ModuleInitializationException;

import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.ConfigModule;

import static diarsid.beam.core.modules.config.Config.CORE_DB_LOCATION;
import static diarsid.beam.core.modules.config.Config.CORE_DB_NAME;
import static diarsid.beam.core.modules.config.Config.CORE_JDBC_DRIVER;
import static diarsid.beam.core.modules.config.Config.CORE_JDBC_URL;

/**
 *
 * @author Diarsid
 */
class DataBaseProvider {
    
    private final IoInnerModule ioEngine;
    private final ConfigModule config;
    private final String dataBaseClassPackage;
    
    DataBaseProvider(
            IoInnerModule ioInnerModule, 
            ConfigModule config, 
            String dataBaseClassPackage) { 
        
        this.ioEngine = ioInnerModule;
        this.config = config;
        this.dataBaseClassPackage = dataBaseClassPackage;
    }

    DataBase getDataBase() {        
        this.loadDriver();
        return this.dataBaseInstantiation();
    }
    
    private void loadDriver() {
        try {
            Class.forName(this.config.get(CORE_JDBC_DRIVER));
        } catch (Exception e) {
            // If there is any problem during the database driver loading, 
            // the program can not work further and must be finished.
            this.handleExceptionAndExit(e, "Data Base Driver loading failure.");
            throw new ModuleInitializationException();
        }
    }
    
    private DataBase dataBaseInstantiation() {
        try {
            String concreteDataBaseName = this.config.get(CORE_DB_NAME);
            String url = 
                    this.config.get(CORE_JDBC_URL) +
                    this.config.get(CORE_DB_LOCATION);
            String dbImplementationName = 
                    this.dataBaseClassPackage + concreteDataBaseName;
            Class dbImplementationClass = Class.forName(dbImplementationName);
            Constructor dbImplementationConstructor = 
                    dbImplementationClass.getDeclaredConstructor(String.class);
            dbImplementationConstructor.setAccessible(true);
            return (DataBase) dbImplementationConstructor.newInstance(url);
        } catch (ClassNotFoundException e) {
            this.handleExceptionAndExit(
                    e, "DataBase implementation class not found."); 
            throw new ModuleInitializationException();
        } catch (NoSuchMethodException e) {
            this.handleExceptionAndExit(
                    e, "Invalid DataBase implementation constructor signature."); 
            throw new ModuleInitializationException();
        } catch (Exception e){
            String message = e.getClass().getSimpleName() + 
                    " during connection attempt with DataBase.";
            this.handleExceptionAndExit(e, message);
            throw new ModuleInitializationException();
        } 
    }    
    
    private void handleExceptionAndExit(
            Exception e, String problemDescription) {
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            throw new ModuleInitializationException();
        }
        this.ioEngine.reportExceptionAndExitLater(
                e, problemDescription, "Program will be closed.");
    }
}
