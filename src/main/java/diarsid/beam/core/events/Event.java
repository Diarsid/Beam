/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

/**
 *
 * @author Diarsid
 */
public final class Event {
    
    private final String type;
    private Object payload;
    
    public Event(String type, Object payload) {
        this.payload = payload;
        this.type = type;
    }
    
    String type() {
        return this.type;
    }
    
    public static Event event(String type, Object payload) {
        return new Event(type, payload);
    }
    
    public Object payload() {
        return this.payload;
    }
    
    public void fireAsync() {
        BeamEventRuntime.fireAsync(this);
    }
}
