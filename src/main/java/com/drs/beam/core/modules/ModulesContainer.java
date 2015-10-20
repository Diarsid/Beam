/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules;

import java.util.HashMap;
import java.util.Map;

import com.drs.beam.core.Modules;
import com.drs.beam.core.modules.data.DataManagerModuleBuilder;
import com.drs.beam.core.modules.exceptions.ModuleInitializationOrderException;
import com.drs.beam.core.modules.executor.ExecutorModuleBuilder;
import com.drs.beam.core.modules.io.IOBuilderProvider;
import com.drs.beam.core.modules.tasks.TaskManagerModuleBuilder;

/**
 *
 * @author Diarsid
 */
class ModulesContainer implements Modules {
// ________________________________________________________________________________________
//                                       Fields                                            
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    private final Map<String, Module> modules;
    
// ________________________________________________________________________________________
//                                     Constructor                                         
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯

    public ModulesContainer() {
        modules = new HashMap<>();
    }    

// ________________________________________________________________________________________
//                                       Methods                                           
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        
    // Methods for proper initialization of appropriate modules ===========================
    
    /**
     * Initializes and registers IoModule. Initializes three modules simultaneously - RemoteControlModule, 
     * InnerControlModule and InnerIoModule.
     * IoModule does not need any other modules.
     */
    @Override
    public void initIoModule(){
        IOBuilder ioBuilder = IOBuilderProvider.createBuilder();
        
        this.modules.put(RemoteControlModule.getModuleName(), ioBuilder.buildRemoteControlModule());
        this.modules.put(InnerIOModule.getModuleName(), ioBuilder.buildInnerIOModule());
    }
    
    /**
     * Initializes and registers DataModule which is responsible for interaction with data base.
     * Requires InnerIoModule.
     */
    @Override
    public void initDataModule(){
        InnerIOModule ioModule = this.getInnerIOModule();
        
        DataManagerModule dataModule = DataManagerModuleBuilder.buildModule(ioModule);
        
        this.modules.put(DataManagerModule.getModuleName(), dataModule);
    }
    
    /**
     * Initializes and registers TaskManagerModule which is responsible for processing tasks 
     * and calendar events.
     * Requires InnerIoModule and DataModule.
     */
    @Override
    public void initTaskManagerModule(){
        InnerIOModule ioModule = this.getInnerIOModule();
        DataManagerModule dataModule = this.getDataModule();
        
        TaskManagerModule taskModule = TaskManagerModuleBuilder.buildModule(ioModule, dataModule);
        
        this.modules.put(TaskManagerModule.getModuleName(), taskModule);        
    }
    
    /**
     * Initializes and registers ExecutorModule which is responsible for executing commands 
     * and interacts with underlying operation system for opening files and directories.
     * Requires InnerIoModule and DataModule.
     */
    @Override
    public void initExecutorModule(){       
        InnerIOModule ioModule = this.getInnerIOModule();
        DataManagerModule dataModule = this.getDataModule();
        
        ExecutorModule executorModule = ExecutorModuleBuilder.buildModule(ioModule, dataModule);
        
        this.modules.put(ExecutorModule.getModuleName(), executorModule);
    }
    
    // Methods for obtaining modules ======================================================
    
    private Module getModule(String moduleName){
        if (modules.containsKey(moduleName)){
            return modules.get(moduleName);
        } else {            
            throw new ModuleInitializationOrderException();
        }
    }
    
    /** 
     * @return Returns InnerIoModule instance that has been properly initialized and is 
     * ready to work.
     */
    @Override
    public InnerIOModule getInnerIOModule(){
        return (InnerIOModule) this.getModule(InnerIOModule.getModuleName());        
    }
    
    /**
     * @return Returns RemoteControlModule instance that has been properly initialized and is 
     * ready to work.
     */
    @Override
    public RemoteControlModule getRemoteControlModule(){
        return (RemoteControlModule) this.getModule(RemoteControlModule.getModuleName());
    }
           
    /**
     * @return Returns DataModule instance that has been properly initialized and is 
     * ready to work.
     */
    @Override
    public DataManagerModule getDataModule(){
        return (DataManagerModule) this.getModule(DataManagerModule.getModuleName());   
    }
    
    /**
     * @return Returns ExecutorModule instance that has been properly initialized and is 
     * ready to work.
     */
    @Override
    public ExecutorModule getExecutorModule(){
        return (ExecutorModule) this.getModule(ExecutorModule.getModuleName());   
    }
    
    /**
     * @return Returns TasksManagerModule instance that has been properly initialized and is 
     * ready to work.
     */
    @Override
    public TaskManagerModule getTasksManagerModule() {
        return (TaskManagerModule) this.getModule(TaskManagerModule.getModuleName());   
    }   
}
