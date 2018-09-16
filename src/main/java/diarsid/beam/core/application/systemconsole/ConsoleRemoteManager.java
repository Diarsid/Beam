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

import diarsid.support.configuration.Configuration;
import diarsid.beam.core.base.control.io.base.console.Console;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.base.rmi.RemoteOuterIoEngine;

import static java.lang.Integer.parseInt;
import static java.rmi.registry.LocateRegistry.createRegistry;
import static java.rmi.registry.LocateRegistry.getRegistry;
import static java.rmi.server.UnicastRemoteObject.exportObject;
import static java.util.Objects.isNull;

import static diarsid.beam.core.application.systemconsole.SystemConsole.getPassport;
import static diarsid.beam.core.base.rmi.RmiComponentNames.CORE_ACCESS_ENDPOINT_NAME;
import static diarsid.beam.core.base.rmi.RmiComponentNames.SYS_CONSOLE_NAME;
import static diarsid.support.log.Logging.logFor;

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
        this.consoleRegistryPort = parseInt(config.asString("rmi.sysconsole.port"));
        this.consoleRegistryHost = config.asString("rmi.sysconsole.host");
        this.consoleName = SYS_CONSOLE_NAME;
        
        this.coreAccessEndpointName = CORE_ACCESS_ENDPOINT_NAME;
        this.coreRegistryPort = parseInt(config.asString("rmi.core.port"));
        this.coreRegistryHost = config.asString("rmi.core.host");        
    }
    
    void export(Console console) {
        try {     
            Registry registry = null;
            boolean registryNotCreated = true;
            int otherConsolesCounter = 0;
            while ( registryNotCreated ) {                
                try {
                    registry = createRegistry(this.consoleRegistryPort); 
                    registryNotCreated = false;
                    logFor(this).info("port: " + this.consoleRegistryPort + " is free.");
                    getPassport().setPort(this.consoleRegistryPort);
                } catch (ExportException ex) {
                    logFor(this).info("port: " + this.consoleRegistryPort + " is in use.");
                    this.consoleRegistryPort++;
                    otherConsolesCounter++;
                }
            }            
            if ( isNull(registry) ) {
                throw new StartupFailedException("Cannot create or obtain RMI Registry.");
            }      
            ConsoleRemoteObjectsHolder.holdedRegistry = registry;
            logFor(this).info("registry created.");
            
            RemoteOuterIoEngine remoteConsole = new RemoteConsoleAdpater(console);
            RemoteOuterIoEngine remoteConsoleExported =
                    (RemoteOuterIoEngine) exportObject(remoteConsole, this.consoleRegistryPort);
            
            this.consoleName = this.consoleName + otherConsolesCounter;
            
            registry.bind(this.consoleName, remoteConsoleExported);  
            ConsoleRemoteObjectsHolder.holdedRemoteConsole = remoteConsole;
            ConsoleRemoteObjectsHolder.holdedRemoteConsoleExported = remoteConsoleExported;
            getPassport().setName(this.consoleName);
            ConsoleRemoteObjectsHolder.holdedRemoteCoreAccess.acceptRemoteOuterIoEngine(
                    this.consoleName, 
                    this.consoleRegistryHost, 
                    this.consoleRegistryPort);
            logFor(this).info("Console is binded with core successfully.");
            
        } catch (AlreadyBoundException|NotBoundException|RemoteException e) {
            throw new StartupFailedException(e);
        }
    }

    RemoteCoreAccessEndpoint importRemoteAccess() {
        try {
            RemoteCoreAccessEndpoint remoteAccess = 
                    (RemoteCoreAccessEndpoint) getRegistry(
                            this.coreRegistryHost,
                            this.coreRegistryPort).lookup(this.coreAccessEndpointName);
            logFor(this).info("RemoteAccess is imported successfully.");
            ConsoleRemoteObjectsHolder.holdedRemoteCoreAccess = remoteAccess;
            return remoteAccess;
        } catch (NumberFormatException|NotBoundException|RemoteException e) {
            throw new StartupFailedException(e);
        }
    }
}
