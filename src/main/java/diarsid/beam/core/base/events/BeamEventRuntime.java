/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.events;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEventPayload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import diarsid.beam.core.base.control.io.base.interaction.Callback;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;

/**
 *
 * @author Diarsid
 */
public class BeamEventRuntime {
    
    private static final Map<String, Set<CallbackEvent>> EMPTY_CALLBACKS;
    private static final Map<String, Set<CallbackEventPayload>> PAYLOAD_CALLBACKS;
    private static final Object CALLBACKS_SYNC_MONITOR;
    
    static {
        EMPTY_CALLBACKS = new HashMap<>();
        PAYLOAD_CALLBACKS = new HashMap<>();
        CALLBACKS_SYNC_MONITOR = new Object();
    }
    
    private BeamEventRuntime() {
    }
    
    public static SubscriptionBuilder subscribeOn(String type) {
        return new SubscriptionBuilder(type);
    }
    
    static void registerCallbackForType(String type, CallbackEvent callback) {
        synchronized ( CALLBACKS_SYNC_MONITOR ) {
            if ( EMPTY_CALLBACKS.containsKey(type) ) {            
                EMPTY_CALLBACKS.get(type).add(callback);
            } else {                
                EMPTY_CALLBACKS.put(type, new HashSet<>());
                EMPTY_CALLBACKS.get(type).add(callback);
            }
        }
    }
    
    static void registerCallbackForType(String type, CallbackEventPayload callback) {
        synchronized ( CALLBACKS_SYNC_MONITOR ) {
            if ( PAYLOAD_CALLBACKS.containsKey(type) ) {            
                PAYLOAD_CALLBACKS.get(type).add(callback);
            } else {                
                PAYLOAD_CALLBACKS.put(type, new HashSet<>());
                PAYLOAD_CALLBACKS.get(type).add(callback);
            }
        }
    }
    
    static void unregisterCallbacksFor(String type, Set<Callback> callbacksToUnreg) {
        synchronized ( CALLBACKS_SYNC_MONITOR ) {
            PAYLOAD_CALLBACKS.get(type).removeAll(callbacksToUnreg);
            EMPTY_CALLBACKS.get(type).removeAll(callbacksToUnreg);
        }
    }
    
    public static void fireAsync(String eventType) {
        asyncDo(() -> {
            EMPTY_CALLBACKS
                    .get(eventType)
                    .stream()
                    .forEach(emptyCallback -> {
                        emptyCallback.onEvent(eventType);
                    });
        });
    }
    
    public static void fireAsync(String eventType, Object payload) {
        asyncDo(() -> {
            EMPTY_CALLBACKS
                    .get(eventType)
                    .stream()
                    .forEach(emptyCallback -> {
                        emptyCallback.onEvent(eventType);
                    });
            PAYLOAD_CALLBACKS
                    .get(eventType)
                    .stream()
                    .forEach(payloadCallback -> {
                        payloadCallback.onEvent(eventType, payload);
                    });
        });
    }
}
