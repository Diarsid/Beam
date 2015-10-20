/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.rmi.interfaces.adapters;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.core.modules.RemoteControlModule;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author Diarsid
 */
public class RmiRemoteControlAdapter implements RmiRemoteControlInterface{
    // Fields =============================================================================
    
    private final RemoteControlModule accessModule;
    
    // Constructors =======================================================================

    public RmiRemoteControlAdapter(RemoteControlModule accessModule) {
        this.accessModule = accessModule;
    }
    
    // Methods ============================================================================
    
    @Override
    public boolean hasExternalIOProcessor() throws RemoteException{
        return this.accessModule.hasExternalIOProcessor();
    }
    
    @Override
    public void acceptNewIOProcessor(String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException{
        Registry registry = LocateRegistry.getRegistry(consoleHost, consolePort);
        ExternalIOInterface externalIOEngine = (ExternalIOInterface) registry.lookup(consoleRmiName);
        this.accessModule.acceptNewIOProcessor(externalIOEngine);
    }
    
    @Override
    public void useExternalShowTaskMethod() throws RemoteException{
        this.accessModule.useExternalShowTaskMethod();
    }
    
    @Override
    public void useNativeShowTaskMethod() throws RemoteException{
        this.accessModule.useNativeShowTaskMethod();
    }
    
    @Override
    public void exit() throws RemoteException{
        this.accessModule.exitBeamServer();
    }
    
    @Override
    public void setDefaultIO() throws RemoteException{
        this.accessModule.setDefaultIO();
    }
}
