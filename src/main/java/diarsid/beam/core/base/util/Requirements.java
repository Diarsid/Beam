/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import diarsid.beam.core.base.exceptions.RequirementException;

import static java.util.Objects.isNull;

/**
 *
 * @author Diarsid
 */
public class Requirements {
    
    private Requirements() {
    }
    
    public static void requireNonEmpty(String s, String requirement) 
            throws RequirementException {
        if ( isNull(s) || s.isEmpty() ) {
            throw new RequirementException("Requirement violation: " + requirement);
        }
    }
    
    public static void requireNotNull(Object obj, String requirement) {
        if ( obj == null ) {
            throw new RequirementException(requirement);
        }
    }
    
    public static void requireNull(Object obj, String requirement) {
        if ( obj != null ) {
            throw new RequirementException(requirement);
        }
    }
    
    public static void requireEquals(Object o1, Object o2, String requirement) 
            throws RequirementException {
        if ( ! o1.equals(o2) ) {
            throw new RequirementException("Requirement violation: " + requirement);
        }
    }
}
