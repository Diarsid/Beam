/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.List;

import static diarsid.beam.core.modules.executor.ProxyMethodExecutionMode.INTERCEPT_AND_PROCEED;
import static diarsid.beam.core.modules.executor.ProxyMethodExecutionMode.JUST_PROCEED;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleProxyArgumentsAnalizer {
    
    ExecutorModuleProxyArgumentsAnalizer() {
    }    
    
    ProxyMethodExecutionMode defineExecutionMode(Object[] args) {
        if ( args.length == 1 ) {
            if ( this.isListOfStrings(args[0]) ) {
                this.preprocessListOfString(args, 0);
                return INTERCEPT_AND_PROCEED;
            } else {
                if ( this.isString(args[0]) ) {
                    args[0] = this.preprocessString((String) args[0]);
                    return JUST_PROCEED;
                } else {
                    return JUST_PROCEED;
                }                    
            }
        } else {
            this.preprocessStringsInArgsIfAny(args);
            return JUST_PROCEED;
        }
    }
    
    private void preprocessListOfString(Object[] args, int argN) {
        List<String> strings = (List<String>) args[argN];
        for (int i = 0; i < strings.size(); i++) {
            if ( ! this.isStringAcceptable(strings.get(i))) {
                args[argN] = new ArrayList<String>();
                break;
            }
        }
    }
    
    private boolean isString(Object arg) {
        return ( arg instanceof String);
    }
        
    private void preprocessStringsInArgsIfAny(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if ( args[i] instanceof String ) {
                args[i] = this.preprocessString( (String) args[i]);
            } else if ( this.isListOfStrings(args[i]) ) {
                this.preprocessListOfString(args, i);
            }
        }
    }
    
    private boolean isStringAcceptable(String s) {
        s = s.trim();
        return 
                this.doesNotHaveUnderscoreAbuse(s) && 
                this.doesNotHaveDashAbuse(s) && 
                this.doesNotHaveSpecialProhibitedChars(s);
    }
    
    private boolean doesNotHaveSpecialProhibitedChars(String s) {
        return ! ( s.contains("$") || s.contains("#") || s.contains("%") );
    }
    
    private boolean doesNotHaveDashAbuse(String s) {
        return ! ( s.startsWith("-") || s.endsWith("-") );
    }

    private boolean doesNotHaveUnderscoreAbuse(String s) {
        return ! ( s.startsWith("_") || s.endsWith("_") );
    }
    
    private String preprocessString(String s) {
        if ( this.isStringAcceptable(s) ) {
            return s;
        } else {
            return "";
        }
    } 
    
    
    private boolean isListOfStrings(Object argument) {
        if ( argument instanceof List<?> ) {
            return this.ifListContainsStrings( (List<Object>) argument );
        } else {
            return false;
        }
    }
    
    private boolean ifListContainsStrings(List<Object> list) {
        // if this argument is real command, passed in from an external
        // ExecutorModule user, List<String> SHOULD have elements in it. 
        // If otherwise, this means that external usage of ExecutorModule
        // is invalid and actual ExecutorModule method should not be 
        // invoked at all as such invocation makes no sense.
        if ( list.isEmpty() ) {
            return false;
        } else {
            return ( (list.get(0)) instanceof String );
        }
    }

}
