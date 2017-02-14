/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.modules.CoreRemoteManagerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.base.util.Logs;

import com.drs.gem.injector.core.Container;
import com.drs.gem.injector.core.GemInjector;

import static diarsid.beam.core.base.util.Logs.log;

/**
 *
 * @author Diarsid
 */

public class Beam {
    
    public static final String CONFIG_FILE = "./../config/config.xml";
    
    private static final Initiator SYSTEM_INITIATOR = new Initiator();
    
    /**
     * Java RMI mechanism requires that remote objects that have been exported 
     * by this JVM for an external usage by other JVM were saved in static variables.
     * Otherwise they will be collected by the GC and the RMI interaction through them will 
     * be impossible. Any attempt to use them after it will cause RemoteException.
     */
    private static RemoteCoreAccessEndpoint remoteAccessEndpoint;
//    private static RmiExecutorInterface rmiExecutorInterface;
//    private static RmiTaskManagerInterface rmiTaskManagerInterface;
//    private static RmiLocationsHandlerInterface rmiLocationsHandlerInterface;
//    private static RmiWebPagesHandlerInterface rmiWebPageHandlerInterface;  
    
    public final static String CORE_CONTAINER = "Beam.core";        
    
    private Beam() {
    }    
           
    public static void main(String... args) {
        Logs.log(Beam.class, "start Beam.core");
        initApplication();
        setJVMShutdownHook();
        Logs.log(Beam.class, "Beam.core started successfully");
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
                container.getModule(IoModule.class).stopModule();
                container.getModule(CoreRemoteManagerModule.class).stopModule();
//                container.getModule(TaskManagerModule.class).stopModule();
//                container.getModule(ExecutorModule.class).stopModule();
//                container.getModule(DataModule.class).stopModule();
//                container.getModule(WebModule.class).stopModule();
//                container.getModule(RmiModule.class).stopModule();
//                container.getModule(IoInnerModule.class).stopModule();        
//                container.getModule(OldIoModule.class).stopModule();
                log(Beam.class, "JVM shutdown: Beam.core modules stopped");
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownCallback));        
    }
    
    public static Initiator getSystemInitiator() {
        return SYSTEM_INITIATOR;
    }
    
    public static void exitBeamCoreNow() {
        Logs.log(Beam.class, "stop Beam.core");
        System.exit(0);
    }
    
    public static void saveRmiInterfacesInStaticContext(
            CoreRemoteManagerModule remoteManagerModule) {
        remoteAccessEndpoint = remoteManagerModule.getRemoteAccessEndpoint();
//        rmiRemoteControlInterface = rmiModule.getRmiRemoteControlInterface();
//        rmiExecutorInterface = rmiModule.getRmiExecutorInterface();
//        rmiTaskManagerInterface = rmiModule.getRmiTaskManagerInterface();
//        rmiLocationsHandlerInterface = rmiModule.getRmiLocationsHandlerInterface();
//        rmiWebPageHandlerInterface = rmiModule.getRmiWebPageHandlerInterface();
    }
}