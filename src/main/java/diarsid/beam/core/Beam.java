/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core;

import diarsid.beam.core.util.Logs;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RmiModule;
import diarsid.beam.core.modules.TaskManagerModule;
import diarsid.beam.core.modules.WebModule;
import diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

import com.drs.gem.injector.core.Container;
import com.drs.gem.injector.core.GemInjector;

/**
 *
 * @author Diarsid
 */

public class Beam {
    
    /**
     * Java RMI mechanism requires that remote objects that have been exported 
     * by this JVM for an external usage by other JVM were saved in static variables.
     * Otherwise they will be collected by the GC and the RMI interaction through them will 
     * be impossible. Any attempt to use them after it will cause RemoteException.
     */
    private static RmiRemoteControlInterface rmiRemoteControlInterface;
    private static RmiExecutorInterface rmiExecutorInterface;
    private static RmiTaskManagerInterface rmiTaskManagerInterface;
    private static RmiLocationsHandlerInterface rmiLocationsHandlerInterface;
    private static RmiWebPagesHandlerInterface rmiWebPageHandlerInterface;  
    
    public final static String CORE_CONTAINER = "Beam.core";        
    
    private Beam() {
    }    
           
    public static void main(String[] args) {
        Logs.log(Beam.class, "start Beam.core");
        initApplication();
        setJVMShutdownHook();
    }
    
    private static void initApplication() {
        GemInjector
                .buildContainer(CORE_CONTAINER, new BeamModulesDeclaration())
                .init();
    }
    
    private static void setJVMShutdownHook() {
        Runnable shutdownCallback = new Runnable() {
            @Override
            public void run() {
                Container container = GemInjector.getContainer(CORE_CONTAINER);
                container.getModule(TaskManagerModule.class).stopModule();
                container.getModule(ExecutorModule.class).stopModule();
                container.getModule(DataModule.class).stopModule();
                container.getModule(WebModule.class).stopModule();
                container.getModule(RmiModule.class).stopModule();
                container.getModule(IoInnerModule.class).stopModule();        
                container.getModule(IoModule.class).stopModule();
                Logs.log(Beam.class, "JVM shutdown: Beam.core modules stopped");
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownCallback));        
    }
    
    public static void exitBeamCoreNow() {
        Logs.log(Beam.class, "stop Beam.core");
        System.exit(0);
    }
    
    public static void saveRmiInterfacesInStaticContext(RmiModule rmiModule){
        rmiRemoteControlInterface = rmiModule.getRmiRemoteControlInterface();
        rmiExecutorInterface = rmiModule.getRmiExecutorInterface();
        rmiTaskManagerInterface = rmiModule.getRmiTaskManagerInterface();
        rmiLocationsHandlerInterface = rmiModule.getRmiLocationsHandlerInterface();
        rmiWebPageHandlerInterface = rmiModule.getRmiWebPageHandlerInterface();
    }
}