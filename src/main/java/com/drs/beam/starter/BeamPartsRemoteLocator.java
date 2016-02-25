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

import com.drs.beam.shared.modules.ConfigModule;
import com.drs.beam.shared.modules.config.Config;

/**
 *
 * @author Diarsid
 */
class BeamPartsRemoteLocator {
    
    private final ConfigModule config;
    
    BeamPartsRemoteLocator(ConfigModule config) {
        this.config = config;
    }    
    
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
                    config.get(Config.CORE_HOST),
                    Integer.parseInt(config.get(Config.CORE_PORT)));
            return beamRegistry.list().length > 0;
        } catch (RemoteException re){            
            return false;
        }
    }
    
    boolean isConsoleWorking(){
        try {
            Registry consoleRegistry = LocateRegistry.getRegistry(
                config.get(Config.SYS_CONSOLE_HOST),
                Integer.parseInt(config.get(Config.SYS_CONSOLE_PORT)));
            return consoleRegistry.list().length > 0;
        } catch (RemoteException re){            
            return false;
        }
    }
}
