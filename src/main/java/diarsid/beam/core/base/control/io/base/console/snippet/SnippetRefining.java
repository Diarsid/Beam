/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import static diarsid.beam.core.base.util.StringNumberUtils.removeLeadingDigitsFrom;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
interface SnippetRefining {
    
    String applyTo(String line);
    
    default SnippetRefining and(SnippetRefining other) {
        return (line) -> {
            return other.applyTo(this.applyTo(line));
        };
    }
    
    static SnippetRefining refiningByRemoveStart(String start) {
        return (line) -> {
            return line.trim().substring(start.length());
        };
    }
    
    static SnippetRefining refiningByRemoveAnyStarts(String... starts) {
        return (line) -> {
            line = line.trim();
            for (String start : starts) {
                if ( line.startsWith(start) ) {
                    line = line.substring(start.length());
                    break;
                }
            }
            return line;
        };
    }
    
    static SnippetRefining refiningByTrim() {
        return (line) -> {
            return line.trim();
        };
    }
    
    static SnippetRefining refiningByRemoveStartAndEndIfPresent(String start, String end) {
        return (line) -> {
            String result = line.trim().substring(start.length());
            if ( result.endsWith(end) ) {
                result = result.substring(0, result.length() - end.length());
            }
            return result;
        };
    }
    
    static SnippetRefining refiningByRemoveAllBefore(String part) {
        return (line) -> {
            String lineTrim = line.trim();
            return lineTrim.substring(
                    lower(lineTrim).indexOf(lower(part)) + part.length());
        };
    }
    
    static SnippetRefining refiningByRemoveAllBeforeAndAfter(String partBefore, String partAfter) {
        return (line) -> {
            String lineTrim = line.trim();
            String lowerLine = lower(lineTrim);
            
            return lineTrim.substring(
                    lowerLine.indexOf(lower(partBefore)) + partBefore.length(),
                    lowerLine.indexOf(lower(partAfter)))
                    .trim();
        };
    }
    
    static SnippetRefining refiningByRemoveStartingDigitsAnd(String part) {
        return (line) -> {            
            return removeLeadingDigitsFrom(line.trim()).substring(part.length());
        };
    }
    
    static SnippetRefining noRefining() {
        return (line) -> {
            return "";
        };
    }
}
