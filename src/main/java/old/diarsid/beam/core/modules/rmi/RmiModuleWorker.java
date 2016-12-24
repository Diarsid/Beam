/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.modules.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import diarsid.beam.core.exceptions.ModuleInitializationException;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.ExecutorModule;
import old.diarsid.beam.core.modules.IoInnerModule;
import old.diarsid.beam.core.modules.RmiModule;
import old.diarsid.beam.core.modules.TaskManagerModule;

import old.diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

import diarsid.beam.core.modules.ConfigModule;

import static diarsid.beam.core.modules.config.Config.CORE_PORT;
import static diarsid.beam.core.modules.config.Config.EXECUTOR_NAME;
import static diarsid.beam.core.modules.config.Config.LOCATIONS_HANDLER_NAME;
import static diarsid.beam.core.modules.config.Config.TASK_MANAGER_NAME;
import static diarsid.beam.core.modules.config.Config.WEB_PAGES_HANDLER_NAME;

import old.diarsid.beam.core.modules.OldIoModule;

import static diarsid.beam.core.modules.config.Config.CORE_ACCESS_ENDPOINT_NAME;

/**
 *
 * @author Diarsid
 */
class RmiModuleWorker implements RmiModule {
    
    private final IoInnerModule ioEngine;
    private final ConfigModule config;
    
    private final RmiTaskManagerInterface rmiTaskManagerInterface;
    private final RmiExecutorInterface rmiExecutorInterface;
    private final RmiRemoteControlInterface rmiRemoteControlInterface;
    private final RmiLocationsHandlerInterface rmiLocationsHandlerInterface;
    private final RmiWebPagesHandlerInterface rmiWebPageHandlerInterface;
    
    RmiModuleWorker(
            OldIoModule ioModule,
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
                dataModule.getLocationsHandler());
        this.rmiWebPageHandlerInterface = new RmiAdapterForWebPagesHandler(
                dataModule.getWebPagesHandler());
    }
    
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
    public RmiWebPagesHandlerInterface getRmiWebPageHandlerInterface() {
        return rmiWebPageHandlerInterface;
    }
    
    @Override
    public void exportInterfaces() {        
        
        try {            
            int beamCorePort = Integer.parseInt(config.get(CORE_PORT));
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
            
            RmiWebPagesHandlerInterface WebPagesHandlerStub = 
                    (RmiWebPagesHandlerInterface) UnicastRemoteObject.exportObject(
                            this.rmiWebPageHandlerInterface, beamCorePort);

            registry.bind(config.get(CORE_ACCESS_ENDPOINT_NAME), orgIOStub);
            registry.bind(config.get(EXECUTOR_NAME), osExecutorStub);
            registry.bind(config.get(TASK_MANAGER_NAME), TaskManagerStub);
            registry.bind(config.get(LOCATIONS_HANDLER_NAME), LocationsHandlerStub);
            registry.bind(config.get(WEB_PAGES_HANDLER_NAME), WebPagesHandlerStub);

        } catch (AlreadyBoundException|RemoteException e) {            
            ioEngine.reportExceptionAndExitLater(e, 
                    "Export Beam.Server modules failure.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }    
    
    @Override
    public void stopModule() {
        int beamCorePort = Integer.parseInt(config.get(CORE_PORT));
        try {            
            Registry registry = LocateRegistry.getRegistry(beamCorePort);
            registry.unbind(config.get(CORE_ACCESS_ENDPOINT_NAME));
            registry.unbind(config.get(EXECUTOR_NAME));
            registry.unbind(config.get(TASK_MANAGER_NAME));
            registry.unbind(config.get(LOCATIONS_HANDLER_NAME));
            registry.unbind(config.get(WEB_PAGES_HANDLER_NAME));
        } catch (NotBoundException|RemoteException e) {            
            ioEngine.reportException(e, 
                    "Unbind Beam.Server rmin module interfaces failure.");
        }        
    }
}
