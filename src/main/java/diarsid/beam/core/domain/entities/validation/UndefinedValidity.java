/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.validation;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.domain.entities.validation.Validities.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.Validities.validationOk;

/**
 *
 * @author Diarsid
 */
public class UndefinedValidity {
    
    private static final boolean MUTABLE_ONCE = true;
    private static final boolean MUTABLE_MULTIPLE = false;
    
    private final boolean isMutableOnlyOnce;
    private boolean ingoreSubsequentDefineCall;
    private Validity validity;
    
    private UndefinedValidity(boolean mutableOnce) {
        this.isMutableOnlyOnce = mutableOnce;
        this.ingoreSubsequentDefineCall = false;
        this.validity = null;
    }
    
    public static UndefinedValidity undefinedValidityMutableOnce() {
        return new UndefinedValidity(MUTABLE_ONCE);
    }
    
    public static UndefinedValidity undefinedValidityMutableMultiple() {
        return new UndefinedValidity(MUTABLE_MULTIPLE);
    }
    
    public UndefinedValidity whenUndefinedThen() {
        this.ingoreSubsequentDefineCall = true;
        return this;
    }
    
    public boolean isDefined() {
        return nonNull(this.validity);
    }
    
    private void isProhibitedToModify() {
        throw new ValidationException(
                "This undefined validity is already defined and can be defined only once!");
    }
    
    private boolean checkCallIgnore() {
        if ( this.ingoreSubsequentDefineCall ) {
            this.ingoreSubsequentDefineCall = false;
            return true;
        }
        return false;
    }
    
    public void set(Validity validity) {
        if ( this.checkCallIgnore() ) {
            return;
        }
        if ( this.isDefined() && this.isMutableOnlyOnce ) {
            this.isProhibitedToModify();
        }
        this.validity = validity;
    }
    
    public void ok() {
        if ( this.checkCallIgnore() ) {
            return;
        }
        if ( this.isDefined() && this.isMutableOnlyOnce ) {
            this.isProhibitedToModify();
        }
        this.validity = validationOk();
    }
    
    public void failsWith(String description) {
        if ( this.checkCallIgnore() ) {
            return;
        }
        if ( this.isDefined() && this.isMutableOnlyOnce ) {
            this.isProhibitedToModify();
        }
        this.validity = validationFailsWith(description);
    }
    
    public void fails() {
        if ( this.checkCallIgnore() ) {
            return;
        }
        if ( this.isDefined() && this.isMutableOnlyOnce ) {
            this.isProhibitedToModify();
        }
        this.validity = Validities.fail();
    }
    
    public Validity get() {
        return this.validity;
    }
    
}
