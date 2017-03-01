/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.inputparsing.common;

import diarsid.beam.core.domain.entities.WebPlace;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.domainWordIsAcceptable;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsAcceptable;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableFilePath;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableRelativePath;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableWebPath;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.domain.entities.TimePeriod.isAppropriateAsTimePeriod;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;

/**
 *
 * @author Diarsid
 */
public enum ArgumentType {
    TEXT {
        @Override
        boolean isAppropriateFor(String arg) {
            return 
                    textIsAcceptable(arg) && 
                    ! isAcceptableWebPath(arg) && 
                    ! isAcceptableFilePath(arg) && 
                    WebPlace.parsePlace(arg).isUndefined() && 
                    argToProperty(arg).isUndefined();
        }
    },
    DOMAIN_WORD {
        @Override
        boolean isAppropriateFor(String arg) {
            return 
                    domainWordIsAcceptable(arg) && 
                    ! isAcceptableWebPath(arg) && 
                    ! isAcceptableFilePath(arg) && 
                    WebPlace.parsePlace(arg).isUndefined() && 
                    argToProperty(arg).isUndefined();
        }
    },
    NUMBER {
        @Override
        boolean isAppropriateFor(String arg) {
            return isNumeric(arg);
        }
    },
    TIME_PERIOD {
        @Override
        boolean isAppropriateFor(String arg) {
            return isAppropriateAsTimePeriod(arg);
        }
    },
    FILE_PATH {
        @Override
        boolean isAppropriateFor(String arg) {
            return isAcceptableFilePath(arg);
        }
    },
    WEB_PATH {
        @Override
        boolean isAppropriateFor(String arg) {
            return isAcceptableWebPath(arg);
        }
    },
    RELATIVE_PATH {
        @Override
        boolean isAppropriateFor(String arg) {
            return isAcceptableRelativePath(arg);
        }
    },
    WEB_PLACE {
        @Override
        boolean isAppropriateFor(String arg) {
            return nonNull(WebPlace.parsePlace(arg));
        }

        @Override
        String convertIfNecessary(String arg) {
            return parsePlace(arg).name();
        }
    },
    ENTITY_PROPERTY {
        @Override
        boolean isAppropriateFor(String arg) {
            return argToProperty(arg).isDefined();
        }

        @Override
        String convertIfNecessary(String arg) {
            return argToProperty(arg).name();
        }
    };

    String convertIfNecessary(String arg) {
        return arg;
    }

    abstract boolean isAppropriateFor(String arg);
    
}
