/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import java.util.Set;

import diarsid.beam.core.domain.actions.Callback;

import static diarsid.beam.core.events.BeamEventRuntime.unregisterCallbacksFor;

/**
 *
 * @author Diarsid
 */
public class Subscription {
    
    private final String type;
    private final Set<Callback> callbacks;
    
    public Subscription(String type, Set<Callback> callbacks) {
        this.type = type;
        this.callbacks = callbacks;
    }
    
    public String type() {
        return this.type;
    }
    
    public void unsubscribe() {
        unregisterCallbacksFor(this.type, this.callbacks);
    }
}
