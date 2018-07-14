/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import diarsid.beam.core.base.control.io.base.interaction.Callback;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEventPayload;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;

/**
 *
 * @author Diarsid
 */
public class BeamEventRuntime {
    
    private final static Map<String, Set<EventAwait>> EVENT_AWAITS;
    private final static Map<String, Set<CallbackEvent>> EMPTY_CALLBACKS;
    private final static Map<String, Set<CallbackEventPayload>> PAYLOAD_CALLBACKS;
    
    private final static Lock EMPTY_CALLBACKS_LOCK;
    private final static Lock PAYLOAD_CALLBACKS_LOCK;
    private final static Lock EVENT_AWAITS_LOCK;
    
    private final static ThenFireAsyncConditionally WHEN_FALSE;
    private final static ThenFireAsyncConditionally WHEN_TRUE;
    
    static {
        EVENT_AWAITS = new HashMap<>();
        EMPTY_CALLBACKS = new HashMap<>();
        PAYLOAD_CALLBACKS = new HashMap<>();
        
        EMPTY_CALLBACKS_LOCK = new ReentrantLock();
        PAYLOAD_CALLBACKS_LOCK = new ReentrantLock();
        EVENT_AWAITS_LOCK = new ReentrantLock();
        
        WHEN_FALSE = new ThenFireAsyncConditionally() {
            
            @Override
            public void thenFireAsync(String eventType) {
                // do nothing.
            }

            @Override
            public void thenFireAsync(String eventType, Object payload) {
                // do nothing.
            }
        };
        WHEN_TRUE = new ThenFireAsyncConditionally() {
            
            @Override
            public void thenFireAsync(String eventType) {
                fireAsync(eventType);
            }

            @Override
            public void thenFireAsync(String eventType, Object payload) {
                fireAsync(eventType, payload);
            }
        };
    }
    
    private BeamEventRuntime() {
    }
    
    public static SubscriptionBuilder onEvent(String type) {
        return new SubscriptionBuilder(type);
    }
    
    public static Subscription subscribe(SubscriptionBuilder subscriptionBuilder) {
        return subscriptionBuilder.finish();
    }
    
    static void registerCallbackForType(String type, CallbackEvent callback) {
        try {            
            EMPTY_CALLBACKS_LOCK.lock();
            
            if ( EMPTY_CALLBACKS.containsKey(type) ) {            
                EMPTY_CALLBACKS.get(type).add(callback);
            } else {                
                EMPTY_CALLBACKS.put(type, new HashSet<>());
                EMPTY_CALLBACKS.get(type).add(callback);
            }
            
        } finally {
            EMPTY_CALLBACKS_LOCK.unlock();
        }
    }
    
    static void registerCallbackForType(String type, CallbackEventPayload callback) {
        try {            
            PAYLOAD_CALLBACKS_LOCK.lock();
            
            if ( PAYLOAD_CALLBACKS.containsKey(type) ) {            
                PAYLOAD_CALLBACKS.get(type).add(callback);
            } else {                
                PAYLOAD_CALLBACKS.put(type, new HashSet<>());
                PAYLOAD_CALLBACKS.get(type).add(callback);
            }
            
        } finally {
            PAYLOAD_CALLBACKS_LOCK.unlock();
        }
    }
    
    static void unregisterCallbacksFor(String type, Set<Callback> callbacksToUnreg) {
        try {            
            EMPTY_CALLBACKS_LOCK.lock();
            PAYLOAD_CALLBACKS_LOCK.lock();
            
            PAYLOAD_CALLBACKS.get(type).removeAll(callbacksToUnreg);
            EMPTY_CALLBACKS.get(type).removeAll(callbacksToUnreg);
            
        } finally {
            EMPTY_CALLBACKS_LOCK.unlock();
            PAYLOAD_CALLBACKS_LOCK.unlock();
        }
    }
    
    public static void fireAsync(String eventType) {
        asyncDo(() -> {
            try {
                EVENT_AWAITS_LOCK.lock();
                EMPTY_CALLBACKS_LOCK.lock();
                
                Set<CallbackEvent> callbacks = EMPTY_CALLBACKS.get(eventType);
                if ( nonNull(callbacks) ) {
                    callbacks
                            .stream()
                            .forEach(emptyCallback -> {
                                emptyCallback.onEvent(eventType);
                            });
                }
                
                Set<EventAwait> eventAwaits = EVENT_AWAITS.get(eventType);
                if ( nonNull(eventAwaits) ) {
                    eventAwaits
                            .stream()
                            .forEach(eventAwait -> eventAwait.notifyAwaitedOnEvent());
                    EVENT_AWAITS.remove(eventType);
                }                        
                
            } finally {
                EVENT_AWAITS_LOCK.unlock();
                EMPTY_CALLBACKS_LOCK.unlock();
            }    
        });
    }
    
    public static void fireAsync(String eventType, Object payload) {
        asyncDo(() -> {
            try {
                EVENT_AWAITS_LOCK.lock();
                EMPTY_CALLBACKS_LOCK.lock();
                PAYLOAD_CALLBACKS_LOCK.lock();
                
                Set<CallbackEvent> callbacks = EMPTY_CALLBACKS.get(eventType);
                if ( nonNull(callbacks) ) {
                    callbacks
                            .stream()
                            .forEach(emptyCallback -> {
                                emptyCallback.onEvent(eventType);
                            });
                }
                        
                Set<CallbackEventPayload> callbackPayloads = PAYLOAD_CALLBACKS.get(eventType);
                if ( nonNull(callbackPayloads) ) {
                    callbackPayloads
                            .stream()
                            .forEach(payloadCallback -> {
                                payloadCallback.onEvent(eventType, payload);
                            });
                }     
                
                Set<EventAwait> eventAwaits = EVENT_AWAITS.get(eventType);
                if ( nonNull(eventAwaits) ) {
                    eventAwaits
                            .stream()
                            .forEach(eventAwait -> eventAwait.notifyAwaitedOnEvent());
                    EVENT_AWAITS.remove(eventType);
                }
                
            } finally {
                EVENT_AWAITS_LOCK.unlock();
                EMPTY_CALLBACKS_LOCK.unlock();
                PAYLOAD_CALLBACKS_LOCK.unlock();
            }     
        });
    }
    
    public static ThenFireAsyncConditionally when(boolean condition) {
        if ( condition ) {
            return WHEN_TRUE;
        } else {
            return WHEN_FALSE;
        }
    } 
    
    public static EventAwait awaitFor(String event) {
        try {
            EVENT_AWAITS_LOCK.lock();
            
            EventAwait eventAwait = new EventAwait();
            
            if ( EVENT_AWAITS.containsKey(event) ) {
                EVENT_AWAITS.get(event).add(eventAwait);
            } else {
                EVENT_AWAITS.put(event, new HashSet<>());
                EVENT_AWAITS.get(event).add(eventAwait);
            }
            
            return eventAwait;
                    
        } finally {
            EVENT_AWAITS_LOCK.unlock();
        }        
    }
    
    public static ThenFireAsyncConditionally ifAwaitedFor(String event) {
        try {
            EVENT_AWAITS_LOCK.lock();   
            
            return when(EVENT_AWAITS.containsKey(event));
            
        } finally {
            EVENT_AWAITS_LOCK.unlock();
        }
    }
}
