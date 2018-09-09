/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.events;

import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Diarsid
 */
public class EventPayloadAwait extends EventAwait {
    
    private Object payload;

    public EventPayloadAwait() {
        super();
    }
    
    public void thenAccept(Consumer consumer) {
        synchronized ( super.monitor() ) {
            try {
                super.monitor().wait();
            } catch (InterruptedException ignore) {
                // nothing
                return;
            }
        }
        consumer.accept(this.payload);
    }
    
    public Optional<Object> thenReturn() {
        synchronized ( super.monitor() ) {
            try {
                super.monitor().wait();
            } catch (InterruptedException ignore) {
                // nothing
                return Optional.empty();
            }
        }
        return Optional.ofNullable(this.payload);
    }
    
    @Override
    void notifyAwaitedOnEvent() {
        // do nothing
    }        
    
    void notifyAwaitedOnEvent(Object payload) {        
        this.payload = payload;
        synchronized ( super.monitor() ) {
            super.monitor().notifyAll();
        }         
    }
}
