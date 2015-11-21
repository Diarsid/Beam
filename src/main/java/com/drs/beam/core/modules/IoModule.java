/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.core.Module;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.drs.beam.external.ExternalIOInterface;

/**
 *
 * @author Diarsid
 */
public interface IoModule extends Module {
    
    boolean hasExternalIOProcessor();    
    boolean useExternalShowTaskMethod();
    
    void setUseExternalShowTaskMethod();
    void setUseNativeShowTaskMethod();
    
    ExternalIOInterface getExternalIOEngine();
    
    void resetIoToDefault();
    
    void acceptNewExternalIOProcessor(
            String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException;
    
    void exitBeam();
    
    static String getModuleName(){
        return Module.class.getSimpleName();
    }
}
