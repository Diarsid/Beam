/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core;

import javafx.application.Application;
import javafx.stage.Stage;

import static diarsid.beam.core.base.events.BeamEventRuntime.awaitFor;
import static diarsid.beam.core.base.events.BeamEventRuntime.fireAsync;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;

/**
 *
 * @author Diarsid
 */
public class JavaFXRuntime extends Application {
    
    private static final String ON_LAUNCH_EVENT;
    private static final Object LOCK;
    private static boolean notLaunched;
    
    static {
        LOCK = new Object();
        ON_LAUNCH_EVENT = "JavaFX Runtime launched";
        notLaunched = true;
    }
    
    public static void launchJavaFXRuntimeAndWait() {
        synchronized ( LOCK ) {
            if ( notLaunched ) {
                asyncDoIndependently(
                        "JavaFX Application starter thread", 
                        () -> Application.launch());
                notLaunched = false;
                awaitFor(ON_LAUNCH_EVENT).thenProceed();                
            }            
        }        
    } 

    @Override
    public void start(Stage stage) throws Exception {
        fireAsync(ON_LAUNCH_EVENT);
    }
}