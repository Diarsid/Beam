/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import java.util.HashMap;
import java.util.Map;

import static diarsid.beam.core.events.BeamEventRuntime.fireEventAsync;

/**
 *
 * @author Diarsid
 */
public abstract class BeamEvent {
    
    private final Map<String, Object> attributes;
    protected boolean alreadyFired;
    
    protected BeamEvent() {
        this.attributes = new HashMap<>();
        this.alreadyFired = false;
    }
    
    protected void set(String attribute, Object value) {
        if ( this.alreadyFired ) {
        } else {
            this.attributes.put(attribute, value);
        }        
    } 
    
    protected Object get(String attribute) {
        return this.attributes.get(attribute);
    }
    
    public abstract Object getCause();
    
    public static final class PrecompiledEvent <E extends BeamEvent> {
        
        protected final E constructableEvent;
        
        protected PrecompiledEvent(E event) {
            this.constructableEvent = event;
        }
        
        public void fireAsync() {
            this.constructableEvent.alreadyFired = true;
            fireEventAsync(this.constructableEvent);
        }
    }  
    
}
