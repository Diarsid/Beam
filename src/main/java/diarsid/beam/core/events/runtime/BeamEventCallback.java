/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.events.runtime;

import diarsid.beam.core.events.BeamEvent;

/**
 *
 * @author Diarsid
 */
public interface BeamEventCallback<T extends BeamEvent> {
    
    public void onEvent(T event);
}
