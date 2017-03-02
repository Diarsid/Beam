/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.remotemanager.endpointholders;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.base.rmi.RemoteOuterIoEngine;
import diarsid.beam.core.modules.ControlModule;
import diarsid.beam.core.modules.IoModule;

import static java.rmi.registry.LocateRegistry.getRegistry;


public class RemoteCoreAccessEndpointHolder implements RemoteCoreAccessEndpoint {
    
    private final IoModule ioModule;
    private final ControlModule coreControlModule;
    
    public RemoteCoreAccessEndpointHolder(IoModule ioModule, ControlModule coreControlModule) {
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
    public void exitBeam(Initiator initiator) throws RemoteException {
        this.coreControlModule.exitBeam(initiator);
    }

    @Override
    public void executeCommand(Initiator initiator, String commandLine) throws RemoteException {
        this.coreControlModule.executeCommand(initiator, commandLine);
    }
}
