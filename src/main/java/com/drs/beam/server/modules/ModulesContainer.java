/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.modules;

import java.util.HashMap;
import java.util.Map;

import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.executor.ExecutorModule;
import com.drs.beam.server.modules.io.BeamIO;
import com.drs.beam.server.modules.io.InnerControlModule;
import com.drs.beam.server.modules.io.InnerIOModule;
import com.drs.beam.server.modules.io.RemoteControlModule;
import com.drs.beam.server.modules.tasks.TaskManagerModule;

/**
 *
 * @author Diarsid
 */
public class ModulesContainer {
// ________________________________________________________________________________________
//                                       Fields                                            
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    private final Map<String, Module> modules = new HashMap<>();

// ________________________________________________________________________________________
//                                       Methods                                           
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        
    // Methods for proper initialization of appropriate modules ===========================
    
    /**
     * Initializes and registers IoModule. Initializes three modules simultaneously - RemoteControlModule, 
     * InnerControlModule and InnerIoModule.
     * IoModule does not need any other modules.
     */
    public void initIoModule(){
        BeamIO io = new BeamIO();
        this.modules.put(RemoteControlModule.getModuleName(), io);
        this.modules.put(InnerIOModule.getModuleName(), io);
        this.modules.put(InnerControlModule.getModuleName(), io);
    }
    
    /**
     * Initializes and registers DataModule which is responsible for interaction with data base.
     * Requires InnerIoModule.
     */
    public void initDataModule(){
        InnerIOModule ioModule = this.getInnerIOModule();
        
        DataManagerModule dataModule = DataManagerModule.buildModule(ioModule);
        
        this.modules.put(DataManagerModule.getModuleName(), dataModule);
    }
    
    /**
     * Initializes and registers TaskManagerModule which is responsible for processing tasks 
     * and calendar events.
     * Requires InnerIoModule and DataModule.
     */
    public void initTaskManagerModule(){
        InnerIOModule ioModule = this.getInnerIOModule();
        DataManagerModule dataModule = this.getDataModule();
        
        TaskManagerModule taskModule = TaskManagerModule.buildModule(ioModule, dataModule);
        
        this.modules.put(TaskManagerModule.getModuleName(), taskModule);        
    }
    
    /**
     * Initializes and registers ExecutorModule which is responsible for executing commands 
     * and interacts with underlying operation system for opening files and directories.
     * Requires InnerIoModule and DataModule.
     */
    public void initExecutorModule(){       
        InnerIOModule ioModule = this.getInnerIOModule();
        DataManagerModule dataModule = this.getDataModule();
        
        ExecutorModule executorModule = ExecutorModule.buildModule(ioModule, dataModule);
        
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
    public InnerIOModule getInnerIOModule(){
        return (InnerIOModule) this.getModule(InnerIOModule.getModuleName());        
    }
    
    /**
     * @return Returns RemoteControlModule instance that has been properly initialized and is 
     * ready to work.
     */
    public RemoteControlModule getRemoteControlModule(){
        return (RemoteControlModule) this.getModule(RemoteControlModule.getModuleName());
    }
    
    /**
     * @return Returns InnerControlModule instance that has been properly initialized and is 
     * ready to work.
     */    
    public InnerControlModule getInnerControlModule(){
        return (InnerControlModule) this.getModule(InnerControlModule.getModuleName());   
    }
    
    /**
     * @return Returns DataModule instance that has been properly initialized and is 
     * ready to work.
     */
    public DataManagerModule getDataModule(){
        return (DataManagerModule) this.getModule(DataManagerModule.getModuleName());   
    }
    
    /**
     * @return Returns ExecutorModule instance that has been properly initialized and is 
     * ready to work.
     */
    public ExecutorModule getExecutorModule(){
        return (ExecutorModule) this.getModule(ExecutorModule.getModuleName());   
    }
    
    /**
     * @return Returns TasksManagerModule instance that has been properly initialized and is 
     * ready to work.
     */
    public TaskManagerModule getTasksManagerModule() {
        return (TaskManagerModule) this.getModule(TaskManagerModule.getModuleName());   
    }   
}
