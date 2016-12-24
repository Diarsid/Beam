/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.modules;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import diarsid.beam.core.StoppableBeamModule;

import old.diarsid.beam.external.ExternalIOInterface;

/**
 * Is responsible for an interaction of the whole program with
 * its remote controls such as remote console.
 * 
 * @author Diarsid
 */
public interface OldIoModule extends StoppableBeamModule {
    
    public boolean isExternalProcessorActive();
    
    boolean hasExternalIOProcessor();    
    boolean useExternalShowTaskMethod();
    
    boolean setUseExternalShowTaskMethod();
    boolean setUseNativeShowTaskMethod();
    
    ExternalIOInterface getExternalIOEngine();
    
    void resetIoToDefault();
    
    void acceptNewExternalIOProcessor(
            String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException;
    
    void exitBeam();
}
