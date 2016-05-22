/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import java.lang.reflect.Constructor;

import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.exceptions.NullDependencyInjectionException;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;

/**
 * Implements DataModule.
 * 
 * @author Diarsid
 */
class DataModuleWorker implements DataModule {
    
    private final IoInnerModule ioEngine; 
    private final DataBase dataBase;   
    private final String daosPackageName;
        
    DataModuleWorker(
            IoInnerModule io,
            DataBase dataBase,
            String daosPackageName) {
        if (dataBase == null) {
            throw new NullDependencyInjectionException(
                    DataModuleWorker.class.getSimpleName(), 
                    DataBase.class.getSimpleName());
        }
        this.dataBase = dataBase;
        this.ioEngine = io;
        this.daosPackageName = daosPackageName;
    }
    
    @Override
    public void stopModule() {
        this.dataBase.disconnect();
    }
        
    @Override
    public DaoTasks getTasksDao() {
        return (DaoTasks) this.assembleConcreteDao(DaoTasks.class);        
    }
    
    @Override
    public HandlerLocations getLocationsHandler() {
        DaoLocations dao = (DaoLocations) this.assembleConcreteDao(DaoLocations.class);    
        return new HandlerWorkerLocations(this.ioEngine, dao);
    }
    
    @Override
    public DaoCommands getCommandsDao() { 
        return (DaoCommands) this.assembleConcreteDao(DaoCommands.class);        
    }  
    
    @Override
    public HandlerWebPages getWebPagesHandler() {
        DaoWebPages dao = (DaoWebPages) this.assembleConcreteDao(DaoWebPages.class);
        return new HandlerWorkerWebPages(this.ioEngine, dao);
    }
    
    @Override
    public DaoExecutorIntelligentChoices getIntellChoiceDao() {
        return (DaoExecutorIntelligentChoices) this.assembleConcreteDao(DaoExecutorIntelligentChoices.class);
    }
    
    private Object assembleConcreteDao(Class daoInterface) {
        try {
            String daoClassName = 
                    this.daosPackageName + 
                    this.dataBase.getName() + 
                    daoInterface.getSimpleName();
            
            Class daoClass = Class.forName(daoClassName);
            Constructor daoConstr = daoClass.getDeclaredConstructor(
                    IoInnerModule.class, DataBase.class);
            daoConstr.setAccessible(true);
            
            return daoConstr.newInstance(this.ioEngine, this.dataBase);
            
        } catch (ClassNotFoundException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DataManager: Dao implementation class not found by its name.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        } catch (NoSuchMethodException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DataManager: Dao constructor creation failure.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        } catch (Exception e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "DataManager: " + daoInterface.getSimpleName() +
                    " instance creation failure.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }
}
