/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.validation;

import static diarsid.beam.core.control.io.interpreter.ControlKeys.findUnacceptableIn;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.wordIsNotAcceptable;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.wordIsNotSimple;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationOk;
import static diarsid.beam.core.util.PathUtils.isAcceptableWebPath;
import static diarsid.beam.core.util.PathUtils.pathIsDirectory;

/**
 *
 * @author Diarsid
 */
public enum ValidationRule {
    
    WEB_URL {
        @Override
        ValidationResult apply(String target) {
            if ( isAcceptableWebPath(target) ) {
                return validationOk();
            } else {
                return validationFailsWith("not a valid URL.");
            }
        }
    },
    
    LOCAL_DIRECTORY_PATH {
        @Override
        ValidationResult apply(String target) {
            if ( ! pathIsDirectory(target) ) {
                return validationFailsWith("this path is not a directory.");
            }
            return validationOk();
        }
    },
    
    ENTITY_NAME {
        @Override
        ValidationResult apply(String target) {
            if ( wordIsNotAcceptable(target) ) {
                return validationFailsWith(
                        "symbol '" + findUnacceptableIn(target) + "' is not allowed in entity name.");
            }
            if ( wordIsNotSimple(target) ) {
                return validationFailsWith(target + " is not a plain word or a group of words.");
            }
            return validationOk();
        }
    };
    
    abstract ValidationResult apply(String target);
    
    public static ValidationResult applyValidatationRule(String target, ValidationRule rule) {
        return rule.apply(target);
    }
    
}
