/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import static diarsid.beam.core.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsAnySnippetsInAnyWordsIgnoreCase;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;

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
    
    public static final List<String> UNACCEPTABLE_ARGUMENT_CHARS = unmodifiableList(asList(
            "`", "?", "$", "@", "!", 
            "#", "%", "^", "{", "}", 
            "*", ";", ":", "+", "=",
            "\"", "~", "<"
    ));
    
    private ControlKeys() {
    }
    
    public static boolean wordIsAcceptable(String word) {
        return 
                ! word.isEmpty() && 
                ! containsIgnoreCaseAnyFragment(word, UNACCEPTABLE_ARGUMENT_CHARS);
    }
    
    public static boolean wordIsAcceptableAndSimple(String word) {
        return 
                ! word.isEmpty() && 
                ! containsPathSeparator(word) && 
                ! containsIgnoreCaseAnyFragment(word, UNACCEPTABLE_ARGUMENT_CHARS);
    }
    
    public static boolean wordsAreAcceptable(String... words) {
        return ( ! containsAnySnippetsInAnyWordsIgnoreCase(
                        asList(words), UNACCEPTABLE_ARGUMENT_CHARS));
    }
    
    public static boolean wordsAreAcceptable(Collection<String> words) {
        return ( ! containsAnySnippetsInAnyWordsIgnoreCase(words, UNACCEPTABLE_ARGUMENT_CHARS));
    }
}
