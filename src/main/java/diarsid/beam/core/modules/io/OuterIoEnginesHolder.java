/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.OuterIoEngine;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.util.Logs.debug;
import static diarsid.beam.core.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class OuterIoEnginesHolder {
    
    private final Map<Initiator, OuterIoEngine> ioEngines;
    private final Object enginesLock;
    
    public OuterIoEnginesHolder() {
        this.ioEngines = new HashMap<>();
        this.enginesLock = new Object();
    }
    
    void acceptNewIoEngine(OuterIoEngine ioEngine) {
        synchronized ( this.enginesLock ) {
            Initiator initiator = new Initiator();
            try {
                ioEngine.acceptInitiator(initiator);
                debug(ioEngine.getName() + " set with initiator: " + initiator.getId());
            } catch (IOException ex) {
                logError(this.getClass(), 
                        "exception during ioEngine initiator token accepting.", ex);
            }
            this.ioEngines.put(initiator, ioEngine);
            debug("ioEngine accepted.");
        } 
    }
    
    OuterIoEngine getEngine(Initiator initiator) {
        return this.ioEngines.get(initiator);
    }
    
    boolean deleteEngine(Initiator initiator) {
        synchronized ( this.enginesLock ) {
            try {
                this.ioEngines.get(initiator).close();
            } catch (IOException e) {
                logError(this.getClass(), "exception during ioEngine closing attempt.", e);
            }
            debug("ioEngine with initiator: " + initiator.getId() + " has been removed.");
            return nonNull(this.ioEngines.remove(initiator));            
        }    
    }
    
    boolean hasEngine(Initiator initiator) {
        return this.ioEngines.keySet().contains(initiator);
    }
    
    void closeAllEngines() {
        synchronized ( this.enginesLock ) {
            
            this.ioEngines
                    .values()
                    .forEach(outerIoEngine -> {
                        try {
                            String engineName = outerIoEngine.getName();
                            outerIoEngine.close();
                            debug("closing engine: " + engineName);
                        } catch (IOException ex) {
                            logError(this.getClass(), 
                                    "exception during ioEngine closing attempt.", ex);
                        }
                    });
            this.ioEngines.clear();
        }
    }
    
    Stream<OuterIoEngine> all() {
        return this.ioEngines.values().stream();
    }
}
