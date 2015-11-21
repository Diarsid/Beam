/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.TaskManagerModule;
import com.drs.beam.core.modules.IoModule;
import com.drs.beam.core.modules.RmiModule;

/**
 *
 * @author Diarsid
 */
public interface Modules {
    
    /**
     * Initializes and registers ConfigModule which provides necessary configuration data. 
     * Accepts main() method String[] args parameter and parses it into configuration data.
     * 
     * @param args in following format = {"param1=value1", "param2=value2", ... "paramX=valueX"}.
     */
    void initConfigModule(String[] args);
    
    /**
     * Initializes and registers IoModule which is responsible for interaction with
     * external IO through through RMI using ExternalIOInterface.
     */
    void initIoModule();
    
    /**
     * Initializes and registers InnerIOModule that is used by other modules to perform
     * output and interaction with user. 
     * Requires IoModule and ConfigModule.
     */
    void initInnerIoModule();
    
    /**
     * Initializes and registers DataModule which is responsible for interaction with data base.
     * Requires InnerIoModule and ConfigModule.
     */
    void initDataModule();

    /**
     * Initializes and registers ExecutorModule which is responsible for executing commands
     * and interacts with underlying operation system for opening files and directories.
     * Requires InnerIoModule, ConfigModule and DataModule.
     */
    void initExecutorModule();

    /**
     * Initializes and registers TaskManagerModule which is responsible for processing tasks
     * and calendar events.
     * Requires InnerIoModule, ConfigModule and DataModule.
     */
    void initTaskManagerModule();
    
    /**
     * Initializes and registers RmiModule which is responsible for exporting other modules
     * through Java RMI mechanism.
     * Requires InnerIOModule, ConfigModule. Exports ExecutorModule, TaskManagerModule and 
     * RemoteControlModule.
     */
    void initRmiModule();

    /**
     * @return Returns ConfigModule instance that has been properly initialized and is 
     * ready to work.
     */
    ConfigModule getConfigModule();
    
    /**
     * @return IoModule instance that has been properly initialized and is
     * ready to work.
     */
    IoModule getIoModule();
    
    /**
     * @return Returns InnerIoModule instance that has been properly initialized and is
     * ready to work.
     */
    InnerIOModule getInnerIOModule();
        
    /**
     * @return Returns DataModule instance that has been properly initialized and is
     * ready to work.
     */
    DataManagerModule getDataModule();

    /**
     * @return Returns ExecutorModule instance that has been properly initialized and is
     * ready to work.
     */
    ExecutorModule getExecutorModule();    

    /**
     * @return Returns TasksManagerModule instance that has been properly initialized and is
     * ready to work.
     */
    TaskManagerModule getTasksManagerModule();
    
    /**
     * @return Returns RmiModule instance that has been properly initialized and is
     * ready to work.
     */
    RmiModule getRmiModule();
}
