/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.remotemanager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import diarsid.beam.core.control.io.base.IoMessage;
import diarsid.beam.core.modules.ConfigModule;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.CoreRemoteManagerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.remotemanager.endpointholders.RemoteAccessEndpointHolder;
import diarsid.beam.core.rmi.RemoteAccessEndpoint;

import static java.rmi.registry.LocateRegistry.getRegistry;

import static diarsid.beam.core.modules.config.Config.CORE_ACCESS_ENDPOINT;
import static diarsid.beam.core.modules.config.Config.CORE_PORT;


public class CoreRemoteManagerModuleWorker implements CoreRemoteManagerModule {
    
    private final RemoteAccessEndpoint remoteAccessEndpoint;
    
    private final ConfigModule config;
    private final IoModule io;
    
    public CoreRemoteManagerModuleWorker(
            ConfigModule configModule, 
            CoreControlModule coreControlModule, 
            IoModule ioModule) {
        this.config = configModule;
        this.io = ioModule;
        this.remoteAccessEndpoint = new RemoteAccessEndpointHolder(ioModule, coreControlModule);
    }
    
    private void exportEndpoints() {
        
    }
    
    private void saveExportedEndpoints() {
        
    }

    @Override
    public void stopModule() {
        int beamCorePort = Integer.parseInt(config.get(CORE_PORT));
        try {            
            Registry registry = getRegistry(beamCorePort);
            registry.unbind(config.get(CORE_ACCESS_ENDPOINT));
//            registry.unbind(config.get(EXECUTOR_NAME));
//            registry.unbind(config.get(TASK_MANAGER_NAME));
//            registry.unbind(config.get(LOCATIONS_HANDLER_NAME));
//            registry.unbind(config.get(WEB_PAGES_HANDLER_NAME));
        } catch (NotBoundException|RemoteException e) {            
            this.io.getInnerIoEngine()
                    .reportMessage(this.io.getSystemInitiator(), new IoMessage(e));
        }
    }
}
