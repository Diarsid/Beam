/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.Collection;

import static java.lang.Math.abs;
import static java.lang.Math.round;

/**
 *
 * @author Diarsid
 */
public class MathUtil {
    
    private MathUtil() {}
    
    public static boolean isOdd(int i) {
        return i % 2 > 0;
    }
    
    public static boolean isEven(int i) {
        return i % 2 == 0;
    }
    
    public static int halfRoundUp(int num) {
        return (num / 2) + (num % 2);
    }
    
    public static double ratio(int part, int whole) {
        return (double) part / (double) whole;
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
    
    public static double absDiff(double one, double two) {
        if ( one == two ) {
            return 0;
        } else {
            return abs(one - two);
        }
    }
    
    public static int absDiffOneIfZero(int one, int two) {
        if ( one == two || one == -two ) {
            return 1;
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
    
    public static int percentAsInt(int part, int whole) {
        return ( part * 100 ) / whole ;
    }
    
    public static int percentAsIntOf(int target, int percent) {
        return ( target * percent ) / 100;
    }
    
    public static float percentAsFloat(float part, float whole) {
        return ( part * 100f ) / whole ;
    }
    
    public static float percentAsFloatOf(float target, float percent) {
        return ( target * percent ) / 100f;
    }
    
    public static int meanSmartIngoringZeros(Collection<Integer> ints) {
        int sum = 0;
        int zeros = 0;
        
        for (Integer i : ints) {
            if ( i == 0 ) {
                zeros++;
            } else {
                sum = sum + i;
            }            
        }
        
        if ( sum == 0 ) {
            return 0;
        }
        
        int size = ints.size();
        if ( zeros == 0 ) {
            return round( (float) sum / size);
        } else {
            if ( zeros > size / 2 ) {
                return 0;
            } else {
                return round( (float) sum / (size - zeros) );
            }
        }        
    }
    
    public static int mean(int a, int b) {
        return ( a + b ) / 2;
    }
    
    public static int mean(int a, int b, int c) {
        return ( a + b + c ) / 3;
    }
    
    public static int mean(int a, int b, int c, int d) {
        return ( a + b + c + d ) / 4;
    }
        
    public static float mean(float a, float b) {
        return ( a + b ) / 2.0f;
    }
    
    public static float mean(float a, float b, float c) {
        return ( a + b + c ) / 3.0f;
    }
    
    public static float mean(float a, float b, float c, float d) {
        return ( a + b + c + d ) / 4.0f;
    }
    
    public static int square(int x) {
        return x * x;
    }
    
    public static double square(double d) {
        return d * d;
    }
    
    public static int cube(int x) {
        return x * x * x;
    }
}
