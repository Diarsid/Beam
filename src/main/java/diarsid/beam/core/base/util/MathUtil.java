/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.Collection;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.String.format;

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
    
    public static void main(String... args) {
        for (int i = 1; i < 6; i++) {
            System.out.println("i: " + i);
            System.out.println("   pow   i^pow");
            for (double pow = 1.0; pow > 0; pow = pow - 0.1) {
                System.out.println(format("   %.1f  %.2f", pow, pow(i, pow)));
            }
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
        return part * 100 / whole ;
    }
    
    public static int mean(Collection<Integer> ints) {
        int sum = 0;
        for (Integer i : ints) {
            sum = sum + i;
        }
        return sum / ints.size();
    }
}
