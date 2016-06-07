/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events.runtime;

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
    
    private static final Map<Class, Set<BeamEventCallback>> callbacks;
    private static final ExecutorService exectutor;
    
    static {
        callbacks = new HashMap<>();
        exectutor = new ScheduledThreadPoolExecutor(3);
    }
    
    private BeamEventRuntime() {
    }
    
    public static void registerForEvent(Class eventClass, BeamEventCallback callback) {
        if ( callbacks.containsKey(eventClass) ) {
            callbacks.get(eventClass).add(callback);
        } else {
            callbacks.put(eventClass, new HashSet<>());
            callbacks.get(eventClass).add(callback);
        }
    }
    
    public static void fireEventAsync(BeamEvent event) {
        for (BeamEventCallback callback : callbacks.get(event.getClass())) { 
            exectutor.submit(new Runnable() {
                @Override
                public void run() {
                    callback.onEvent(event);
                }
            });
        }        
    }
}
