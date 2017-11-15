/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
        
    private final static int UNLIMITED_ENGINES_SLOT_NUMBER = 0;
            
    private final Set<Initiator> initiators;    
    private final Object enginesLock;
    private final LimitedOuterIoEnginesManager limitedEnginesManager;
    private final Map<Initiator, OuterIoEngine> unlimitedEngines;
    
    public OuterIoEnginesHolder(LimitedOuterIoEnginesManager enginesManager) {
        this.enginesLock = new Object();
        this.limitedEnginesManager = enginesManager;
        this.unlimitedEngines = new HashMap<>();
        this.initiators = new HashSet<>();
    }
    
    boolean acceptNewIoEngine(OuterIoEngine ioEngine) throws IOException {
        if ( ioEngine.type().isLimitedBySlots() ) {
            return this.acceptAsLimited(ioEngine);
        } else {
            return this.acceptAsUnlimited(ioEngine);
        }   
    }
    
    private boolean acceptAsLimited(OuterIoEngine ioEngine) throws IOException {
        synchronized ( this.enginesLock ) {
            if ( this.limitedEnginesManager.hasSlots() ) {
                int slotNumber = this.limitedEnginesManager.addEngine(ioEngine);
                if ( slotNumber < 0 ) {
                    log(this.getClass(), "cannot accept new engine.");
                    return false;
                }
                Initiator initiator = new Initiator(slotNumber, ioEngine.type());
                ioEngine.accept(initiator);
                this.initiators.add(initiator);
                log(this.getClass(), format("%s %s set with initiator: %s, into slot %d", 
                        ioEngine.name(), 
                        ioEngine.type(),
                        initiator.identity(), 
                        initiator.engineNumber()));
                return true;
            } else {
                log(this.getClass(), "there are no free slots for new engine.");
                ioEngine.report("cannot be accepted - not enough slots.");
                return false;
            }
        } 
    }
    
    private boolean acceptAsUnlimited(OuterIoEngine ioEngine) {
        synchronized ( this.enginesLock ) {
            
        }
    }
    
    OuterIoEngine getEngineBy(Initiator initiator) {
        return this.limitedEnginesManager.getEngineBy(initiator);
    }
    
    boolean deleteEngineBy(Initiator initiator) {
        synchronized ( this.enginesLock ) {
            try {
                this.limitedEnginesManager.getEngineBy(initiator).close();
                this.initiators.remove(initiator);
                boolean removed = this.limitedEnginesManager.removeEngineBy(initiator);
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
    
    boolean hasEngineBy(Initiator initiator) {
        return 
                this.initiators.contains(initiator) && 
                this.limitedEnginesManager.hasEngineBy(initiator);
    }
    
    void closeAllEngines() {
        synchronized ( this.enginesLock ) {
            this.initiators
                    .forEach(initiator -> {
                        try {
                            OuterIoEngine engine = this.limitedEnginesManager.getEngineBy(initiator);
                            String engineName = engine.name();
                            engine.close();
                            this.limitedEnginesManager.removeEngineBy(initiator);
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
                .map(initiator -> this.limitedEnginesManager.getEngineBy(initiator));
    }
}
