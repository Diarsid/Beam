/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import java.util.HashSet;
import java.util.Set;

import static diarsid.beam.core.events.BeamEventRuntime.registerCallbackForType;
import static diarsid.beam.core.events.BeamEventRuntime.unregisterCallbacksFor;

/**
 *
 * @author Diarsid
 */
public class Subscription {
    
    private final String type;
    private final Set<EventCallback> callbacks;
    
    public Subscription(String type) {
        this.type = type;
        this.callbacks = new HashSet<>();
    }
    
    public Subscription withCallback(EventCallback callback) {
        registerCallbackForType(this.type, callback);
        this.callbacks.add(callback);
        return this;
    }
    
    public void unsubscribe() {
        unregisterCallbacksFor(this.type, this.callbacks);
    }
}
