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


public class DirectoryRecognizer extends NodeRecognizer {
    
    private static final List<String> DIRECTORY_KEYWORDS = unmodifiableList(asList(
            "dir", "direct", "directory"
    ));
    
    public DirectoryRecognizer() {
    }
    
    private boolean isArgDirectory(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                containsWordInIgnoreCase(DIRECTORY_KEYWORDS, input.currentArg());
    }

    @Override
    public Command assess(Input input) {
        if ( this.isArgDirectory(input) ) {
            return super.delegateRecognitionForward(input.toNextArg());
        } else {
            return undefinedCommand();
        }
    }
}
