/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.beam.core.domain.actions.Callback;

import static java.lang.Runtime.getRuntime;

import static diarsid.beam.core.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.util.Logs.log;

/**
 *
 * @author Diarsid
 */
public class BeamEventRuntime {
    
    private static final Map<String, Set<EmptyEventCallback>> EMPTY_CALLBACKS;
    private static final Map<String, Set<PayloadEventCallback>> PAYLOAD_CALLBACKS;
    private static final ExecutorService EXECUTOR;
    private static final Object CALLBACKS_SYNC_MONITOR;
    
    static {
        EMPTY_CALLBACKS = new HashMap<>();
        PAYLOAD_CALLBACKS = new HashMap<>();
        EXECUTOR = new ScheduledThreadPoolExecutor(5);
        CALLBACKS_SYNC_MONITOR = new Object();
        getRuntime().addShutdownHook(new Thread(() -> shutdownEventRuntime()));
    }
    
    private BeamEventRuntime() {
    }
    
    public static SubscriptionBuilder subscribeOn(String type) {
        return new SubscriptionBuilder(type);
    }
    
    private static void shutdownEventRuntime() {
        EXECUTOR.shutdown();
        log(BeamEventRuntime.class, "shutdown.");
    }
    
    static void registerCallbackForType(String type, EmptyEventCallback callback) {
        synchronized ( CALLBACKS_SYNC_MONITOR ) {
            if ( EMPTY_CALLBACKS.containsKey(type) ) {            
                EMPTY_CALLBACKS.get(type).add(callback);
            } else {                
                EMPTY_CALLBACKS.put(type, new HashSet<>());
                EMPTY_CALLBACKS.get(type).add(callback);
            }
        }
    }
    
    static void registerCallbackForType(String type, PayloadEventCallback callback) {
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
