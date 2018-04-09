/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.LISTED_COMMAND;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.LISTED_WEBPAGE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.UNKNOWN;

public class ConsoleSnippetFinderTest {
    
    private static final String CARET = "^";
    private static final ConsoleSnippetFinder FINDER = new ConsoleSnippetFinder();    
    
    private Snippet findSnippetFrom(String textWithCaret) {
        int caretPosition = textWithCaret.indexOf(CARET);
        String text = textWithCaret.replace(CARET, "");
        
        Snippet snippet = FINDER
                .in(text)
                .goToLineAt(caretPosition)
                .defineLineSnippetType()
                .composeSnippet()
                .getSnippetAndReset();
        
        return snippet;
    }
    
    @Test
    public void test_LISTED_WEBPAGE() {
        
        Snippet snippet = findSnippetFrom(
                "Beam > ? dir common\n" +
                "     > 'Common' found.\n" +
                "     > with pages ?\n" +
                "     > yes/no : y\n" +
                "       WebPanel > Common (order: 5)\n" +
                "          0) Wertex \n" +
                "          1) qwwew \n" +
                "          2) ffff^ggg \n" +
                "          3) eeeerrrr \n" +
                "          4) Google \n" +
                "          5) myname \n" +
                "Beam > ");
        
        assertThat(snippet.type(), equalTo(LISTED_WEBPAGE));
    }
    
    @Test
    public void test_LISTED_COMMAND() {
                
        Snippet snippet = findSnippetFrom(
                "Beam > ? common\n" +
                "       WebDirectories:\n" +
                "          Common (webpanel)\n" +
                "       Commands:\n" +
                "          Books/Common -> open Books/Common\n" +
                "          Books/Common/Salv^atore_R -> open Books/Common/Salvatore_R\n" +
                "          Books/Common/Tolkien_J.R.R -> open Books/Common/Tolkien_J.R.R\n");
        
        assertThat(snippet.type(), equalTo(LISTED_COMMAND));
    }
    
    @Test
    public void test_UNKNOWN() {
                
        Snippet snippet = findSnippetFrom(
                "Be^am > ");
        
        assertThat(snippet.type(), equalTo(UNKNOWN));
    }
}
