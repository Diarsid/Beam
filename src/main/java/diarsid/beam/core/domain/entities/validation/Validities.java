/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities.validation;

/**
 *
 * @author Diarsid
 */
public class Validities {
    
    private static final Validity INITIAL_FAIL_RESULT;
    private static final Validity OK_RESULT;
    
    static {
        INITIAL_FAIL_RESULT = new ValidityImpl(false, "");
        OK_RESULT = new ValidityImpl(true, "Validation is successful.");
    }
    
    private Validities() {
    }
    
    public static Validity validationOk() {
        return OK_RESULT;
    }
    
    public static Validity validationFailsWith(String description) {
        return new ValidityImpl(false, description);
    }
    
    public static Validity fail() {
        return INITIAL_FAIL_RESULT;
    }
}
