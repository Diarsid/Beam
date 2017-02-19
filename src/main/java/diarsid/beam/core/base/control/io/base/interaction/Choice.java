/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.io.Serializable;

import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isNo;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isRejection;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isYes;

/**
 *
 * @author Diarsid
 */
public enum Choice implements Serializable {
    
    POSTIVE,
    NEGATIVE,
    REJECT,
    CHOICE_NOT_MADE;
    
    public static Choice choiceOfPattern(String input) {
        if ( isYes(input) ) {
            return POSTIVE;
        } else if ( isNo(input) ) {
            return NEGATIVE;
        } else if ( isRejection(input) ) {
            return REJECT;
        } else {
            return CHOICE_NOT_MADE;
        }
    }
    
    public boolean isPositive() {
        return this.equals(POSTIVE);
    }
    
    public boolean isNotPositive() {
        return ! this.equals(POSTIVE);
    }
    
    public boolean isNegative() {
        return this.equals(NEGATIVE);
    }
    
    public boolean isRejected() {
        return this.equals(REJECT);
    }
    
    public boolean isNotMade() {
        return this.equals(CHOICE_NOT_MADE);
    }
}
