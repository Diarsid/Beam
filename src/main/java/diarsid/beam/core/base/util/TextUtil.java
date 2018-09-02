/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.Comparator;

import static java.util.Objects.isNull;

import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_TOO_LONG;
import static diarsid.beam.core.base.util.StringsLengthComparator.LONGER_STRINGS_FIRST;
import static diarsid.beam.core.base.util.StringsLengthComparator.SHORTER_STRINGS_FIRST;

/**
 *
 * @author Diarsid
 */
public class TextUtil {
    
    private final static StringsLengthComparator SHORTER_FIRST_AND_SPACES;
    private final static StringsLengthComparator SHORTER_FIRST;
    private final static StringsLengthComparator LONGER_FIRST_AND_SPACES;
    private final static StringsLengthComparator LONGER_FIRST;
    
    static {
        SHORTER_FIRST_AND_SPACES = new StringsLengthComparator(SHORTER_STRINGS_FIRST, true);
        SHORTER_FIRST = new StringsLengthComparator(SHORTER_STRINGS_FIRST, false);
        LONGER_FIRST_AND_SPACES = new StringsLengthComparator(LONGER_STRINGS_FIRST, true);
        LONGER_FIRST = new StringsLengthComparator(LONGER_STRINGS_FIRST, false);
    }
    
    private TextUtil() {}

    public static String lineAtCaret(String text, int caret) {
        if ( caret < 0 || caret >= text.length() ) {
            return "";
        }
        
        int previousLineFeed = text.lastIndexOf('\n', caret);
        if ( previousLineFeed < 0 ) {
            previousLineFeed = 0;
        }
        
        int nextLineFeed = text.indexOf('\n', caret);
        if ( nextLineFeed < 0 ) {
            nextLineFeed = text.length();
        }
        
        if ( previousLineFeed == nextLineFeed ) {
            previousLineFeed = text.lastIndexOf('\n', caret - 1);
        }
        
        if ( previousLineFeed != 0 ) {
            previousLineFeed++;
        }
        String lineAtCaret = text.substring(previousLineFeed, nextLineFeed);
        
        return lineAtCaret;
    }
    
    public static String shrinkIfTooLong(String target, int lengthTreshold) {
        if ( target.length() > lengthTreshold ) {
            int deltaRight = SIGN_OF_TOO_LONG.length() / 2;
            int deltaLeft = SIGN_OF_TOO_LONG.length() - deltaRight;
            int rightSideLength = lengthTreshold / 2;
            int leftSideLength = lengthTreshold - rightSideLength;
            return 
                    target.substring(0, leftSideLength - deltaLeft - 1) +
                    SIGN_OF_TOO_LONG +
                    target.substring(
                            target.length() - rightSideLength - deltaRight, 
                            target.length());
        }
        return target;
    }
    
    public static int indexOfFirstNonSpaceIn(String target) {
        if ( isNull(target) || target.isEmpty() ) {
            return -1;
        }
        
        for (int i = 0; i < target.length(); i++) {
            if ( target.charAt(i) != ' ' ) {
                return i;
            }            
        }
        return -1;
    }
    
    public static int indexOfFirstNonSpaceIn(String target, int fromInclusive, int toExclusive) {
        if ( isNull(target) 
             || target.isEmpty() 
             || fromInclusive < 0 
             || toExclusive > target.length() 
             || fromInclusive >= toExclusive ) {
            return -1;
        }
        
        for (int i = fromInclusive; i < toExclusive; i++) {
            if ( target.charAt(i) != ' ' ) {
                return i - fromInclusive;
            }            
        }
        return -1;
    }
    
    public static Comparator<String> shorterStringsFirstNotCountingSpaces() {
        return SHORTER_FIRST;
    }
}
