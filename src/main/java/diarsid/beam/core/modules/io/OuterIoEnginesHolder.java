/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.Logs.log;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class OuterIoEnginesHolder {
            
    private final Set<Initiator> initiators;    
    private final Object enginesLock;
    private final LimitedOuterIoEnginesManager limitedEnginesManager;
    private final UnlimitedOuterIoEnginesManager unlimitedEnginesManager;
    
    public OuterIoEnginesHolder(
            LimitedOuterIoEnginesManager limitedEnginesManager,
            UnlimitedOuterIoEnginesManager unlimitedEnginesManager) {
        this.enginesLock = new Object();
        this.limitedEnginesManager = limitedEnginesManager;
        this.unlimitedEnginesManager = unlimitedEnginesManager;
        this.initiators = new HashSet<>();
    }
    
    private OuterIoEnginesManager enginesManagerFor(Initiator initiator) {
        if ( initiator.outerIoEngineType().isLimitedBySlots() ) {
            return this.limitedEnginesManager;
        } else {
            return this.unlimitedEnginesManager;
        }
    }
    
    private OuterIoEnginesManager enginesManagerFor(OuterIoEngine ioEngine) throws IOException {
        if ( ioEngine.type().isLimitedBySlots() ) {
            return this.limitedEnginesManager;
        } else {
            return this.unlimitedEnginesManager;
        }
    }
    
    boolean acceptNewIoEngine(OuterIoEngine ioEngine) throws IOException {
        synchronized ( this.enginesLock ) {
            Optional<Initiator> initiator = this
                    .enginesManagerFor(ioEngine)
                    .registerEngine(ioEngine);
            if ( initiator.isPresent() ) {
                this.initiators.add(initiator.get());
                log(this.getClass(), format("%s %s set with initiator: %s, into slot %d", 
                        ioEngine.name(), 
                        ioEngine.type(),
                        initiator.get().identity(), 
                        initiator.get().engineNumber()));
                return true;
            } else {
                return false;
            }
        }   
    }
    
    OuterIoEngine getEngineBy(Initiator initiator) {
        return this.enginesManagerFor(initiator).getEngineBy(initiator);
    }        
    
    boolean processCloseRequestBy(Initiator initiator) {
        try {
            OuterIoEngine ioEngine = this.getEngineBy(initiator);
            if ( nonNull(ioEngine) ) {
                if ( ioEngine.isActiveWhenClosed() ) {
                    ioEngine.close();
                    return true;
                } else {
                    return this.closeEngineAndRemoveBy(initiator);                    
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            logError(this.getClass(), "exception during ioEngine closing attempt.", e);
            return false;
        } 
    }
    
    private boolean closeEngineAndRemoveBy(Initiator initiator) throws IOException {
        synchronized ( this.enginesLock ) {
            this.initiators.remove(initiator);
            if ( this.enginesManagerFor(initiator).closeAndRemoveEngineBy(initiator) ) {
                log(this.getClass(), "engine has been removed.");
                return true;
            } else {
                log(this.getClass(), "engine has not been removed.");
                return false;
            }
        }
    }
    
    boolean hasEngineBy(Initiator initiator) {
        return 
                this.initiators.contains(initiator) && 
                this.enginesManagerFor(initiator).hasEngineBy(initiator);
    }
    
    void closeAllEngines() {
        synchronized ( this.enginesLock ) {
            this.initiators.forEach(initiator -> {
                try {
                    this.enginesManagerFor(initiator).closeAndRemoveEngineBy(initiator);
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
                .map(initiator -> this.enginesManagerFor(initiator).getEngineBy(initiator));
    }
}
