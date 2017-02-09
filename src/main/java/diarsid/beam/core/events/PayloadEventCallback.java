/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.events;

import diarsid.beam.core.domain.actions.Callback;

/**
 *
 * @author Diarsid
 */
@FunctionalInterface
public interface PayloadEventCallback extends Callback {
    void onEvent(String eventType, Object payload);
}
