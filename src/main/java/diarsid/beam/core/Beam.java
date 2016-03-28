/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core;

import diarsid.beam.core.modules.RmiModule;
import diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

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
    private static RmiWebPagesHandlerInterface rmiWebPageHandlerInterface;    
    
    private Beam() {
    }    
           
    public static void main(String[] args) {
        GemInjector.buildContainer("Beam.core", new BeamModulesDeclaration());
        GemInjector.getContainer("Beam.core").init();
        //GemInjector.clear();
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