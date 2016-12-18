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
import static diarsid.beam.core.util.StringUtils.lower;


public class WordRecognizer extends NodeRecognizer {
    
    private final String controlWord;
    
    public WordRecognizer(String controlWord) {
        this.controlWord = lower(controlWord);
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() && lower(input.currentArg()).equals(this.controlWord) ) {
            return super.delegateRecognitionForward(input.toNextArg());
        } else {
            return undefinedCommand();
        }
    }
}
