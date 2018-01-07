/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.UNKNOWN;
import static diarsid.beam.core.base.util.TextUtil.shrinkIfTooLong;

/**
 *
 * @author Diarsid
 */
public class Snippet {
    
    private final static Snippet UNKNOWN_SNIPPET;
    
    static {
        UNKNOWN_SNIPPET = new Snippet(UNKNOWN, "");
    }
    
    private final SnippetType type;
    private final String line;

    Snippet(SnippetType type, String snippetLine) {
        this.type = type;
        this.line = snippetLine;
    }
    
    static Snippet unknownSnippet() {
        return UNKNOWN_SNIPPET;
    }
    
    public SnippetType type() {
        return this.type;
    }
    
    public String line() {
        return this.line;
    }
    
    public String reinvokationText() {
        return this.type.reinvokationTextFormat().applyTo(this.line);
    }
    
    public String reinvokationTextWithLengthLimit(int lengthLimit) {
        return this.type.reinvokationTextFormat().applyTo(shrinkIfTooLong(this.line, lengthLimit));
    }
    
}
