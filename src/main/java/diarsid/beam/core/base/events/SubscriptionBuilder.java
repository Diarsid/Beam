/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.events;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEventPayload;

import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.base.control.io.base.interaction.Callback;

import static diarsid.beam.core.base.events.BeamEventRuntime.registerCallbackForType;

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
    public SubscriptionBuilder withCallback(CallbackEvent callback) {
        registerCallbackForType(this.eventType, callback);
        this.callbacks.add(callback);
        return this;
    }
    
    public SubscriptionBuilder withCallback(CallbackEventPayload callback) {
        registerCallbackForType(this.eventType, callback);
        this.callbacks.add(callback);
        return this;
    }
    
    public Subscription done() {
        return new Subscription(this.eventType, this.callbacks);
    }
}
