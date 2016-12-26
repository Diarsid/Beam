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

import static java.lang.Runtime.getRuntime;

import static diarsid.beam.core.util.CollectionsUtils.nonNullNonEmpty;
import static diarsid.beam.core.util.Logs.log;

/**
 *
 * @author Diarsid
 */
public class BeamEventRuntime {
    
    private static final Map<String, Set<EventCallback>> CALLBACKS;
    private static final ExecutorService EXECUTOR;
    private static final Object CALLBACKS_SYNC_MONITOR;
    
    static {
        CALLBACKS = new HashMap<>();
        EXECUTOR = new ScheduledThreadPoolExecutor(5);
        CALLBACKS_SYNC_MONITOR = new Object();
        getRuntime().addShutdownHook(new Thread(() -> shutdownEventRuntime()));
    }
    
    private BeamEventRuntime() {
    }
    
    public static Subscription subscribeOn(String type) {
        return new Subscription(type);
    }
    
    private static void shutdownEventRuntime() {
        EXECUTOR.shutdown();
        log(BeamEventRuntime.class, "shutdown.");
    }
    
    static void registerCallbackForType(String type, EventCallback callback) {
        if ( CALLBACKS.containsKey(type) ) {
            synchronized ( CALLBACKS_SYNC_MONITOR ) {
                CALLBACKS.get(type).add(callback);
            }
        } else {
            synchronized ( CALLBACKS_SYNC_MONITOR ) {
                CALLBACKS.put(type, new HashSet<>());
                CALLBACKS.get(type).add(callback);
            }            
        }
    }
    
    static void unregisterCallbacksFor(String type, Set<EventCallback> callbacksToUnreg) {
        Set<EventCallback> callbacksOfType = CALLBACKS.get(type);
        if ( nonNullNonEmpty(callbacksOfType) ) {
            synchronized ( CALLBACKS_SYNC_MONITOR ) { 
                callbacksOfType.removeAll(callbacksToUnreg);
            }
        }
    }
    
    public static void fireAsync(Event event) {
        Set<EventCallback> callbacks = CALLBACKS.get(event.type());
        if ( nonNullNonEmpty(callbacks) ) {
            callbacks.forEach((callback) -> EXECUTOR.submit(() -> callback.onEvent(event)));         
        }
    }
}
