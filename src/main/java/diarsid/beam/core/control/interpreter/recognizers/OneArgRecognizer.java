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


public class OneArgRecognizer extends NodeRecognizer {
    
    public OneArgRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasArgsQty(1) ) {
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }
    }
}
