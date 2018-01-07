/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import static java.lang.Character.isDigit;
import static java.lang.Integer.parseInt;

/**
 *
 * @author Diarsid
 */
public class StringNumberUtils {
    
    private StringNumberUtils() {
    }
    
    public static boolean isNumeric(String s) {
        return s.matches("\\d+");
    }
    
    public static boolean isNumericRange(String s) {
        return s.matches("\\d+-\\d+");
    }
    
    public static boolean notNumeric(String s) {
        return ! s.matches("\\d+");
    }
    
    public static int parseNumberElseZero(String s) {
        try {
            return parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public static String removeLeadingDigitsFrom(String s) {
        int offset = 0;
        while ( isDigit(s.charAt(offset))) {            
            offset++;
        }
        return s.substring(offset);
    }
}
