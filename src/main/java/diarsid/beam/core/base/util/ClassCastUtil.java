/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.validation.Validity;

/**
 *
 * @author Diarsid
 */
public class ClassCastUtil {
    
    private ClassCastUtil() {}
    
    public static String asString(Object object) {
        return (String) object;
    }
    
    public static WebPlace asWebPlace(Object object) {
        return (WebPlace) object;
    }
    
    public static Validity asValidationResult(Object object) {
        return (Validity) object;
    }
}
