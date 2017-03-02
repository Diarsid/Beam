/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

/**
 *
 * @author Diarsid
 */
public class IntUtil {
    
    public IntUtil() {
    }
    
    public static int adjustBetween(int valueToAdjust, int fromInclusive, int toInclusive) {
        if ( valueToAdjust > toInclusive ) {
            return toInclusive;
        } else if ( valueToAdjust < fromInclusive ) {
            return fromInclusive;
        } else {
            return valueToAdjust;
        }
    }
}
