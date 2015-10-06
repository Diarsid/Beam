/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server;

import com.drs.beam.server.modules.ModuleInitializationException;
import com.drs.beam.server.modules.Modules;
import com.drs.beam.server.modules.io.InnerControlModule;
import com.drs.beam.server.rmi.RmiManager;
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
    private static InnerControlModule innerControl;
    
    // Constructor ========================================================================
    Beam() {
    }

    // Methods ============================================================================
   
           
    public static void main(String[] args) {        
        try {
            ConfigContainer.parseStartArgumentsIntoConfiguration(args);
            Modules.initIoModule();
            Beam.innerControl = Modules.getInnerControlModule();
            Modules.initDataModule();
            Modules.initTaskManagerModule();
            Modules.initExecutorModule();
            RmiManager.exportModules(
                    Modules.getRemoteControlModule(), 
                    Modules.getExecutorModule(), 
                    Modules.getTasksManagerModule());
            ConfigContainer.cancel();
        } catch (ModuleInitializationException e){
            Beam.exitServerLater();
        } catch (BeamFailureException e){
            Beam.exitServerNow();
        } 
    }
    
    public static String getConfigFilePath(){
        return Beam.BEAM_CONFIG_FILE_PATH;
    }
    
    public static void exitServerNow(){
        System.exit(1);
    }
    
    public static void exitServerLater(){
        Beam.innerControl.exitAfterAllNotifications();
    }
}