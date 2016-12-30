/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.remotemanager.endpointholders;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static java.rmi.registry.LocateRegistry.getRegistry;

import diarsid.beam.core.rmi.RemoteCoreAccessEndpoint;


public class RemoteAccessEndpointHolder implements RemoteCoreAccessEndpoint {
    
    private final IoModule ioModule;
    private final CoreControlModule coreControlModule;
    
    public RemoteAccessEndpointHolder(IoModule ioModule, CoreControlModule coreControlModule) {
        this.ioModule = ioModule;
        this.coreControlModule = coreControlModule;
    }

    @Override
    public void acceptRemoteOuterIoEngine(
            String consoleRmiName, String consoleHost, int consolePort) 
                    throws RemoteException, NotBoundException {
        RemoteOuterIoEngine remoteIO = 
                (RemoteOuterIoEngine) 
                        getRegistry(consoleHost, consolePort).lookup(consoleRmiName);
        this.ioModule.registerOuterIoEngine(remoteIO);
    }

    @Override
    public boolean unregisterIoEngine(Initiator initiator) throws RemoteException {
        return this.ioModule.unregisterIoEngine(initiator);
    }

    @Override
    public void exitBeam() throws RemoteException {
        this.coreControlModule.exitBeam();
    }

    @Override
    public void executeCommand(Initiator initiator, String commandLine) throws RemoteException {
        this.coreControlModule.executeCommand(initiator, commandLine);
    }
}
