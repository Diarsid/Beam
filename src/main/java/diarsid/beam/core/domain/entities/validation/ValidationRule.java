/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.validation;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_DOMAIN_CHARS;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_TEXT_CHARS;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.domainWordIsNotAcceptable;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.findUnacceptableInDomainWord;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.findUnacceptableInText;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsAcceptable;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsNotAcceptable;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableWebPath;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationOk;

/**
 *
 * @author Diarsid
 */
public enum ValidationRule {
    
    TEXT_RULE {
        @Override
        public List<String> helpInfo() {
            return asList(
                    "Print plain text.",
                    "It can contain any characters except following chars:",
                    UNACCEPTABLE_TEXT_CHARS.stream().collect(joining()),
                    "Use dot or empty line to break.");
        }
        
        @Override
        public ValidationResult applyTo(String target) {
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
        public List<String> helpInfo() {
            return asList(
                    "Print URL.",
                    "It should be a valid URL.");
        }
        
        @Override
        public ValidationResult applyTo(String target) {
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
        public List<String> helpInfo() {
            return asList(
                    "Print URL path pointing to a local directory.",
                    "Directory must exist.");
        }
        
        @Override
        public ValidationResult applyTo(String target) {
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
        public List<String> helpInfo() {
            return asList(
                    "Print slash-separated path.",
                    "It can be any relative path, not neccesary",
                    "pointing to real file or folder. Path must not",
                    "contain following chars: " + 
                            UNACCEPTABLE_TEXT_CHARS.stream().collect(joining()));
        }
        
        @Override 
        public ValidationResult applyTo(String target) {
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
        public List<String> helpInfo() {
            return asList(
                    "Print name.",
                    "It must not contain any path separators or following",
                    "chars: " + UNACCEPTABLE_DOMAIN_CHARS.stream().collect(joining()));
        }
        
        @Override
        public ValidationResult applyTo(String target) {
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
    
    public abstract ValidationResult applyTo(String target);
    
    public abstract List<String> helpInfo();
    
    public static ValidationResult applyValidatationRule(String target, ValidationRule rule) {
        return rule.applyTo(target);
    }
    
}
