/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RmiModule;
import diarsid.beam.core.modules.TaskManagerModule;
import diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

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
            int beamCorePort = Integer.parseInt(config.get(Config.CORE_PORT));
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

            registry.bind(config.get(Config.BEAM_ACCESS_NAME), orgIOStub);
            registry.bind(config.get(Config.EXECUTOR_NAME), osExecutorStub);
            registry.bind(config.get(Config.TASK_MANAGER_NAME), TaskManagerStub);
            registry.bind(config.get(Config.LOCATIONS_HANDLER_NAME), LocationsHandlerStub);
            registry.bind(config.get(Config.WEB_PAGES_HANDLER_NAME), WebPagesHandlerStub);

        } catch (AlreadyBoundException|RemoteException e) {            
            ioEngine.reportExceptionAndExitLater(e, 
                    "Export Beam.Server modules failure.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }     
}
