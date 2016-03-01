/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import diarsid.beam.external.ExternalIOInterface;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface IoModule extends GemModule {
    
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