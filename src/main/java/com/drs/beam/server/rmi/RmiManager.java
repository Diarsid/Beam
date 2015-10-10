/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.drs.beam.server.modules.Modules;
import com.drs.beam.server.modules.executor.ExecutorModule;
import com.drs.beam.server.modules.io.RemoteControlModule;
import com.drs.beam.server.modules.tasks.TaskManagerModule;
import com.drs.beam.server.rmi.adapters.RmiExecutorAdapter;
import com.drs.beam.server.rmi.adapters.RmiRemoteControlAdapter;
import com.drs.beam.server.rmi.adapters.RmiTaskManagerAdapter;
import com.drs.beam.server.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.server.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.server.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
public class RmiManager {
    // Fields =============================================================================
    
    private static RmiRemoteControlInterface access;
    private static RmiExecutorInterface executor;
    private static RmiTaskManagerInterface tasks;
    
    private static boolean initialized = false;
    
    // Methods ============================================================================
    
    public static void exportModules(
            RemoteControlModule accessModule, 
            ExecutorModule executorModule, 
            TaskManagerModule tasksModule) {
        
        if (! initialized){
            access = new RmiRemoteControlAdapter(accessModule);
            executor = new RmiExecutorAdapter(executorModule);
            tasks = new RmiTaskManagerAdapter(tasksModule);
            
            initialized = true;
            
            export();
        }        
    }
    
    private static void export(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{            
            int organizerPort = Integer.parseInt(ConfigContainer.getParam(ConfigParam.ORGANIZER_PORT));
            Registry registry = LocateRegistry.createRegistry(organizerPort);
            RmiRemoteControlInterface orgIOStub =
                    (RmiRemoteControlInterface) UnicastRemoteObject.exportObject(access, organizerPort);

            RmiExecutorInterface osExecutorStub =
                    (RmiExecutorInterface) UnicastRemoteObject.exportObject(executor, organizerPort);

            RmiTaskManagerInterface TaskManagerStub =
                    (RmiTaskManagerInterface) UnicastRemoteObject.exportObject(tasks, organizerPort);

            registry.bind(ConfigContainer.getParam(ConfigParam.ORG_IO_NAME), orgIOStub);
            registry.bind(ConfigContainer.getParam(ConfigParam.EXECUTOR_NAME), osExecutorStub);
            registry.bind(ConfigContainer.getParam(ConfigParam.TASK_MANAGER_NAME), TaskManagerStub);

        }catch (AlreadyBoundException|RemoteException e){            
            Modules.getInnerIOModule().reportExceptionAndExit(e, 
                    "Export Beam.Server modules failure.",
                    "Program will be closed.");
        }
    }     
}
