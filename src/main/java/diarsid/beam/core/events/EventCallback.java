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
@FunctionalInterface
public interface EventCallback {
    
    public void onEvent(Event event);
}