/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.domain.actions.Callback;

import static diarsid.beam.core.events.BeamEventRuntime.registerCallbackForType;

/**
 *
 * @author Diarsid
 */
public class SubscriptionBuilder {
    
    private final String eventType;
    private final Set<Callback> callbacks;
    
    SubscriptionBuilder(String eventType) {
        this.eventType = eventType;
        this.callbacks = new HashSet<>();
    }
    public SubscriptionBuilder withCallback(EmptyEventCallback callback) {
        registerCallbackForType(this.eventType, callback);
        this.callbacks.add(callback);
        return this;
    }
    
    public SubscriptionBuilder withCallback(PayloadEventCallback callback) {
        registerCallbackForType(this.eventType, callback);
        this.callbacks.add(callback);
        return this;
    }
    
    public Subscription done() {
        return new Subscription(this.eventType, this.callbacks);
    }
}
