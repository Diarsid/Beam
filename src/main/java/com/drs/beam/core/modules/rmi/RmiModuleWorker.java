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
import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.IoModule;
import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.modules.TaskManagerModule;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
class RmiModuleWorker implements RmiModule {
    // Fields =============================================================================
    
    private final IoInnerModule ioEngine;
    private final ConfigModule config;
    
    private final RmiTaskManagerInterface rmiTaskManagerInterface;
    private final RmiExecutorInterface rmiExecutorInterface;
    private final RmiRemoteControlInterface rmiRemoteControlInterface;
    private final RmiLocationsHandlerInterface rmiLocationsHandlerInterface;
    private final RmiWebPageHandlerInterface rmiWebPageHandlerInterface;
    
    RmiModuleWorker(
            IoModule ioModule,
            IoInnerModule innerIoModule, 
            ConfigModule configModule,
            DataModule dataModule,
            ExecutorModule executorModule,
            TaskManagerModule taskManagerModule) {
        
        this.ioEngine = innerIoModule;
        this.config = configModule;
        
        this.rmiExecutorInterface = new RmiAdapterForExecutor(executorModule);
        this.rmiTaskManagerInterface = new RmiAdapterForTaskManager(taskManagerModule);
        this.rmiRemoteControlInterface = new RmiAdapterForRemoteControl(ioModule);
        this.rmiLocationsHandlerInterface = new RmiAdapterForLocationsHandler(
                dataModule.getLocationsDao(), executorModule);
        this.rmiWebPageHandlerInterface = new RmiAdapterForWebPageHandler(
                dataModule.getWebPagesDao());
    }
    
    // Methods ============================================================================
    
    @Override
    public RmiExecutorInterface getRmiExecutorInterface() {
        return this.rmiExecutorInterface;
    }
    
    @Override
    public RmiTaskManagerInterface getRmiTaskManagerInterface() {
        return this.rmiTaskManagerInterface;
    }
    
    @Override
    public RmiRemoteControlInterface getRmiRemoteControlInterface() {
        return this.rmiRemoteControlInterface;
    }

    @Override
    public RmiLocationsHandlerInterface getRmiLocationsHandlerInterface() {
        return rmiLocationsHandlerInterface;
    }

    @Override
    public RmiWebPageHandlerInterface getRmiWebPageHandlerInterface() {
        return rmiWebPageHandlerInterface;
    }
    
    @Override
    public void exportInterfaces() {
        
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try {            
            int beamCorePort = Integer.parseInt(config.getParameter(ConfigParam.BEAMCORE_PORT));
            Registry registry = LocateRegistry.createRegistry(beamCorePort);
            RmiRemoteControlInterface orgIOStub = 
                    (RmiRemoteControlInterface) UnicastRemoteObject.exportObject(
                            this.rmiRemoteControlInterface, beamCorePort);

            RmiExecutorInterface osExecutorStub =
                    (RmiExecutorInterface) UnicastRemoteObject.exportObject(
                            this.rmiExecutorInterface, beamCorePort);

            RmiTaskManagerInterface TaskManagerStub =
                    (RmiTaskManagerInterface) UnicastRemoteObject.exportObject(
                            this.rmiTaskManagerInterface, beamCorePort);
            
            RmiLocationsHandlerInterface LocationsHandlerStub = 
                    (RmiLocationsHandlerInterface) UnicastRemoteObject.exportObject(
                            this.rmiLocationsHandlerInterface, beamCorePort);
            
            RmiWebPageHandlerInterface WebPagesHandlerStub = 
                    (RmiWebPageHandlerInterface) UnicastRemoteObject.exportObject(
                            this.rmiWebPageHandlerInterface, beamCorePort);

            registry.bind(config.getParameter(ConfigParam.BEAM_ACCESS_NAME), orgIOStub);
            registry.bind(config.getParameter(ConfigParam.EXECUTOR_NAME), osExecutorStub);
            registry.bind(config.getParameter(ConfigParam.TASK_MANAGER_NAME), TaskManagerStub);
            registry.bind(config.getParameter(ConfigParam.LOCATIONS_HANDLER_NAME), LocationsHandlerStub);
            registry.bind(config.getParameter(ConfigParam.WEB_PAGES_HANDLER_NAME), WebPagesHandlerStub);

        } catch (AlreadyBoundException|RemoteException e) {            
            ioEngine.reportExceptionAndExitLater(e, 
                    "Export Beam.Server modules failure.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }     
}
