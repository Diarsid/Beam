/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.events;

/**
 *
 * @author Diarsid
 */
public interface ThenFireAsyncConditionally {
    
    void thenFireAsync(String eventType);
    
    void thenFireAsync(String eventType, Object payload);
}
