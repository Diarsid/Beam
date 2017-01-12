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
public class ValidationResults {
    
    private static final ValidationResult INITIAL_FAIL_RESULT;
    private static final ValidationResult OK_RESULT;
    
    static {
        INITIAL_FAIL_RESULT = new ValidationResultObject(false, "");
        OK_RESULT = new ValidationResultObject(true, "Validation is successful.");
    }
    
    private ValidationResults() {
    }
    
    public static ValidationResult validationOk() {
        return OK_RESULT;
    }
    
    public static ValidationResult validationFailsWith(String description) {
        return new ValidationResultObject(false, description);
    }
    
    public static ValidationResult fail() {
        return INITIAL_FAIL_RESULT;
    }
}
