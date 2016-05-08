/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;

import diarsid.beam.external.ExternalIOInterface;
import diarsid.beam.core.StoppableBeamModule;

/**
 * Is responsible for an interaction of the whole program with
 * its remote controls such as remote console.
 * 
 * @author Diarsid
 */
public interface IoModule extends StoppableBeamModule {
    
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
    
    Set<String> getPreviousConsoleCommands();
    
    void storeCommandsFromConsole(Set<String> commands);
}
