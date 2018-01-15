/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import static java.util.Objects.isNull;

import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_TOO_LONG;

/**
 *
 * @author Diarsid
 */
public class TextUtil {
    
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
}
