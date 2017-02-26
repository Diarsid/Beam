/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.inputparsing.common;

import java.util.Objects;

import diarsid.beam.core.base.control.io.interpreter.ControlKeys;
import diarsid.beam.core.base.util.PathUtils;
import diarsid.beam.core.base.util.StringNumberUtils;
import diarsid.beam.core.domain.entities.TimePeriod;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;

/**
 *
 * @author Diarsid
 */
public enum ArgumentType {
    SIMPLE_WORD {
        @Override
        boolean isAppropriateFor(String arg) {
            return ControlKeys.wordIsAcceptable(arg) && !PathUtils.isAcceptableWebPath(arg) && !PathUtils.isAcceptableFilePath(arg) && Objects.isNull(WebPlace.parsePlace(arg)) && EntityProperty.argToProperty(arg).isUndefined();
        }
    },
    NUMBER {
        @Override
        boolean isAppropriateFor(String arg) {
            return StringNumberUtils.isNumeric(arg);
        }
    },
    TIME_PERIOD {
        @Override
        boolean isAppropriateFor(String arg) {
            return TimePeriod.isAppropriateAsTimePeriod(arg);
        }
    },
    FILE_PATH {
        @Override
        boolean isAppropriateFor(String arg) {
            return PathUtils.isAcceptableFilePath(arg);
        }
    },
    WEB_PATH {
        @Override
        boolean isAppropriateFor(String arg) {
            return PathUtils.isAcceptableWebPath(arg);
        }
    },
    RELATIVE_PATH {
        @Override
        boolean isAppropriateFor(String arg) {
            return PathUtils.isAcceptableRelativePath(arg);
        }
    },
    WEB_PLACE {
        @Override
        boolean isAppropriateFor(String arg) {
            return Objects.nonNull(WebPlace.parsePlace(arg));
        }

        @Override
        String convertIfNecessary(String arg) {
            return WebPlace.parsePlace(arg).name();
        }
    },
    ENTITY_PROPERTY {
        @Override
        boolean isAppropriateFor(String arg) {
            return EntityProperty.argToProperty(arg).isDefined();
        }

        @Override
        String convertIfNecessary(String arg) {
            return EntityProperty.argToProperty(arg).name();
        }
    };

    String convertIfNecessary(String arg) {
        return arg;
    }

    abstract boolean isAppropriateFor(String arg);
    
}
