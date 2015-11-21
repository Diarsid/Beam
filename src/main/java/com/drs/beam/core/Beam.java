/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core;

import com.drs.beam.core.exceptions.NullDependencyInjectionException;
import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.exceptions.ModuleInitializationException;
import com.drs.beam.core.exceptions.ModuleInitializationOrderException;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;


/**
 *
 * @author Diarsid
 */

public class Beam {
    // Fields =============================================================================
    private static final String BEAM_CONFIG_FILE_PATH = "./config/config.xml";
    
    /**
     * Java RMI mechanism requires that remote objects which has been exported for
     * usage by other JVM were saved in static variables.
     * Otherwise they will be collected by GC and RMI interaction through them will 
     * be impossible. Any attempt to use them after it will cause RemoteException.
     */
    private static RmiRemoteControlInterface rmiRemoteControlInterface;
    private static RmiExecutorInterface rmiExecutorInterface;
    private static RmiTaskManagerInterface rmiTaskManagerInterface;
    private static RmiLocationsHandlerInterface rmiLocationsHandlerInterface;
    private static RmiWebPageHandlerInterface rmiWebPageHandlerInterface;
    
    // Constructor ========================================================================
    Beam() {
    }

    // Methods ============================================================================
   
           
    public static void main(String[] args) {        
        try {
            Modules modules = new ModulesContainer();
            modules.initConfigModule(args);
            modules.initIoModule();
            modules.initInnerIoModule();
            modules.initDataModule();
            modules.initTaskManagerModule();
            modules.initExecutorModule();
            modules.initRmiModule();
            Beam.saveRmiInterfacesInStaticContext(modules.getRmiModule());
            modules.getRmiModule().exportInterfaces();
        } catch(ModuleInitializationOrderException e){
            e.printStackTrace();
            Beam.exitBeamCoreNow();
        } catch (ModuleInitializationException e){
            // Do nothing.
            // Wait until all notifiacations will be readed by user.
        } catch (NullDependencyInjectionException e){
            // Respond on the exception.
        }
    }
    
    public static String getConfigFilePath(){
        return Beam.BEAM_CONFIG_FILE_PATH;
    }
    
    public static void exitBeamCoreNow(){
        System.exit(1);
    }
    
    public static void saveRmiInterfacesInStaticContext(RmiModule rmiModule){
        rmiRemoteControlInterface = rmiModule.getRmiRemoteControlInterface();
        rmiExecutorInterface = rmiModule.getRmiExecutorInterface();
        rmiTaskManagerInterface = rmiModule.getRmiTaskManagerInterface();
        rmiLocationsHandlerInterface = rmiModule.getRmiLocationsHandlerInterface();
        rmiWebPageHandlerInterface = rmiModule.getRmiWebPageHandlerInterface();
    }
}