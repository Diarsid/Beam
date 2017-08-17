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
public class DecimalUtil {
    
    private DecimalUtil() {}
    
    public static double ratio(int less, int more) {
        return (double) less / (double) more;
    }
    
    public static double onePointRatio(int less, int more) {
        return 1.0 + ( (double) less / (double) more );
    }
}
