/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core;

import com.drs.beam.core.modules.ModulesContainerBuilder;
import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.modules.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.exceptions.ModuleInitializationOrderException;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;

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
            Modules modules = ModulesContainerBuilder.buildContainer();
            modules.initConfigModule(args);
            modules.initIoModule();
            modules.initDataModule();
            modules.initTaskManagerModule();
            modules.initExecutorModule();
            modules.initRmiModule();
            Beam.saveRmiInterfacesInStaticContext(modules.getRmiModule());
            modules.getRmiModule().exportInterfaces();
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
    
    public static void saveRmiInterfacesInStaticContext(RmiModule rmiModule){
        rmiRemoteControlInterface = rmiModule.getRmiRemoteControlInterface();
        rmiExecutorInterface = rmiModule.getRmiExecutorInterface();
        rmiTaskManagerInterface = rmiModule.getRmiTaskManagerInterface();
    }
}