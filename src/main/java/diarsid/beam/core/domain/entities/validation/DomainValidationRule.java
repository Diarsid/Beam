/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.validation;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.domainWordIsNotAcceptable;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.findUnacceptableInDomainWord;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.findUnacceptableInText;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsAcceptable;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsNotAcceptable;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableWebPath;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.domain.entities.validation.Validities.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.Validities.validationOk;

/**
 *
 * @author Diarsid
 */
public enum DomainValidationRule implements ValidationRule {
    
    TEXT_RULE {        
        
        @Override
        public Validity applyTo(String target) {
            if ( target.isEmpty() ) {
                return validationFailsWith("cannot be empty.");
            }
            if ( textIsAcceptable(target) ) {
                return validationOk();
            } else {
                return validationFailsWith(format(
                        "symbol '%s' is not allowed in text.", 
                        findUnacceptableInText(target)));
            }
        }
    },
    
    WEB_URL_RULE {
        
        @Override
        public Validity applyTo(String target) {
            if ( target.isEmpty() ) {
                return validationFailsWith("cannot be empty.");
            }
            if ( isAcceptableWebPath(target) ) {
                return validationOk();
            } else {
                return validationFailsWith("not a valid URL.");
            }
        }
    },
    
    LOCAL_DIRECTORY_PATH_RULE {
        
        @Override
        public Validity applyTo(String target) {
            if ( target.isEmpty() ) {
                return validationFailsWith("cannot be empty.");
            }
            if ( ! pathIsDirectory(target) ) {
                return validationFailsWith("this path is not a directory.");
            }
            return validationOk();
        }
    },
    
    SIMPLE_PATH_RULE {
        
        @Override 
        public Validity applyTo(String target) {
            if ( target.isEmpty() ) {
                return validationFailsWith("cannot be empty.");
            } 
            if ( textIsNotAcceptable(target) ) {
                return validationFailsWith(format(
                        "symbol '%s' is not allowed in entity name.", 
                        findUnacceptableInDomainWord(target)));
            }
            return validationOk();
        }
    },
    
    ENTITY_NAME_RULE {
        
        @Override
        public Validity applyTo(String target) {
            if ( target.isEmpty() ) {
                return validationFailsWith("cannot be empty.");
            }
            if ( containsPathSeparator(target) ) {
                return validationFailsWith("separators are not allowed here.");
            }
            if ( domainWordIsNotAcceptable(target) ) {
                return validationFailsWith(format(
                        "symbol '%s' is not allowed in entity name.", 
                        findUnacceptableInDomainWord(target)));
            }
            return validationOk();
        }
    };
    
}
