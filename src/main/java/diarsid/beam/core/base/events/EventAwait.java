/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.events;

import java.util.Optional;
import java.util.function.Supplier;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;

import static diarsid.beam.core.base.events.BeamEventRuntime.fireAsync;

/**
 *
 * @author Diarsid
 */
public class EventAwait {
    
    private final Object monitor;

    EventAwait() {
        this.monitor = new Object();
    }
    
    protected final Object monitor() {
        return this.monitor;
    }
    
    public void thenDo(CallbackEmpty callback) {
        synchronized ( this.monitor ) {
            try {
                this.monitor.wait();
            } catch (InterruptedException ignore) {
                // nothing
                return;
            }
        }
        callback.call();
    }
    
    public void thenFire(String eventType) {
        synchronized ( this.monitor ) {
            try {
                this.monitor.wait();
            } catch (InterruptedException ignore) {
                // nothing
                return;
            }
        }
        fireAsync(eventType);
    }
    
    public void thenFire(String eventType, Object payload) {
        synchronized ( this.monitor ) {
            try {
                this.monitor.wait();
            } catch (InterruptedException ignore) {
                // nothing
                return;
            }
        }
        fireAsync(eventType, payload);
    }
    
    public <T> Optional<T> thenGet(Supplier<T> supplierT) {
        synchronized ( this.monitor ) {
            try {
                this.monitor.wait();
            } catch (InterruptedException ignore) {
                // nothing
                return Optional.empty();
            }
        }
        return Optional.ofNullable(supplierT.get());
    }
    
    public void thenProceed() {
        synchronized ( this.monitor ) {
            try {
                this.monitor.wait();
            } catch (InterruptedException ingore) {
                // nothing
            }
        }
    }
    
    void notifyAwaitedOnEvent() {
        synchronized ( this.monitor ) {
            this.monitor.notifyAll();
        }
    }
}
