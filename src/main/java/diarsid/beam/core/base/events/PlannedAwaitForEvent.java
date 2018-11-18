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
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class PlannedAwaitForEvent {
    
    protected boolean isAwaited;
    protected boolean isToBeNotified;

    PlannedAwaitForEvent() {
        this.isAwaited = false;
        this.isToBeNotified = false;
    }

    protected final void syncAwaitBewareOfNotifier() throws InterruptedException {
        synchronized ( this ) {
            this.isAwaited = true;
            if ( this.isToBeNotified ) {
                logFor(this).info("notifier thread is already awaiting to notify!");
                this.notifyAll();
                this.wait();
            } else {
                this.wait();
            }
        }
    }

    protected final void syncNotifyBewareOfAwaiting() throws InterruptedException{
        synchronized ( this ) {            
            this.isToBeNotified = true;
            if ( this.isAwaited ) {
                this.notifyAll();
            } else {
                logFor(this).info("notifier thread is awaiting for event to be awaited...");
                this.wait();
                this.notifyAll();
            }
        }         
    }
    
    public void awaitThenDo(CallbackEmpty callback) {
        try {
            this.syncAwaitBewareOfNotifier();
        } catch (InterruptedException ignore) {
            // nothing
            return;
        }
        callback.call();
    }
    
    public void awaitThenFire(String eventType) {
        try {
            this.syncAwaitBewareOfNotifier();
        } catch (InterruptedException ignore) {
            // nothing
            return;
        }
        fireAsync(eventType);
    }
    
    public void awaitThenFire(String eventType, Object payload) {
        try {
            this.syncAwaitBewareOfNotifier();
        } catch (InterruptedException ignore) {
            // nothing
            return;
        }
        fireAsync(eventType, payload);
    }
    
    public <T> Optional<T> awaitThenGet(Supplier<T> supplierT) {
        try {
            this.syncAwaitBewareOfNotifier();
        } catch (InterruptedException ignore) {
            // nothing
            return Optional.empty();
        }
        return Optional.ofNullable(supplierT.get());
    }
    
    public void awaitThenProceed() {
        try {
            this.syncAwaitBewareOfNotifier();
        } catch (InterruptedException ignore) {
            // nothing
        }
    }
    
    void notifyAwaitedOnEvent() {
        try {
            this.syncNotifyBewareOfAwaiting();
        } catch (InterruptedException ignore) {
            // nothing
        }
    }
}
