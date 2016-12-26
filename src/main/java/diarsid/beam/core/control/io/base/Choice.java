/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.base;

import java.io.Serializable;
import java.util.List;

import static diarsid.beam.core.util.CollectionUtils.toUnmodifiableList;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;

/**
 *
 * @author Diarsid
 */
public enum Choice implements Serializable {
    
    POSTIVE,
    NEGATIVE,
    NOT_MADE;
    
    private static final List<String> YES_PATTERNS;
    private static final List<String> NO_PATTERNS;
    private static final List<String> REJECT_PATTERNS;
    
    static {
        YES_PATTERNS = toUnmodifiableList("y", "+", "yes", "ye", "true", "enable");
        NO_PATTERNS = toUnmodifiableList("n", "no", "-", "false", "disable");
        REJECT_PATTERNS = toUnmodifiableList(".", "", "s", "stop", " ");
    }
    
    public static Choice choiceOfPattern(String input) {
        if ( containsWordInIgnoreCase(YES_PATTERNS, input) ) {
            return POSTIVE;
        } else if ( containsWordInIgnoreCase(NO_PATTERNS, input) ) {
            return NEGATIVE;
        } else if ( containsWordInIgnoreCase(REJECT_PATTERNS, input) ) {
            return NOT_MADE;
        } else {
            return NOT_MADE;
        }
    }
    
    public boolean isPositive() {
        return this.equals(POSTIVE);
    }
    
    public boolean isNegative() {
        return this.equals(NEGATIVE);
    }
    
    public boolean isNotMade() {
        return this.equals(NOT_MADE);
    }
}
