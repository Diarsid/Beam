/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.validation;

import java.util.Collection;

import diarsid.beam.core.domain.entities.WebPlace;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.ClassCastUtil.asString;
import static diarsid.beam.core.base.util.ClassCastUtil.asValidationResult;
import static diarsid.beam.core.base.util.ClassCastUtil.asWebPlace;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.TEXT_RULE;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.WEB_URL_RULE;
import static diarsid.beam.core.domain.entities.validation.Validities.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.Validities.validationOk;

/**
 *
 * @author Diarsid
 */
public class Validation {
    
    private Validation() {}
    
    public static Validity validateUsingRules(
            String value, Collection<ValidationRule> rules) {
        Validity result;
        for (ValidationRule rule : rules) {
            result = rule.applyTo(value);
            if ( result.isFail() ) {
                return result;
            }
        }
        return validationOk();
    }
    
    public static void validate(Object... objs) throws ValidationException {
        if ( objs.length > 0 ) {
            stream(objs)
                .filter(obj -> nonNull(obj))
                .forEach(obj -> discoverTypeAndValidate(obj));
        }        
    }
    
    private static void discoverTypeAndValidate(Object obj) {
        if ( obj instanceof Validity ) {
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
    
    public static Validity asName(String name) {
        return ENTITY_NAME_RULE.applyTo(name);
    }
    
    public static Validity asNames(String... names) {
        return stream(names)
                .filter(name -> nonNull(name))
                .map(name -> ENTITY_NAME_RULE.applyTo(name))
                .filter(validity -> validity.isFail())
                .findFirst()
                .orElse(validationOk());
    }
    
    public static Validity asUrl(String url) {
        return WEB_URL_RULE.applyTo(url);
    }
    
    public static Validity asOrder(int i) {
        if ( i < 0 ) {
            return validationFailsWith("Natural order is less than 0.");
        } else {
            return validationOk();
        }           
    }
}
