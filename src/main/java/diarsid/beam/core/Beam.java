/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.util.Logs;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RemoteManagerModule;
import diarsid.beam.core.modules.TasksWatcherModule;
import diarsid.beam.core.modules.WebModule;

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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Container container = GemInjector.getContainer(CORE_CONTAINER);
            container.getModule(IoModule.class).stopModule();
            container.getModule(RemoteManagerModule.class).stopModule();
            container.getModule(TasksWatcherModule.class).stopModule();
            container.getModule(ExecutorModule.class).stopModule();
            container.getModule(DataModule.class).stopModule();
            container.getModule(WebModule.class).stopModule();
            log(Beam.class, "JVM shutdown: Beam.core modules stopped");
        }));        
    }
    
    public static Initiator systemInitiator() {
        return SYSTEM_INITIATOR;
    }
    
    public static void exitBeamCoreNow() {
        Logs.log(Beam.class, "stop Beam.core");
        System.exit(0);
    }
}