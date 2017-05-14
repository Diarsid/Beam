/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.remotemanager;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.base.control.io.base.interaction.TextMessage;
import diarsid.beam.core.base.exceptions.ModuleInitializationException;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.ControlModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RemoteManagerModule;
import diarsid.beam.core.modules.remotemanager.endpointholders.RemoteCoreAccessEndpointHolder;

import static java.rmi.registry.LocateRegistry.getRegistry;

import static diarsid.beam.core.Beam.getSystemInitiator;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;
import static diarsid.beam.core.base.rmi.RmiComponentNames.CORE_ACCESS_ENDPOINT_NAME;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.modules.remotemanager.CoreRemoteObjectsHolder.holdedRegistry;
import static diarsid.beam.core.modules.remotemanager.CoreRemoteObjectsHolder.holdedRemoteAccessEndpoint;


public class RemoteManagerModuleWorker implements RemoteManagerModule {
    
    private final RemoteCoreAccessEndpoint remoteAccessEndpoint;    
    private final ApplicationComponentsHolderModule appComponentsHolderModule;
    private final IoModule io;
    
    public RemoteManagerModuleWorker(
            ApplicationComponentsHolderModule configModule, 
            ControlModule coreControlModule, 
            IoModule ioModule) {
        this.appComponentsHolderModule = configModule;
        this.io = ioModule;
        this.remoteAccessEndpoint = new RemoteCoreAccessEndpointHolder(ioModule, coreControlModule);
        this.exportAndSaveExportedEndpoints();
    }
    
    private void exportAndSaveExportedEndpoints() {
        Configuration configuration = this.appComponentsHolderModule.getConfiguration();
        try {
            int beamCorePort = Integer.parseInt(configuration.asString("rmi.core.port"));
            Registry registry = LocateRegistry.createRegistry(beamCorePort);
            RemoteCoreAccessEndpoint access = 
                    (RemoteCoreAccessEndpoint) UnicastRemoteObject.exportObject(
                            this.remoteAccessEndpoint, beamCorePort);

            registry.bind(CORE_ACCESS_ENDPOINT_NAME, access);
            holdedRegistry = registry;
            holdedRemoteAccessEndpoint = access;
            debug("Core endpoints exported successfully");
        } catch (AlreadyBoundException|RemoteException e) {            
            this.io.getInnerIoEngine().reportMessageAndExitLater(getSystemInitiator(), 
                    new TextMessage(ERROR, 
                            "Export Beam.Server modules failure.",
                            "Program will be closed.")
            );
            throw new ModuleInitializationException();
        }
    }

    @Override
    public void stopModule() {
        Configuration configuration = this.appComponentsHolderModule.getConfiguration();
        int beamCorePort = Integer.parseInt(configuration.asString("rmi.core.port"));
        try {            
            Registry registry = getRegistry(beamCorePort);
            registry.unbind(CORE_ACCESS_ENDPOINT_NAME);
//            registry.unbind(config.get(EXECUTOR_NAME));
//            registry.unbind(config.get(TASK_MANAGER_NAME));
//            registry.unbind(config.get(LOCATIONS_HANDLER_NAME));
//            registry.unbind(config.get(WEB_PAGES_HANDLER_NAME));
        } catch (NotBoundException|RemoteException e) {            
            this.io.getInnerIoEngine()
                    .reportMessage(getSystemInitiator(), new TextMessage(e));
        }        
    }

    @Override
    public RemoteCoreAccessEndpoint getRemoteAccessEndpoint() {
        return this.remoteAccessEndpoint;
    }
}
