/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;

/**
 *
 * @author Diarsid
 */
public interface RemoteCoreAccessEndpoint extends Remote {
    
    void acceptRemoteOuterIoEngine(
            String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException;
        
    boolean unregisterIoEngine(Initiator initiator) throws RemoteException;
    
    void exitBeam() throws RemoteException;    
    
    void executeCommand(Initiator initiator, String commandLine) throws RemoteException;
}
