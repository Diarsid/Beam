/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import static java.lang.Math.abs;

/**
 *
 * @author Diarsid
 */
public class MathUtil {
    
    private MathUtil() {}
    
    public static int halfRoundUp(int num) {
        return (num / 2) + (num % 2);
    }
    
    public static double ratio(int less, int more) {
        return (double) less / (double) more;
    }
    
    public static double onePointRatio(int less, int more) {
        return 1.0 + ( (double) less / (double) more );
    }
    
    public static int absDiff(int one, int two) {
        if ( one == two ) {
            return 0;
        } else {
            return abs(one - two);
        }
    }
    
    public static boolean isBetween(double from, double mid, double to) {
        return 
                mid > from &&
                mid < to;
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
