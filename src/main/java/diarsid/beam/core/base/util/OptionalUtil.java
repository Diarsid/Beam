/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.Optional;

/**
 *
 * @author Diarsid
 */
public class OptionalUtil {
    
    private OptionalUtil() {}
    
    public static boolean isNotPresent(Optional optional) {
        return ! optional.isPresent();
    }
}
