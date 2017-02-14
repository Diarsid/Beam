/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;

import diarsid.beam.core.base.util.CollectionsUtils;

import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class UserReaction {
    
    private static final List<String> YES_PATTERNS;
    private static final List<String> NO_PATTERNS;
    private static final List<String> REJECT_PATTERNS;
    
    static {
        YES_PATTERNS = CollectionsUtils.toUnmodifiableList("y", "+", "yes", "ye", "true", "enable");
        NO_PATTERNS = CollectionsUtils.toUnmodifiableList("n", "no", "-", "false", "disable");
        REJECT_PATTERNS = CollectionsUtils.toUnmodifiableList(".", "", "s", "stop", " ");
    }
    
    private UserReaction() {
    }
    
    public static boolean isYes(String input) {
        return containsWordInIgnoreCase(YES_PATTERNS, input);
    }
    
    public static boolean isNo(String input) {
        return containsWordInIgnoreCase(NO_PATTERNS, input);
    }
    
    public static boolean isRejection(String input) {
        return containsWordInIgnoreCase(REJECT_PATTERNS, input);
    }
}
