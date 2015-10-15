/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.modules;

import java.util.HashMap;
import java.util.Map;

import com.drs.beam.server.modules.data.DataManager;
import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.executor.Executor;
import com.drs.beam.server.modules.executor.ExecutorModule;
import com.drs.beam.server.modules.io.BeamIO;
import com.drs.beam.server.modules.io.InnerControlModule;
import com.drs.beam.server.modules.io.InnerIOModule;
import com.drs.beam.server.modules.io.RemoteControlModule;
import com.drs.beam.server.modules.tasks.TaskManager;
import com.drs.beam.server.modules.tasks.TaskManagerModule;

/**
 *
 * @author Diarsid
 */
public class Modules {
// ________________________________________________________________________________________
//                                       Fields                                            
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    private static final Map<String, Module> modules = new HashMap<>();

// ________________________________________________________________________________________
//                                       Methods                                           
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    /**
     * Method is called by Module implementation class during new module initialization 
     * process. New Module instance will be initialized by appropriate Module implementation 
     * class and saved in Modules. This Module instance can be retrieved later by calling 
     * corresponding static get method.
     *
     * @param name      Module`s name. Stored in and can be retrieved from Module implementation
     *                  class by calling static getModuleName() method.
     * @param module    Module instance initialized by Module implementation class.
     */
    public static void registerModule(String name, Module module){
        modules.put(name, module);
    }
    
    // Methods for proper initialization of appropriate modules ===========================
    
    /**
     * Initializes and registers IoModule. Initializes three modules simultaneously - RemoteControlModule, 
     * InnerControlModule and InnerIoModule.
     * IoModule does not need any other modules.
     */
    public static void initIoModule(){
        BeamIO.initAndRegister();
    }
    
    /**
     * Initializes and registers DataModule which is responsible for interaction with data base.
     * Requires InnerIoModule.
     */
    public static void initDataModule(){
        DataManager.initAndRegister(Modules.getInnerIOModule());
    }
    
    /**
     * Initializes and registers TaskManagerModule which is responsible for processing tasks 
     * and calendar events.
     * Requires InnerIoModule and DataModule.
     */
    public static void initTaskManagerModule(){
        TaskManager.initAndRegister(Modules.getInnerIOModule(), Modules.getDataModule());
    }
    
    /**
     * Initializes and registers ExecutorModule which is responsible for executing commands 
     * and interacts with underlying operation system for opening files and directories.
     * Requires InnerIoModule and DataModule.
     */
    public static void initExecutorModule(){        
        Executor.initAndRegister(Modules.getInnerIOModule(), Modules.getDataModule());
    }
    
    // Methods for obtaining modules ======================================================
    
    /** 
     * @return Returns InnerIoModule instance that has been properly initialized and is 
     * ready to work.
     */
    public static InnerIOModule getInnerIOModule(){
        return (InnerIOModule) modules.get(InnerIOModule.getModuleName());
    }
    
    /**
     * @return Returns RemoteControlModule instance that has been properly initialized and is 
     * ready to work.
     */
    public static RemoteControlModule getRemoteControlModule(){
        return (RemoteControlModule) modules.get(RemoteControlModule.getModuleName());
    }
    
    /**
     * @return Returns InnerControlModule instance that has been properly initialized and is 
     * ready to work.
     */    
    public static InnerControlModule getInnerControlModule(){
        return (InnerControlModule) modules.get(InnerControlModule.getModuleName());
    }
    
    /**
     * @return Returns DataModule instance that has been properly initialized and is 
     * ready to work.
     */
    public static DataManagerModule getDataModule(){
        return (DataManagerModule) modules.get(DataManagerModule.getModuleName());
    }
    
    /**
     * @return Returns ExecutorModule instance that has been properly initialized and is 
     * ready to work.
     */
    public static ExecutorModule getExecutorModule(){
        return (ExecutorModule) modules.get(ExecutorModule.getModuleName());
    }
    
    /**
     * @return Returns TasksManagerModule instance that has been properly initialized and is 
     * ready to work.
     */
    public static TaskManagerModule getTasksManagerModule() {
        return (TaskManagerModule) modules.get(TaskManagerModule.getModuleName());
    }
}
