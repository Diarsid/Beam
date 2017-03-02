/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.log;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class OuterIoEnginesHolder {
    
    private final Set<Initiator> initiators;    
    private final Object enginesLock;
    private final OuterIoEnginesManager enginesManager;
    
    public OuterIoEnginesHolder(OuterIoEnginesManager enginesManager) {
        this.enginesLock = new Object();
        this.enginesManager = enginesManager;
        this.initiators = new HashSet<>();
    }
    
    boolean acceptNewIoEngine(OuterIoEngine ioEngine) {
        synchronized ( this.enginesLock ) {
            try {
                if ( this.enginesManager.hasSlots() ) {
                    int slotNumber = this.enginesManager.addEngine(ioEngine);
                    if ( slotNumber < 0 ) {
                        log(this.getClass(), "cannot accept new engine.");
                        return false;
                    }
                    Initiator initiator = new Initiator(slotNumber);
                    ioEngine.acceptInitiator(initiator);
                    this.initiators.add(initiator);
                    log(this.getClass(), format("%s set with initiator: %s, into slot %d", 
                            ioEngine.getName(), 
                            initiator.identity(), 
                            initiator.engineNumber()));
                    return true;
                } else {
                    log(this.getClass(), "there are no free slots for new engine.");
                    ioEngine.report("cannot be accepted - not enough slots.");
                    return false;
                }
            } catch (IOException ex) {
                logError(this.getClass(), 
                        "exception during ioEngine initiator token accepting.", ex);
                return false;
            }
        } 
    }
    
    OuterIoEngine getEngine(Initiator initiator) {
        return this.enginesManager.getEngine(initiator);
    }
    
    boolean deleteEngine(Initiator initiator) {
        synchronized ( this.enginesLock ) {
            try {
                this.enginesManager.getEngine(initiator).close();
                this.initiators.remove(initiator);
                boolean removed = this.enginesManager.removeEngine(initiator.engineNumber());
                if ( removed ) {
                    log(this.getClass(), "engine has been removed.");
                    return true;
                } else {
                    log(this.getClass(), "engine has not been removed.");
                    return false;
                }
            } catch (IOException e) {
                logError(this.getClass(), "exception during ioEngine closing attempt.", e);
                return false;
            }           
        }    
    }
    
    boolean hasEngine(Initiator initiator) {
        return 
                this.initiators.contains(initiator) && 
                this.enginesManager.hasEngine(initiator);
    }
    
    void closeAllEngines() {
        synchronized ( this.enginesLock ) {
            this.initiators
                    .forEach(initiator -> {
                        try {
                            OuterIoEngine engine = this.enginesManager.getEngine(initiator);
                            String engineName = engine.getName();
                            engine.close();
                            this.enginesManager.removeEngine(initiator.engineNumber());
                            debug("closing engine: " + engineName);
                        } catch (IOException ex) {
                            logError(this.getClass(), 
                                    "exception during ioEngine closing attempt.", ex);
                        }
                    });
            this.initiators.clear();
        }
    }
    
    Stream<OuterIoEngine> all() {
        return this.initiators
                .stream()
                .map(initiator -> this.enginesManager.getEngine(initiator));
    }
}
