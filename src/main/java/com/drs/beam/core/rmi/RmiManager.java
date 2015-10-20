/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.drs.beam.core.modules.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
public class RmiManager {
    // Fields =============================================================================
    
    private final InnerIOModule ioEngine;
    
    public RmiManager(InnerIOModule ioEngine){
        this.ioEngine = ioEngine;
    }
    
    // Methods ============================================================================
    
    public void exportInterfaces(
            RmiRemoteControlInterface rmiRemoteControl, 
            RmiExecutorInterface rmiExecutor, 
            RmiTaskManagerInterface rmiTaskManager){
        
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{            
            int organizerPort = Integer.parseInt(ConfigContainer.getParam(ConfigParam.ORGANIZER_PORT));
            Registry registry = LocateRegistry.createRegistry(organizerPort);
            RmiRemoteControlInterface orgIOStub = (RmiRemoteControlInterface) UnicastRemoteObject.exportObject(rmiRemoteControl, organizerPort);

            RmiExecutorInterface osExecutorStub =
                    (RmiExecutorInterface) UnicastRemoteObject.exportObject(rmiExecutor, organizerPort);

            RmiTaskManagerInterface TaskManagerStub =
                    (RmiTaskManagerInterface) UnicastRemoteObject.exportObject(rmiTaskManager, organizerPort);

            registry.bind(ConfigContainer.getParam(ConfigParam.ORG_IO_NAME), orgIOStub);
            registry.bind(ConfigContainer.getParam(ConfigParam.EXECUTOR_NAME), osExecutorStub);
            registry.bind(ConfigContainer.getParam(ConfigParam.TASK_MANAGER_NAME), TaskManagerStub);

        }catch (AlreadyBoundException|RemoteException e){            
            ioEngine.reportExceptionAndExitLater(e, 
                    "Export Beam.Server modules failure.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }     
}
