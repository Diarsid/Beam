/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core;

import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;
import com.drs.gem.injector.core.Container;
import com.drs.gem.injector.core.GemInjector;


/**
 *
 * @author Diarsid
 */

public class Beam {
    
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
    
    
    private Beam() {
    }    
           
    public static void main(String[] args) {
        GemInjector.buildContainer("Beam.core", new BeamModulesDeclaration());
        GemInjector.getContainer("Beam.core").init();
        GemInjector.clear();
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