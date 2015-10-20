/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core;

import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.TaskManagerModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.RemoteControlModule;

/**
 *
 * @author Diarsid
 */
public interface Modules {

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
     * @return Returns InnerIoModule instance that has been properly initialized and is
     * ready to work.
     */
    InnerIOModule getInnerIOModule();

    /**
     * @return Returns RemoteControlModule instance that has been properly initialized and is
     * ready to work.
     */
    RemoteControlModule getRemoteControlModule();

    /**
     * @return Returns TasksManagerModule instance that has been properly initialized and is
     * ready to work.
     */
    TaskManagerModule getTasksManagerModule();

    /**
     * Initializes and registers DataModule which is responsible for interaction with data base.
     * Requires InnerIoModule.
     */
    void initDataModule();

    /**
     * Initializes and registers ExecutorModule which is responsible for executing commands
     * and interacts with underlying operation system for opening files and directories.
     * Requires InnerIoModule and DataModule.
     */
    void initExecutorModule();
    
    /**
     * Initializes and registers IoModule. Initializes three modules simultaneously - RemoteControlModule,
     * InnerControlModule and InnerIoModule.
     * IoModule does not need any other modules.
     */
    void initIoModule();

    /**
     * Initializes and registers TaskManagerModule which is responsible for processing tasks
     * and calendar events.
     * Requires InnerIoModule and DataModule.
     */
    void initTaskManagerModule();
    
}
