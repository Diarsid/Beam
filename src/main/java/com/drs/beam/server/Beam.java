/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server;

import com.drs.beam.server.modules.ModuleInitializationException;
import com.drs.beam.server.modules.ModuleInitializationOrderException;
import com.drs.beam.server.modules.ModulesContainer;
import com.drs.beam.server.rmi.RmiManager;
import com.drs.beam.server.rmi.adapters.RmiExecutorAdapter;
import com.drs.beam.server.rmi.adapters.RmiRemoteControlAdapter;
import com.drs.beam.server.rmi.adapters.RmiTaskManagerAdapter;
import com.drs.beam.server.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.server.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.server.rmi.interfaces.RmiTaskManagerInterface;
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
    private static final ModulesContainer modules = new ModulesContainer();
    
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
            modules.initIoModule();
            modules.initDataModule();
            modules.initTaskManagerModule();
            modules.initExecutorModule();
            Beam.initRmiInterfaces();
            Beam.exportRmiInterfaces();
            ConfigContainer.cancel();
        } catch(ModuleInitializationOrderException e){
            e.printStackTrace();
            Beam.exitServerNow();
        } catch (ModuleInitializationException e){
            Beam.exitServerLater();
        }
    }
    
    public static String getConfigFilePath(){
        return Beam.BEAM_CONFIG_FILE_PATH;
    }
    
    public static void exitServerNow(){
        System.exit(1);
    }
    
    public static void exitServerLater(){
        modules.getInnerControlModule().exitAfterAllNotifications();
    }
    
    private static void initRmiInterfaces(){
        rmiRemoteControlInterface = new RmiRemoteControlAdapter(modules.getRemoteControlModule());
        rmiExecutorInterface = new RmiExecutorAdapter(modules.getExecutorModule());
        rmiTaskManagerInterface = new RmiTaskManagerAdapter(modules.getTasksManagerModule());
    }
    
    private static void exportRmiInterfaces(){
        RmiManager rmiManager = new RmiManager(modules.getInnerIOModule());
        rmiManager.exportInterfaces(
                    rmiRemoteControlInterface, 
                    rmiExecutorInterface, 
                    rmiTaskManagerInterface);
    }
}