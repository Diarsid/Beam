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

import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.ConfigHolderModule;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.CoreRemoteManagerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.remotemanager.endpointholders.RemoteAccessEndpointHolder;
import diarsid.beam.core.rmi.RemoteCoreAccessEndpoint;

import static java.rmi.registry.LocateRegistry.getRegistry;

import static diarsid.beam.core.Beam.saveRmiInterfacesInStaticContext;
import static diarsid.beam.core.config.Config.CORE_ACCESS_ENDPOINT_NAME;
import static diarsid.beam.core.config.Config.CORE_PORT;
import static diarsid.beam.core.control.io.base.Message.MessageType.ERROR;
import static diarsid.beam.core.util.Logs.debug;


public class CoreRemoteManagerModuleWorker implements CoreRemoteManagerModule {
    
    private final RemoteCoreAccessEndpoint remoteAccessEndpoint;
    
    private final ConfigHolderModule config;
    private final IoModule io;
    
    public CoreRemoteManagerModuleWorker(
            ConfigHolderModule configModule, 
            CoreControlModule coreControlModule, 
            IoModule ioModule) {
        this.config = configModule;
        this.io = ioModule;
        this.remoteAccessEndpoint = new RemoteAccessEndpointHolder(ioModule, coreControlModule);
        this.exportAndSaveExportedEndpoints();
    }
    
    private void exportAndSaveExportedEndpoints() {
        saveRmiInterfacesInStaticContext(this);
        try {
            int beamCorePort = Integer.parseInt(config.get(CORE_PORT));
            Registry registry = LocateRegistry.createRegistry(beamCorePort);
            RemoteCoreAccessEndpoint access = 
                    (RemoteCoreAccessEndpoint) UnicastRemoteObject.exportObject(
                            this.remoteAccessEndpoint, beamCorePort);

            registry.bind(config.get(CORE_ACCESS_ENDPOINT_NAME), access);
            debug("Core endpoints exported successfully");
        } catch (AlreadyBoundException|RemoteException e) {            
            this.io.getInnerIoEngine().reportMessageAndExitLater(this.io.getSystemInitiator(), 
                    new TextMessage(ERROR, 
                            "Export Beam.Server modules failure.",
                            "Program will be closed.")
            );
            throw new ModuleInitializationException();
        }
    }

    @Override
    public void stopModule() {
        int beamCorePort = Integer.parseInt(config.get(CORE_PORT));
        try {            
            Registry registry = getRegistry(beamCorePort);
            registry.unbind(config.get(CORE_ACCESS_ENDPOINT_NAME));
//            registry.unbind(config.get(EXECUTOR_NAME));
//            registry.unbind(config.get(TASK_MANAGER_NAME));
//            registry.unbind(config.get(LOCATIONS_HANDLER_NAME));
//            registry.unbind(config.get(WEB_PAGES_HANDLER_NAME));
        } catch (NotBoundException|RemoteException e) {            
            this.io.getInnerIoEngine()
                    .reportMessage(this.io.getSystemInitiator(), new TextMessage(e));
        }        
    }

    @Override
    public RemoteCoreAccessEndpoint getRemoteAccessEndpoint() {
        return this.remoteAccessEndpoint;
    }
}
