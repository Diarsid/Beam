/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.exceptions;

/**
 *
 * @author Diarsid
 */
public class NullDependencyInjectionException extends RuntimeException{
    
    public NullDependencyInjectionException(String injectedClassName, String nullInjectionClassName){
        super("Dependency Injection in " + injectedClassName + " broken: "
                + nullInjectionClassName + " object is NULL.");
    }
}
