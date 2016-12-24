/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.interpreter.recognizers;

import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.interpreter.Input;
import diarsid.beam.core.control.io.interpreter.NodeRecognizer;

import static diarsid.beam.core.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.util.StringUtils.lower;


public class WordRecognizer extends NodeRecognizer {
    
    private final String controlWord;
    
    public WordRecognizer(String controlWord) {
        this.controlWord = lower(controlWord).trim();
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