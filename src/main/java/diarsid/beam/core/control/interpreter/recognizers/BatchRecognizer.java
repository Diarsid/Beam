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

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;


public class BatchRecognizer extends NodeRecognizer {
    
    private final List<String> possibleBatchPatterns;
    
    public BatchRecognizer() {
        this.possibleBatchPatterns = asList("bat", "batch", "exe");
    }

    @Override
    public Command assess(Input input) {
        if ( this.argToRecognizeIsCall(input) ) {
            input.toNextArg();
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }
    }

    private boolean argToRecognizeIsCall(Input input) {
        return input.hasNotRecognizedArgs() && 
                containsWordInIgnoreCase(this.possibleBatchPatterns, input.currentArg());
    }
    
}
