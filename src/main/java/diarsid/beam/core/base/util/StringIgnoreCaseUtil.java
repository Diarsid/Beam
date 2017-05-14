/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.abs;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class StringIgnoreCaseUtil {
    
    private StringIgnoreCaseUtil() {
    }
    
    public static String replaceIgnoreCase(String target, String replaceable, String replacement) {
        if ( containsIgnoreCase(target, replaceable) ) {
            int indexOf = lower(target).indexOf(lower(replaceable));
            return target
                    .substring(0, indexOf)
                    .concat(replacement)
                    .concat(target
                            .substring(
                                    indexOf + replaceable.length(), 
                                    target.length()));
        } else {
            return target;
        }
    }
    
    public static String findAnyInIgnoreCase(
            String whereToSearch, Collection<String> searchedAnyOf) {
        if ( 
                nonNull(whereToSearch) && 
                nonNull(searchedAnyOf) && 
                ! whereToSearch.isEmpty() && 
                ! searchedAnyOf.isEmpty() ) {
            return searchedAnyOf
                    .stream()
                    .filter(any -> containsIgnoreCase(whereToSearch, any))
                    .findFirst()
                    .orElse("");
        } else {
            return "";
        }
    }
    
//    public static boolean containsAllPartsIgnoreCase(String whereToSearch, String searched) {
//        for (String searchedPart : splitByWildcard(searched)) {
//            if ( ! lower(whereToSearch).contains(lower(searchedPart)) ) {
//                return false;
//            }
//        }
//        return true;
//    }
    
    public static boolean containsAllPartsIgnoreCase(String whereToSearch, List<String> patterns) {
        for (String searchedPart : patterns) {
            if ( ! lower(whereToSearch).contains(lower(searchedPart)) ) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean containsIgnoreCase(String whereToSearch, String searched) {
        if ( isNull(searched) || isNull(whereToSearch) ) {
            return false;
        } else {
            return lower(whereToSearch).contains(lower(searched));
        }        
    }
    
    public static boolean containsWordInIgnoreCase(
            Collection<String> whereToSearch, String searched) {
        if ( isNull(whereToSearch) || whereToSearch.isEmpty() || isNull(searched) ) {
            return false;
        } else {
            return whereToSearch.stream()
                    .filter(s -> s.equalsIgnoreCase(searched))
                    .findFirst()
                    .isPresent();
        }        
    }
    
    public static boolean containsSnippetIgnoreCase(
            Collection<String> whereToSearch, String searched) {
        if ( isNull(whereToSearch) || whereToSearch.isEmpty() || isNull(searched) ) {
            return false;
        } else {
            return whereToSearch.stream()
                    .filter(s -> containsIgnoreCase(s, searched))
                    .findFirst()
                    .isPresent();
        }        
    }
    
    public static boolean containsIgnoreCaseAnyFragment(
            String whereToSearch, Collection<String> searchedSnippets) {
        return searchedSnippets
                .stream()
                .filter(snippet -> containsIgnoreCase(whereToSearch, snippet))
                .findFirst()
                .isPresent();
    }
    
    public static boolean containsAnySnippetsInAnyWordsIgnoreCase(
            Collection<String> whereToSearch, Collection<String> snippetsToSearch) {
        return whereToSearch
                .stream()
                .filter(stringToSearchIn -> 
                        containsIgnoreCaseAnyFragment(stringToSearchIn, snippetsToSearch))
                .findFirst()
                .isPresent();
    }
    
    public static int indexOfIgnoreCase(List<String> whereToSearch, String searched) {
        if ( isNull(whereToSearch) || whereToSearch.isEmpty() || isNull(searched) ) {
            return -1;
        } else {
            Optional<String> optionalElement =  whereToSearch.stream()
                    .filter(s -> s.equalsIgnoreCase(searched))
                    .findFirst();
            if ( optionalElement.isPresent() ) {
                return whereToSearch.indexOf(optionalElement.get());
            } else {
                return -1;
            }
        }
    }
    
    public static boolean containsKeyIgnoreCase(Map<String, Object> mapToSearch, String searched) {
        if ( isNull(mapToSearch) || mapToSearch.isEmpty() || isNull(searched) ) {
            return false;
        } else {
            return mapToSearch.keySet().stream()
                    .filter(s -> s.equalsIgnoreCase(searched))
                    .findFirst()
                    .isPresent();
        }        
    }
    
    public static <T> T getIgnoreCase(Map<String, T> mapToSearch, String keyIgnoreCase) {
        if ( isNull(mapToSearch) || mapToSearch.isEmpty() || isNull(keyIgnoreCase) ) {
            return null;
        } else {
            Optional<String> optionalKey = mapToSearch.keySet().stream()
                    .filter(s -> s.equalsIgnoreCase(keyIgnoreCase))
                    .findFirst();
            if ( optionalKey.isPresent() ) {
                return mapToSearch.get(optionalKey.get());
            } else {
                return null;
            }
        }    
    }
    
    public static boolean isSimilarIgnoreCase(String realTarget, String pattern) {
        realTarget = lower(realTarget);
        pattern = lower(pattern);
        int notFoundChars = 0;
        int realTargetCharIndex;
        int realTargetPreviousCharIndex = MIN_VALUE;
        int clustered = 0;
        for (int patternCharIndex = 0; patternCharIndex < pattern.length(); patternCharIndex++) {
            realTargetCharIndex = realTarget.indexOf(pattern.charAt(patternCharIndex));
            if ( realTargetCharIndex < 0 ) {
                realTargetPreviousCharIndex = MIN_VALUE;
                notFoundChars++;
            } else {
                if ( abs(realTargetCharIndex - realTargetPreviousCharIndex) == 1 || 
                        realTargetCharIndex == realTargetPreviousCharIndex ) {
                    if ( clustered == 0 ) {
                        clustered++;
                    } 
                    clustered++;
                }
                realTargetPreviousCharIndex = realTargetCharIndex;
            }
        }
        debug("[SIMILARITY] " + realTarget + "::" + pattern + " = clustered " + clustered);
        if ( notFoundChars == 0 ) {
            return clustered > 0;
        } else if ( notFoundChars == pattern.length() ) {
            return false;
        } else {
            return 
                    ( ( (notFoundChars * 1.0f) / (pattern.length() * 1.0f) ) <= 0.2f ) &&
                    ( clustered > 0 );
        }        
    }
}
