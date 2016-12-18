/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.control.interpreter.ControlKeys.wordIsAcceptable;
import static diarsid.beam.core.util.PathUtils.containsPathSeparator;


public class SimpleWordRecognizer extends NodeRecognizer {
    
    public SimpleWordRecognizer() {
    }
    
    private boolean argIsSimpleAcceptableWord(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                ! containsPathSeparator(input.currentArg()) && 
                wordIsAcceptable(input.currentArg());
    }

    @Override
    public Command assess(Input input) {
        if ( this.argIsSimpleAcceptableWord(input) ) {
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }
    }
}
