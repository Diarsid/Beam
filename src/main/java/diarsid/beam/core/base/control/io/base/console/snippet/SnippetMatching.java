/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import static java.lang.Character.isDigit;

import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
interface SnippetMatching {
    
    boolean matches(String line);
    
    default SnippetMatching andNotContaining(String part) {
        return (line) -> {
            return ! containsIgnoreCase(line, part) && this.matches(line);
        };
    }
    
    default SnippetMatching andNotContaining(String... parts) {
        return (line) -> {
            return ! containsIgnoreCaseAnyFragment(line, parts) && this.matches(line);
        };
    }
    
    static SnippetMatching noMatching() {
        return (line) -> false;
    }
    
    static SnippetMatching matchesByStartingWith(String start) {
        return (line) -> {
            return lower(line).trim().startsWith(lower(start));
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
