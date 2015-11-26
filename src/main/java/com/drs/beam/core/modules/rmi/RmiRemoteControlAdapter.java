/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.rmi;

import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.drs.beam.core.modules.IoModule;

/**
 *
 * @author Diarsid
 */
class RmiRemoteControlAdapter implements RmiRemoteControlInterface{
    // Fields =============================================================================
    
    private final IoModule ioModule;
    
    // Constructors =======================================================================

    RmiRemoteControlAdapter(IoModule ioModule) {
        this.ioModule = ioModule;
    }
    
    // Methods ============================================================================
    
    @Override
    public boolean isExternalIoProcessorActive() throws RemoteException{
        return this.ioModule.isExternalProcessorActive();
    }
    
    @Override
    public void acceptNewIOProcessor(String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException{
        
        this.ioModule.acceptNewExternalIOProcessor(consoleRmiName, consoleHost, consolePort);
    }
    
    @Override
    public void useExternalShowTaskMethod() throws RemoteException{
        this.ioModule.useExternalShowTaskMethod();
    }
    
    @Override
    public void useNativeShowTaskMethod() throws RemoteException{
        this.ioModule.setUseNativeShowTaskMethod();
    }
    
    @Override
    public void exit() throws RemoteException{
        this.ioModule.exitBeam();
    }
    
    @Override
    public void setDefaultIO() throws RemoteException{
        this.ioModule.resetIoToDefault();
    }
}
