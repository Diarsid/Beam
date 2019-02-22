/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import java.util.List;

import static java.lang.Character.isDigit;

import static org.eclipse.jetty.util.StringUtil.startsWithIgnoreCase;

import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.startsIngoreCase;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.startsWithIgnoreCaseAnyFragment;
import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
interface SnippetMatching {
    
    boolean matches(String line);
    
    static SnippetMatching matchesByNotContaining(String part) {
        return (line) -> {
            return ! containsIgnoreCase(line, part);
        };
    }
    
    static SnippetMatching matchesByNotContainingAny(String... parts) {
        return (line) -> {
            return ! containsIgnoreCaseAnyFragment(line, parts);
        };
    }
    
    static SnippetMatching matchesByNotContainingAny(List<String> parts) {
        return (line) -> {
            return ! containsIgnoreCaseAnyFragment(line, parts);
        };
    }
    
    static SnippetMatching noMatching() {
        return (line) -> false;
    }
    
    static SnippetMatching matchesByStartingWith(String start) {
        return (line) -> {
            return startsIngoreCase(line.trim(), start);
        };
    }
    
    static SnippetMatching matchesByStartingWithAny(List<String> starts) {
        return (line) -> {
            return startsWithIgnoreCaseAnyFragment(line.trim(), starts);
        };
    }
    
    static SnippetMatching matchesByNotStartingWith(String start) {
        return (line) -> {
            return ! startsWithIgnoreCase(line.trim(), start);
        };
    }
    
    static SnippetMatching matchesByNotStartingWithAny(String... starts) {
        return (line) -> {
            return ! startsWithIgnoreCaseAnyFragment(line.trim(), starts);
        };
    }
    
    static SnippetMatching matchesByNotEndingWith(String end) {
        return (line) -> {
            return ! lower(line.trim()).endsWith(lower(end));
        };
    }
    
    static SnippetMatching matchesByContaining(String part) {
        return (line) -> {
            return lower(line).trim().contains(lower(part));
        };
    }
    
    static SnippetMatching matchesByStartingWithDigitAndContains(String part) {
        return (line) -> {
            String lowerLine = lower(line).trim();
            return 
                    isDigit(lowerLine.charAt(0)) &&
                    lowerLine.contains(part);
        };
    }
    
    static SnippetMatching matchesByNotStartingWithDigit() {
        return (line) -> {
            return ! isDigit(line.trim().charAt(0));
        };
    }
    
    static SnippetMatching matchesByStartingContainingEndingWith(
            String start, String part, String end) {
        return (line) -> {
            String lowerLine = lower(line).trim();
            String lowerStart = lower(start); // no trim here!
            String lowerPart = lower(part); // no trim here!
            String lowetEnd = lower(end); // no trim here!
            
            return 
                    lowerLine.startsWith(lowerStart) && 
                    lowerLine.contains(lowerPart) &&
                    lowerLine.endsWith(lowetEnd);
        };
    }
    
    static SnippetMatching matchesByStartingEndingWith(String start, String end) {
        return (line) -> {
            String lowerLine = lower(line).trim();
            String lowerStart = lower(start); // no trim here!
            String lowetEnd = lower(end); // no trim here!
            
            return 
                    lowerLine.startsWith(lowerStart) && 
                    lowerLine.endsWith(lowetEnd);
        };
    }
}
