/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;

/**
 * Remote adapter for interaction with Beam core IO system.
 * 
 * Is responsible for accepting new remote ExternalIO objects and
 * managing them.
 * 
 * @author Diarsid
 */
class RmiAdapterForRemoteControl implements RmiRemoteControlInterface {
    
    private final IoModule ioModule;
    
    RmiAdapterForRemoteControl(IoModule ioModule) {
        this.ioModule = ioModule;
    }
        
    @Override
    public boolean isExternalIoProcessorActive() throws RemoteException {
        return this.ioModule.isExternalProcessorActive();
    }
    
    @Override
    public void acceptNewIOProcessor
            (String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException {
        
        this.ioModule.acceptNewExternalIOProcessor
                (consoleRmiName, consoleHost, consolePort);
    }
    
    @Override
    public boolean setUseExternalShowTaskMethod() throws RemoteException {
        return this.ioModule.setUseExternalShowTaskMethod();
    }
    
    @Override
    public boolean setUseNativeShowTaskMethod() throws RemoteException {
        return this.ioModule.setUseNativeShowTaskMethod();
    }
    
    @Override
    public void exit() throws RemoteException {
        this.ioModule.exitBeam();
    }
    
    @Override
    public void setDefaultIO() throws RemoteException {
        this.ioModule.resetIoToDefault();
    }
}
