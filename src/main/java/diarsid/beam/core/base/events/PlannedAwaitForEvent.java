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
public class PlannedAwaitForEvent {
    
    private final Object monitor;

    PlannedAwaitForEvent() {
        this.monitor = new Object();
    }
    
    protected final Object monitor() {
        return this.monitor;
    }
    
    public void awaitThenDo(CallbackEmpty callback) {
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
    
    public void awaitThenFire(String eventType) {
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
    
    public void awaitThenFire(String eventType, Object payload) {
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
    
    public <T> Optional<T> awaitThenGet(Supplier<T> supplierT) {
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
    
    public void awaitThenProceed() {
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
