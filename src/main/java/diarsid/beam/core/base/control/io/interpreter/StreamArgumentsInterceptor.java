/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.control.io.commands.EditableTarget.argToTarget;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.wordIsAcceptable;
import static diarsid.beam.core.base.control.io.interpreter.StreamArgumentsInterceptor.ArgumentInterception.INTERCEPTED;
import static diarsid.beam.core.base.control.io.interpreter.StreamArgumentsInterceptor.ArgumentInterception.NOT_INTERCEPTED;
import static diarsid.beam.core.domain.entities.TimePeriod.isAppropriateAsTimePeriod;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableFilePath;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableRelativePath;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableWebPath;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
public class StreamArgumentsInterceptor {
    
    public static enum ArgumentType {
        
        SIMPLE_WORD {
            @Override
            boolean isAppropriateFor(String arg) {
                return 
                        wordIsAcceptable(arg) && 
                        ! isAcceptableWebPath(arg) && ! 
                        isAcceptableFilePath(arg) && 
                        isNull(parsePlace(arg)) &&
                        argToTarget(arg).isNotDefined();
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
        
        WEB_PLACEMENT {
            @Override
            boolean isAppropriateFor(String arg) {
                return nonNull(parsePlace(arg));
            }
            
            @Override
            String convertIfNecessary(String arg) {
                return parsePlace(arg).name();
            }
        },
        
        EDITABLE_TARGET {
            @Override
            boolean isAppropriateFor(String arg) {
                return argToTarget(arg).isDefined();
            }
            
            @Override
            String convertIfNecessary(String arg) {
                return argToTarget(arg).name();
            }
        };
        
        String convertIfNecessary(String arg) {
            return arg;
        }
        
        abstract boolean isAppropriateFor(String arg);
    }
    
    public static enum ArgumentInterception {
        NOT_INTERCEPTED,
        INTERCEPTED;
        
        public boolean ifContinue() {
            return this.equals(NOT_INTERCEPTED);
        }
    }
    
    private final Map<ArgumentType, String> argumentsByName;
    
    public StreamArgumentsInterceptor() {
        this.argumentsByName = new HashMap<>();
        stream(ArgumentType.values())
                .forEach(type -> this.argumentsByName.put(type, ""));    
    }
    
    public String of(ArgumentType type) {
        return this.argumentsByName.getOrDefault(type, "");
    }
    
    
    public ArgumentInterception interceptArgumentOfType(String arg, ArgumentType type) {
        if ( type.isAppropriateFor(arg) ) {
            this.argumentsByName.put(type, type.convertIfNecessary(arg));
            return INTERCEPTED;
        } else {
            return NOT_INTERCEPTED;
        }
    }
}
