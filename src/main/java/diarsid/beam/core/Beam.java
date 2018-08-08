/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RemoteManagerModule;
import diarsid.beam.core.modules.TasksWatcherModule;
import diarsid.beam.core.modules.WebModule;

import com.drs.gem.injector.core.Container;
import com.drs.gem.injector.core.GemInjector;

import static java.lang.Integer.MAX_VALUE;

import static diarsid.beam.core.JavaFXRuntime.launchJavaFXRuntimeAndWait;
import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.util.Logging.logFor;

/**
 *
 * @author Diarsid
 */

public class Beam {
    
    private final static String CORE_CONTAINER; 
    private final static Initiator SYSTEM_INITIATOR;
    private final static BeamRuntime BEAM_RUNTIME;
    
    static {
        CORE_CONTAINER = "Beam.core"; 
        SYSTEM_INITIATOR = new Initiator(MAX_VALUE, IN_MACHINE);
        BEAM_RUNTIME = new BeamRuntime();
    }
    
    private Beam() {
    }    
           
    public static void main(String... args) {
        try {
            logFor(Beam.class).info("start Beam.core");
            Configuration configuration = configuration();
            launchJavaFXRuntimeAndWait();
            initApplication(configuration);
            stopModulesBeforeExit(configuration);
            logFor(Beam.class).info("Beam.core started successfully");
        } catch (Exception e) {
            logFor(Beam.class).error("Beam.core failed:", e);
        }
    }
    
    private static void initApplication(Configuration configuration) {
        GemInjector
                .buildContainer(CORE_CONTAINER, new BeamModulesDeclaration(configuration))
                .init();
    }
    
    private static void stopModulesBeforeExit(Configuration configuration) {
        BEAM_RUNTIME.doBeforeExit(() -> {
            Container container = GemInjector.getContainer(CORE_CONTAINER);
            container.getModule(IoModule.class).stopModule();
            if ( configuration.asBoolean("rmi.core.active") ) {
                container.getModule(RemoteManagerModule.class).stopModule();
            }
            container.getModule(TasksWatcherModule.class).stopModule();
            container.getModule(ExecutorModule.class).stopModule();
            container.getModule(DataModule.class).stopModule();
            container.getModule(WebModule.class).stopModule();
            logFor(Beam.class).info("JVM shutdown: Beam.core modules stopped");
        });
    }
    
    public static Initiator systemInitiator() {
        return SYSTEM_INITIATOR;
    }
    
    public static BeamRuntime beamRuntime() {
        return BEAM_RUNTIME;
    }
}