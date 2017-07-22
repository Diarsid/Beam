/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.validation;

import diarsid.beam.core.domain.entities.WebPlace;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.ClassCastUtil.asString;
import static diarsid.beam.core.base.util.ClassCastUtil.asValidationResult;
import static diarsid.beam.core.base.util.ClassCastUtil.asWebPlace;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationOk;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.TEXT_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.WEB_URL_RULE;

/**
 *
 * @author Diarsid
 */
public class Validation {
    
    private Validation() {}
    
    public static void validate(Object... objs) throws ValidationException {
        if ( objs.length > 0 ) {
            stream(objs)
                .filter(obj -> nonNull(obj))
                .forEach(obj -> discoverTypeAndValidate(obj));
        }        
    }
    
    private static void discoverTypeAndValidate(Object obj) {
        if ( obj instanceof ValidationResult ) {
            asValidationResult(obj).throwIfFail();
        } else if ( obj instanceof String ) {
            TEXT_RULE.applyTo(asString(obj)).throwIfFail();
        } else if ( obj instanceof WebPlace ) {
            throwIf(asWebPlace(obj).isUndefined(), "WebPlace is not defined.");
        }
    }
    
    private static void throwIf(boolean cause, String message) {
        if ( cause ) {
            throw new ValidationException(message);
        }
    }
    
    public static ValidationResult asName(String name) {
        return ENTITY_NAME_RULE.applyTo(name);
    }
    
    public static ValidationResult asNames(String... names) {
        return stream(names)
                .filter(name -> nonNull(name))
                .map(name -> ENTITY_NAME_RULE.applyTo(name))
                .filter(validity -> validity.isFail())
                .findFirst()
                .orElse(null);
    }
    
    public static ValidationResult asUrl(String url) {
        return WEB_URL_RULE.applyTo(url);
    }
    
    public static ValidationResult asOrder(int i) {
        if ( i < 0 ) {
            return validationFailsWith("Natural order is less than 0.");
        } else {
            return validationOk();
        }           
    }
}
