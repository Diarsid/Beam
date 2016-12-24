/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;

/**
 *
 * @author Diarsid
 */
public class IoChoice implements Serializable {
    
    private static final IoChoice EMPTY_CHOICE;
    static {
        EMPTY_CHOICE = new IoChoice("");
    }
    
    private final String chosen;
        
    private IoChoice(String chosen) {
        this.chosen = chosen;
    }
    
    public static IoChoice choiceNotMade() {
        return EMPTY_CHOICE;
    }
    
    public static IoChoice choiceOf(Variant variant) {
        return new IoChoice(variant.get());
    }
    
    public boolean isMade() {
        return ! this.chosen.isEmpty();
    }
    
    public String get() {
        return this.chosen;
    }
}
