/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.events;

import java.util.Optional;
import java.util.function.Consumer;

import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class PlannedAwaitForEventPayload extends PlannedAwaitForEvent {
    
    private Object payload;

    public PlannedAwaitForEventPayload() {
        super();
    }
    
    public void awaitThenAccept(Consumer consumer) {
        try {
            super.syncAwaitBewareOfNotifier();       
        } catch (InterruptedException ignore) {
            // nothing
            return;
        }
        consumer.accept(this.payload);
    }
    
    public Optional<Object> awaitThenReturn() {
        try {
            super.syncAwaitBewareOfNotifier();                
        } catch (InterruptedException ignore) {
            // nothing
            return Optional.empty();
        }
        return Optional.ofNullable(this.payload);
    }
    
    @Override
    void notifyAwaitedOnEvent() {
        // do nothing
    }        
    
    void notifyAwaitedOnEvent(Object payload) {        
        this.payload = payload;
        try {
            super.syncNotifyBewareOfAwaiting();
        } catch (InterruptedException ignore) {
            // nothing
        }
        logFor(this).info("notifying with " + payload.getClass().getSimpleName() + "...");        
    }
    
}
