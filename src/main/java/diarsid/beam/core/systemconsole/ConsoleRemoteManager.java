/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.rmi.RemoteAccessEndpoint;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static java.lang.Integer.parseInt;
import static java.rmi.registry.LocateRegistry.getRegistry;
import static java.util.Objects.isNull;

import static diarsid.beam.core.config.Config.CORE_ACCESS_ENDPOINT_NAME;
import static diarsid.beam.core.config.Config.CORE_HOST;
import static diarsid.beam.core.config.Config.CORE_PORT;
import static diarsid.beam.core.config.Config.SYS_CONSOLE_HOST;
import static diarsid.beam.core.config.Config.SYS_CONSOLE_NAME;
import static diarsid.beam.core.config.Config.SYS_CONSOLE_PORT;

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
        this.consoleRegistryPort = parseInt(config.get(SYS_CONSOLE_PORT));
        this.consoleRegistryHost = config.get(SYS_CONSOLE_HOST);
        this.consoleName = config.get(SYS_CONSOLE_NAME);
        
        this.coreAccessEndpointName = config.get(CORE_ACCESS_ENDPOINT_NAME);
        this.coreRegistryPort = parseInt(config.get(CORE_PORT));
        this.coreRegistryHost = config.get(CORE_HOST);        
    }
    
    void export(ConsoleController console) {
        try {            
            RemoteAccessEndpoint remoteAccess = importRemoteAccess();            
            console.setRemoteAccess(remoteAccess);
                        
            Registry registry = null;
            boolean registryNotCreated = true;
            int otherConsolesCounter = 0;
            while ( registryNotCreated ) {                
                try {
                    registry = LocateRegistry.createRegistry(this.consoleRegistryPort); 
                    registryNotCreated = false;
                } catch (ExportException ex) {
                    System.out.println("port: " + this.consoleRegistryPort + " is in use.");
                    this.consoleRegistryPort++;
                    otherConsolesCounter++;
                }
            }            
            if ( isNull(registry) ) {
                throw new StartupFailedException("Cannot create or obtain RMI Registry.");
            }             
            System.out.println("registry created.");
            
            RemoteOuterIoEngine remoteConsole = new RemoteConsoleAdpater(console);
            RemoteOuterIoEngine consoleStub =
                    (RemoteOuterIoEngine) UnicastRemoteObject.exportObject(
                            remoteConsole, this.consoleRegistryPort);
            
            this.consoleName = this.consoleName + otherConsolesCounter;
            
            registry.bind(this.consoleName, consoleStub);            
            console.setName(this.consoleName);
            remoteAccess.acceptRemoteOuterIoEngine(
                    this.consoleName, 
                    this.consoleRegistryHost, 
                    this.consoleRegistryPort);
            System.out.println("Console is binded with core successfully.");
            
        } catch(ExportException e) {
            e.printStackTrace();
            //showProblemMessageAndClose("Console export: this port already in use.");
        } catch (AlreadyBoundException abe) {
            abe.printStackTrace();
            //showProblemMessageAndClose("Console export failure: AlreadyBoundException");
        } catch (NotBoundException e) {
            e.printStackTrace();
            //showProblemMessageAndClose("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re) {
            re.printStackTrace();
            //showProblemMessageAndClose("Console export failure: RemoteException");
        }
    }

    private RemoteAccessEndpoint importRemoteAccess() 
            throws NumberFormatException, RemoteException, NotBoundException {
        RemoteAccessEndpoint remoteAccess = (RemoteAccessEndpoint) getRegistry(
                        this.coreRegistryHost,
                        this.coreRegistryPort
                ).lookup(this.coreAccessEndpointName);
        System.out.println("RemoteAccess is imported successfully.");
        return remoteAccess;
    }
}
