/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.starter;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import com.drs.beam.util.config.reader.ConfigReader;

/**
 *
 * @author Diarsid
 */
class RemoteLocator {
    // Fields =============================================================================
    private final ConfigReader reader = ConfigReader.getReader();
    
    // Constructors =======================================================================
    RemoteLocator() {
    }    
    
    // Methods ============================================================================
        
    List<String> defineModulesToStart(){
        List<String> modules = new ArrayList<>();
        if (!isBeamWorking()){
            modules.add("beam");
        } 
        if (!isConsoleWorking()){
            modules.add("console");
        }
        return modules;
    }
    
    boolean isBeamWorking(){
        try {
            Registry beamRegistry = LocateRegistry.getRegistry(
                reader.getBeamHost(),
                reader.getBeamPort());
            return beamRegistry.list().length > 0;
        } catch (RemoteException re){            
            return false;
        }
    }
    
    boolean isConsoleWorking(){
        try {
            Registry consoleRegistry = LocateRegistry.getRegistry(
                reader.getSystemConsoleHost(),
                reader.getSystemConsolePort());
            return consoleRegistry.list().length > 0;
        } catch (RemoteException re){            
            return false;
        }
    }
}
