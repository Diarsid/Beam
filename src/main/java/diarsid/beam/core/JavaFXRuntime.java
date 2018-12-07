/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core;

import javafx.application.Application;
import javafx.stage.Stage;

import diarsid.beam.core.base.events.PlannedAwaitForEvent;

import static diarsid.beam.core.base.events.BeamEventRuntime.fireAsync;
import static diarsid.beam.core.base.events.BeamEventRuntime.planAwaitingFor;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;

/**
 *
 * @author Diarsid
 */
public class JavaFXRuntime extends Application {
    
    private static final String LAUNCHED;
    private static final Object LOCK;
    private static boolean notLaunched;
    
    static {
        LOCK = new Object();
        LAUNCHED = "JavaFX Runtime launched";
        notLaunched = true;
    }
    
    public static void launchJavaFXRuntimeAndWait() {
        synchronized ( LOCK ) {
            if ( notLaunched ) {
                PlannedAwaitForEvent plannedAwaitForLaunch = planAwaitingFor(LAUNCHED);
                
                asyncDoIndependently(
                        "JavaFX Application starter thread", 
                        () -> Application.launch());
                
                plannedAwaitForLaunch.awaitThenProceed();   
                notLaunched = false;             
            }            
        }        
    } 

    @Override
    public void start(Stage stage) throws Exception {
        fireAsync(LAUNCHED);
    }
}
