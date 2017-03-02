/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.util.Logs;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RemoteManagerModule;

import com.drs.gem.injector.core.Container;
import com.drs.gem.injector.core.GemInjector;

import static java.lang.Integer.MAX_VALUE;

import static diarsid.beam.core.base.util.Logs.log;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */

public class Beam {
    
    private static final Initiator SYSTEM_INITIATOR = new Initiator(MAX_VALUE);
    
    public final static String CORE_CONTAINER = "Beam.core";        
    
    private Beam() {
    }    
           
    public static void main(String... args) {
        try {
            log(Beam.class, "start Beam.core");
            initApplication();
            setJVMShutdownHook();
            log(Beam.class, "Beam.core started successfully");
        } catch (Exception e) {
            logError(Beam.class, e);
            exitBeamCoreNow();
        }
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
                container.getModule(RemoteManagerModule.class).stopModule();
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
}