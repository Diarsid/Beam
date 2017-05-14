/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.findAnyInIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class ControlKeys {
    
    public static final String DOT = ".";
    public static final String QUESTION = "?";
    public static final String CREATE = "+";
    public static final String REMOVE = "-";
    public static final String WILDCARD = "-";
    
    public static final List<String> UNACCEPTABLE_DOMAIN_CHARS = unmodifiableList(asList(
            "`", "?", "$", "@", "!", 
            "#", "%", "^", "{", "}", 
            "*", ";", ":", "+", "=",
            "~", "<"
    ));
    public static final List<String> UNACCEPTABLE_TEXT_CHARS = unmodifiableList(asList(
            "`", "$", "@", "^", "{", 
            "}", "*", ";", "\"", "~", 
            "<"
    ));
    
    private ControlKeys() {
    }
    
//    public static boolean hasWildcard(String word) {
//        return word.contains(WILDCARD);
//    }
    
    public static String findUnacceptableInDomainWord(String word) {
        return findAnyInIgnoreCase(word, UNACCEPTABLE_DOMAIN_CHARS);
    }
    
    public static String findUnacceptableInText(String text) {
        return findAnyInIgnoreCase(text, UNACCEPTABLE_TEXT_CHARS);
    }
    
    public static boolean textIsAcceptable(String text) {
        return 
                ! text.isEmpty() && 
                ! containsIgnoreCaseAnyFragment(text, UNACCEPTABLE_TEXT_CHARS);
    }
    
    public static boolean textIsNotAcceptable(String text) {
        return 
                text.isEmpty() || 
                containsIgnoreCaseAnyFragment(text, UNACCEPTABLE_TEXT_CHARS);
    }
    
    public static boolean domainWordIsAcceptable(String word) {
        return 
                ! word.isEmpty() && 
                ! containsPathSeparator(word) &&
                ! containsIgnoreCaseAnyFragment(word, UNACCEPTABLE_DOMAIN_CHARS);
    }
    
    public static boolean domainWordIsNotAcceptable(String word) {
        return 
                word.isEmpty() ||  
                containsPathSeparator(word) ||
                containsIgnoreCaseAnyFragment(word, UNACCEPTABLE_DOMAIN_CHARS);
    }
    
    public static boolean charsAreDomainAcceptable(String target) {
        return ! containsIgnoreCaseAnyFragment(target, UNACCEPTABLE_DOMAIN_CHARS);
    }
}
