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
public abstract class WebPageEvent extends BeamEvent {
    
    protected WebPageEvent() {
        super();
    }
    
    public abstract Object getCause();
}
