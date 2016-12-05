/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.Collection;

import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class CollectionsUtils {
    
    private CollectionsUtils() {
    }
    
    public static boolean nonEmpty(Collection c) {
        return ( ! c.isEmpty() );
    }
    
    public static boolean nonNullNonEmpty(Collection c) {
        return ( 
                nonNull(c) && 
                ! c.isEmpty() );
    }
}
