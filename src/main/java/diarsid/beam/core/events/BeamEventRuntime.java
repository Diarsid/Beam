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

import diarsid.beam.core.events.BeamEvent;

/**
 *
 * @author Diarsid
 */
public class BeamEventRuntime {
    
    private static final Map<Class, Set<BeamEventCallback>> CALLBACKS;
    private static final ExecutorService EXECUTOR;
    
    static {
        CALLBACKS = new HashMap<>();
        EXECUTOR = new ScheduledThreadPoolExecutor(3);
    }
    
    private BeamEventRuntime() {
    }
    
    public static void registerForEvent(Class eventClass, BeamEventCallback callback) {
        if ( CALLBACKS.containsKey(eventClass) ) {
            CALLBACKS.get(eventClass).add(callback);
        } else {
            CALLBACKS.put(eventClass, new HashSet<>());
            CALLBACKS.get(eventClass).add(callback);
        }
    }
    
    protected static void fireEventAsync(BeamEvent event) {
        for (BeamEventCallback callback : CALLBACKS.get(event.getClass())) { 
            EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    callback.onEvent(event);
                }
            });
        }        
    }
}
