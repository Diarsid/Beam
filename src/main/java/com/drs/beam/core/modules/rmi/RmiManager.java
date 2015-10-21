/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.RemoteControlModule;
import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.modules.TaskManagerModule;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
class RmiManager implements RmiModule{
    // Fields =============================================================================
    
    private final InnerIOModule ioEngine;
    private final ConfigModule config;
    
    private final RmiTaskManagerInterface rmiTaskManagerInterface;
    private final RmiExecutorInterface rmiExecutorInterface;
    private final RmiRemoteControlInterface rmiRemoteControlInterface;
    
    RmiManager(
            InnerIOModule ioEngine, 
            ConfigModule configModule,
            ExecutorModule executorModule,
            TaskManagerModule taskManagerModule,
            RemoteControlModule remoteControlModule){
        
        this.ioEngine = ioEngine;
        this.config = configModule;
        
        this.rmiExecutorInterface = new RmiExecutorAdapter(executorModule);
        this.rmiTaskManagerInterface = new RmiTaskManagerAdapter(taskManagerModule);
        this.rmiRemoteControlInterface = new RmiRemoteControlAdapter(remoteControlModule);
    }
    
    // Methods ============================================================================
    
    @Override
    public RmiExecutorInterface getRmiExecutorInterface(){
        return this.rmiExecutorInterface;
    }
    
    @Override
    public RmiTaskManagerInterface getRmiTaskManagerInterface(){
        return this.rmiTaskManagerInterface;
    }
    
    @Override
    public RmiRemoteControlInterface getRmiRemoteControlInterface(){
        return this.rmiRemoteControlInterface;
    }
    
    @Override
    public void exportInterfaces(){
        
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{            
            int organizerPort = Integer.parseInt(config.getParameter(ConfigParam.ORGANIZER_PORT));
            Registry registry = LocateRegistry.createRegistry(organizerPort);
            RmiRemoteControlInterface orgIOStub = 
                    (RmiRemoteControlInterface) UnicastRemoteObject.exportObject(
                            this.rmiRemoteControlInterface, organizerPort);

            RmiExecutorInterface osExecutorStub =
                    (RmiExecutorInterface) UnicastRemoteObject.exportObject(
                            this.rmiExecutorInterface, organizerPort);

            RmiTaskManagerInterface TaskManagerStub =
                    (RmiTaskManagerInterface) UnicastRemoteObject.exportObject(
                            this.rmiTaskManagerInterface, organizerPort);

            registry.bind(config.getParameter(ConfigParam.ORG_IO_NAME), orgIOStub);
            registry.bind(config.getParameter(ConfigParam.EXECUTOR_NAME), osExecutorStub);
            registry.bind(config.getParameter(ConfigParam.TASK_MANAGER_NAME), TaskManagerStub);

        }catch (AlreadyBoundException|RemoteException e){            
            ioEngine.reportExceptionAndExitLater(e, 
                    "Export Beam.Server modules failure.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }     
}
