/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core;

import java.util.ArrayList;
import java.util.List;

import static diarsid.beam.core.base.util.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class BeamRuntime {
    
    private final Object beamRuntimeLock;
    private final List<Runnable> beforeExitActions;

    BeamRuntime() {
        this.beamRuntimeLock = new Object();
        this.beforeExitActions = new ArrayList<>();
    }
    
    public void doBeforeExit(Runnable runnable) {
        synchronized ( this.beamRuntimeLock ) {
            this.beforeExitActions.add(runnable);
        }
    }
    
    public void exitBeamCoreNow() {
        synchronized ( this.beamRuntimeLock ) {
            logFor(Beam.class).info("stopping Beam.core");
            this.beforeExitActions.forEach(runnable -> runnable.run());
            System.exit(0);          
        }
    }
}
