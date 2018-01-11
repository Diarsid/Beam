/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.events;

import java.util.Optional;
import java.util.function.Supplier;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;

/**
 *
 * @author Diarsid
 */
public class EventAwait {
    
    private final Object monitor;

    EventAwait() {
        this.monitor = new Object();
    }
    
    public void thenDo(CallbackEmpty callback) {
        synchronized ( this.monitor ) {
            try {
                this.monitor.wait();
            } catch (InterruptedException ingore) {
                // nothing
                return;
            }
        }
        callback.call();
    }
    
    public <T> Optional<T> thenGet(Supplier<T> supplierT) {
        synchronized ( this.monitor ) {
            try {
                this.monitor.wait();
            } catch (InterruptedException ingore) {
                // nothing
                return Optional.empty();
            }
        }
        return Optional.ofNullable(supplierT.get());
    }
    
    void notifyAwaitedOnEvent() {
        synchronized ( this.monitor ) {
            this.monitor.notifyAll();
        }
    }
}
