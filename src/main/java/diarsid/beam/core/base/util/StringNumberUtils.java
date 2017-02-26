/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

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
}
