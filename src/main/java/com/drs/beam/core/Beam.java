/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core;

import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.ModulesContainerBuilder;
import com.drs.beam.core.modules.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.exceptions.ModuleInitializationOrderException;
import com.drs.beam.core.rmi.RmiManager;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.rmi.interfaces.adapters.RmiExecutorAdapter;
import com.drs.beam.core.rmi.interfaces.adapters.RmiRemoteControlAdapter;
import com.drs.beam.core.rmi.interfaces.adapters.RmiTaskManagerAdapter;
import com.drs.beam.util.config.ConfigContainer;

/*
 * Main application class.
 * Creates all parts of program, initializes and exports them on localhost port trough RMI.
 */

/**
 *
 * @author Diarsid
 */

public class Beam {
    // Fields =============================================================================
    private static final String BEAM_CONFIG_FILE_PATH = "./config/config.xml";
    
    private static RmiRemoteControlInterface rmiRemoteControlInterface;
    private static RmiExecutorInterface rmiExecutorInterface;
    private static RmiTaskManagerInterface rmiTaskManagerInterface;
    
    // Constructor ========================================================================
    Beam() {
    }

    // Methods ============================================================================
   
           
    public static void main(String[] args) {        
        try {
            ConfigContainer.parseStartArgumentsIntoConfiguration(args);
            Modules modules = ModulesContainerBuilder.buildContainer();
            modules.initIoModule();
            modules.initDataModule();
            modules.initTaskManagerModule();
            modules.initExecutorModule();
            Beam.initRmiInterfaces(modules);
            Beam.exportRmiInterfaces(modules.getInnerIOModule());
            ConfigContainer.cancel();
        } catch(ModuleInitializationOrderException e){
            e.printStackTrace();
            Beam.exitServerNow();
        } catch (ModuleInitializationException e){
            // Do nothing.
            // Wait for all notifiacations will be readed.
        }
    }
    
    public static String getConfigFilePath(){
        return Beam.BEAM_CONFIG_FILE_PATH;
    }
    
    public static void exitServerNow(){
        System.exit(1);
    }
    
    private static void initRmiInterfaces(Modules modules){
        rmiRemoteControlInterface = new RmiRemoteControlAdapter(modules.getRemoteControlModule());
        rmiExecutorInterface = new RmiExecutorAdapter(modules.getExecutorModule());
        rmiTaskManagerInterface = new RmiTaskManagerAdapter(modules.getTasksManagerModule());
    }
    
    private static void exportRmiInterfaces(InnerIOModule ioModule){
        RmiManager rmiManager = new RmiManager(ioModule);
        rmiManager.exportInterfaces(
                    rmiRemoteControlInterface, 
                    rmiExecutorInterface, 
                    rmiTaskManagerInterface);
    }
}