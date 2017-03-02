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

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class OuterIoEnginesHolder {
    
    private final static Map<Initiator, OuterIoEngine> IO_ENGINES = new HashMap<>();
    
    private final Object enginesLock;
    
    public OuterIoEnginesHolder() {
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
            IO_ENGINES.put(initiator, ioEngine);
            debug("ioEngine accepted.");
        } 
    }
    
    OuterIoEngine getEngine(Initiator initiator) {
        return IO_ENGINES.get(initiator);
    }
    
    boolean deleteEngine(Initiator initiator) {
        synchronized ( this.enginesLock ) {
            try {
                IO_ENGINES.get(initiator).close();
            } catch (IOException e) {
                logError(this.getClass(), "exception during ioEngine closing attempt.", e);
            }
            debug("ioEngine with initiator: " + initiator.getId() + " has been removed.");
            return nonNull(IO_ENGINES.remove(initiator));            
        }    
    }
    
    boolean hasEngine(Initiator initiator) {
        return IO_ENGINES.keySet().contains(initiator);
    }
    
    void closeAllEngines() {
        synchronized ( this.enginesLock ) {
            
            IO_ENGINES
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
            IO_ENGINES.clear();
        }
    }
    
    Stream<OuterIoEngine> all() {
        return IO_ENGINES.values().stream();
    }
}
