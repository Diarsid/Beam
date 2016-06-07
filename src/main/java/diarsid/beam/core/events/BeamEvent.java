/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Diarsid
 */
public abstract class BeamEvent {
    
    private final Map<String, Object> attributes;
    private boolean alreadyFired;
    
    protected BeamEvent() {
        this.attributes = new HashMap<>();
        this.alreadyFired = false;
    }
    
    protected BeamEvent with(String attribute, Object value) {
        if ( this.alreadyFired ) {
            return this;
        } else {
            this.attributes.put(attribute, value);
            return this;
        }        
    } 
    
    public final BeamEvent compile() {
        this.alreadyFired = true;
        return this;
    }
    
    protected Object get(String attribute) {
        return this.attributes.get(attribute);
    }
}
