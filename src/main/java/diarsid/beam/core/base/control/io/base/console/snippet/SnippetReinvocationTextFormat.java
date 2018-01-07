/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import static java.lang.String.format;

/**
 *
 * @author Diarsid
 */
interface SnippetReinvocationTextFormat {
    
    String applyTo(String snippetLine);
    
    static SnippetReinvocationTextFormat noFormat() {
        return (line) -> {
            return line;
        };
    }
    
    static SnippetReinvocationTextFormat reinvocationTextFormat(String format) {
        return (line) -> {
            return format(format, line);
        };
    }
}
