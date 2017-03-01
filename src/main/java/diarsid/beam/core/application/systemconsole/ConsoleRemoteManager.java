/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;

import diarsid.beam.core.application.configuration.Configuration;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.base.rmi.RemoteOuterIoEngine;

import static java.lang.Integer.parseInt;
import static java.rmi.registry.LocateRegistry.createRegistry;
import static java.rmi.registry.LocateRegistry.getRegistry;
import static java.rmi.server.UnicastRemoteObject.exportObject;
import static java.util.Objects.isNull;

import static diarsid.beam.core.application.systemconsole.SystemConsole.getPassport;
import static diarsid.beam.core.application.systemconsole.SystemConsoleLog.consoleDebug;
import static diarsid.beam.core.base.rmi.RmiComponentNames.CORE_ACCESS_ENDPOINT_NAME;
import static diarsid.beam.core.base.rmi.RmiComponentNames.SYS_CONSOLE_NAME;

/**
 *
 * @author Diarsid
 */
public class ConsoleRemoteManager {
    
    private int consoleRegistryPort;
    private final String consoleRegistryHost;
    private String consoleName;
    
    private final int coreRegistryPort;
    private final String coreRegistryHost;
    private final String coreAccessEndpointName;
    
    public ConsoleRemoteManager(Configuration config) {               
        this.consoleRegistryPort = parseInt(config.getAsString("rmi.sysconsole.port"));
        this.consoleRegistryHost = config.getAsString("rmi.sysconsole.host");
        this.consoleName = SYS_CONSOLE_NAME;
        
        this.coreAccessEndpointName = CORE_ACCESS_ENDPOINT_NAME;
        this.coreRegistryPort = parseInt(config.getAsString("rmi.core.port"));
        this.coreRegistryHost = config.getAsString("rmi.core.host");        
    }
    
    void export(ConsoleController console) {
        try {            
            RemoteCoreAccessEndpoint remoteAccess = importRemoteAccess();            
            console.setRemoteAccess(remoteAccess);
                        
            Registry registry = null;
            boolean registryNotCreated = true;
            int otherConsolesCounter = 0;
            while ( registryNotCreated ) {                
                try {
                    registry = createRegistry(this.consoleRegistryPort); 
                    registryNotCreated = false;
                    consoleDebug("port: " + this.consoleRegistryPort + " is free.");
                    getPassport().setPort(this.consoleRegistryPort);
                } catch (ExportException ex) {
                    consoleDebug("port: " + this.consoleRegistryPort + " is in use.");
                    this.consoleRegistryPort++;
                    otherConsolesCounter++;
                }
            }            
            if ( isNull(registry) ) {
                throw new StartupFailedException("Cannot create or obtain RMI Registry.");
            }             
            consoleDebug("registry created.");
            
            RemoteOuterIoEngine remoteConsole = new RemoteConsoleAdpater(console);
            RemoteOuterIoEngine consoleStub =
                    (RemoteOuterIoEngine) exportObject(remoteConsole, this.consoleRegistryPort);
            
            this.consoleName = this.consoleName + otherConsolesCounter;
            
            registry.bind(this.consoleName, consoleStub);            
            getPassport().setName(this.consoleName);
            remoteAccess.acceptRemoteOuterIoEngine(
                    this.consoleName, 
                    this.consoleRegistryHost, 
                    this.consoleRegistryPort);
            consoleDebug("Console is binded with core successfully.");
            
        } catch (AlreadyBoundException|NotBoundException|RemoteException e) {
            throw new StartupFailedException(e);
        }
    }

    private RemoteCoreAccessEndpoint importRemoteAccess() 
            throws NumberFormatException, RemoteException, NotBoundException {
        RemoteCoreAccessEndpoint remoteAccess = (RemoteCoreAccessEndpoint) getRegistry(
                        this.coreRegistryHost,
                        this.coreRegistryPort
                ).lookup(this.coreAccessEndpointName);
        consoleDebug("RemoteAccess is imported successfully.");
        return remoteAccess;
    }
}
