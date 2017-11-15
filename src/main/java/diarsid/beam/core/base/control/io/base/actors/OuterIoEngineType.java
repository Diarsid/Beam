/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.actors;

import java.io.Serializable;

/**
 *
 * @author Diarsid
 */
public enum OuterIoEngineType implements Serializable {
    REMOTE (true), 
    IN_MACHINE (false);
    
    private final boolean limitedBySlots;
    
    private OuterIoEngineType(boolean limitedBySlots) {
        this.limitedBySlots = limitedBySlots;
    }
    
    public boolean isLimitedBySlots() {
        return this.limitedBySlots;
    }
}
