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
public class ArraysUtil {
    
    private ArraysUtil() {
    }
    
    public static boolean isEmpty(Object[] arr) {
        return arr.length == 0 ;
    }
    
    public static char[] array(char... chars) {
        return chars;
    } 
}
