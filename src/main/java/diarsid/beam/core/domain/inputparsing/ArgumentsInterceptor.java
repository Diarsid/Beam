/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;

import static diarsid.beam.core.domain.inputparsing.ArgumentInterception.INTERCEPTED;
import static diarsid.beam.core.domain.inputparsing.ArgumentInterception.NOT_INTERCEPTED;

/**
 *
 * @author Diarsid
 */
public class ArgumentsInterceptor {
    
    private final Map<ArgumentType, String> argumentsByName;
    
    public ArgumentsInterceptor() {
        this.argumentsByName = new HashMap<>();
        stream(ArgumentType.values())
                .forEach(type -> this.argumentsByName.put(type, ""));    
    }
    
    public String argOfType(ArgumentType type) {
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
