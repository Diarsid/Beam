/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.data;

import java.lang.reflect.Constructor;

import com.drs.beam.core.exceptions.NullDependencyInjectionException;
import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.exceptions.ModuleInitializationException;

/**
 *
 * @author Diarsid
 */
class DataManager implements DataManagerModule {
    
    private final InnerIOModule ioEngine; 
    private final DataBase dataBase;   
    private final String daosPackageName;
        
    DataManager(InnerIOModule io, DataBase dataBase, String daosPackageName) {
        if (dataBase == null){
            throw new NullDependencyInjectionException(
                    DataManager.class.getSimpleName(), 
                    DataBase.class.getSimpleName());
        }
        this.dataBase = dataBase;
        this.ioEngine = io;
        this.daosPackageName = daosPackageName;
    }
        
    @Override
    public DaoTasks getTasksDao(){
        return (DaoTasks) this.assembleConcreteDao(DaoTasks.class);        
    }
    
    @Override
    public DaoLocations getLocationsDao(){
        return (DaoLocations) this.assembleConcreteDao(DaoLocations.class);    
    }
    
    @Override
    public DaoCommands getCommandsDao(){ 
        return (DaoCommands) this.assembleConcreteDao(DaoCommands.class);        
    }  
    
    @Override
    public DaoWebPages getWebPagesDao(){
        return (DaoWebPages) this.assembleConcreteDao(DaoWebPages.class);
    }
    
    private Object assembleConcreteDao(Class daoInterface){
        try {
            
            String daoType = daoInterface.getSimpleName();
            String daoClassName = this.daosPackageName + this.dataBase.getName() + daoType;
            
            Class daoClass = Class.forName(daoClassName);
            Constructor daoConstr = daoClass.getDeclaredConstructor(InnerIOModule.class, DataBase.class);
            daoConstr.setAccessible(true);
            
            return daoConstr.newInstance(this.ioEngine, this.dataBase);
            
        } catch (ClassNotFoundException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DataManager: Dao implementation class not found by its name.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        } catch (NoSuchMethodException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DataManager: Dao constructor creation failure.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        } catch (Exception e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DataManager: "+daoInterface.getSimpleName()+" instance creation failure.", 
                    "Program will be closed.");
            e.printStackTrace();
            throw new ModuleInitializationException();
        }
    }
}
