/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.inputparsing.common;

/**
 *
 * @author Diarsid
 */
public enum ArgumentInterception {
    
    NOT_INTERCEPTED,
    INTERCEPTED;

    public boolean ifContinue() {
        return this.equals(NOT_INTERCEPTED);
    }    
}
