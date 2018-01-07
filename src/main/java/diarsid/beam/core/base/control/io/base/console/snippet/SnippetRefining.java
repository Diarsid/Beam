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
    
    String refineFromLine(String line);
    
    static SnippetRefining refiningByRemoveStart(String start) {
        return (line) -> {
            return line.trim().substring(start.length());
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
    
    static SnippetRefining refiningBeRemoveAllBefore(String part) {
        return (line) -> {
            String lineTrim = line.trim();
            return lineTrim.substring(
                    lower(lineTrim).indexOf(lower(part)) + part.length());
        };
    }
    
    static SnippetRefining refiningBeRemoveAllBeforeAndAfter(String partBefore, String partAfter) {
        return (line) -> {
            String lineTrim = line.trim();
            String lowerLine = lower(lineTrim);
            
            return lineTrim.substring(
                    lowerLine.indexOf(lower(partBefore)) + partBefore.length(),
                    lowerLine.indexOf(lower(partAfter)))
                    .trim();
        };
    }
    
    static SnippetRefining refiningBeRemoveStartingDigitsAndColon() {
        return (line) -> {            
            return removeLeadingDigitsFrom(line.trim()).substring(" : ".length());
        };
    }
    
    static SnippetRefining noRefining() {
        return (line) -> {
            return "";
        };
    }
}
