/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import java.util.List;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;


public class PageRecognizer extends NodeRecognizer {
    
    private static final List<String> WEB_PAGES_WORDS = unmodifiableList(asList(
            "page", "webpage", "webp", "web"
    ));
    
    public PageRecognizer() {
    }
    
    private boolean argToRecognizeIsPage(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                containsWordInIgnoreCase(WEB_PAGES_WORDS, input.currentArg());
    }

    @Override
    public Command assess(Input input) {
        if ( this.argToRecognizeIsPage(input) ) {
            input.toNextArg();
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }
    }
}
