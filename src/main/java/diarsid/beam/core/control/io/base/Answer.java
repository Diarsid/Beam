/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class Answer implements Serializable {
    
    private static final Answer EMPTY_CHOICE;
    static {
        EMPTY_CHOICE = new Answer(null);
    }
    
    private final Variant chosen;
        
    private Answer(Variant chosen) {
        this.chosen = chosen;
    }
    
    public static Answer noAnswer() {
        return EMPTY_CHOICE;
    }
    
    public static Answer answerOf(Variant variant) {
        return new Answer(variant);
    }
    
    public boolean isPresent() {
        return nonNull(this.chosen);
    }
    
    public boolean isNotPresent() {
        return isNull(this.chosen);
    }
    
    public Variant get() {
        return this.chosen;
    }
}
