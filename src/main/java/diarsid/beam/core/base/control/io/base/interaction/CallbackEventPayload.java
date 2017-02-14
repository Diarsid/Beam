/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

/**
 *
 * @author Diarsid
 */
@FunctionalInterface
public interface CallbackEventPayload extends Callback {
    void onEvent(String eventType, Object payload);
}
