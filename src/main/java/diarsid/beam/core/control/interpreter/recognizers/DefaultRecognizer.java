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


public class DefaultRecognizer extends NodeRecognizer {
    
    public DefaultRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        System.out.println("DEFAULT");
        if ( this.argIsSingleAcceptableWord(input) ) {
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }
    }

    private boolean argIsSingleAcceptableWord(Input input) {
        return 
                input.hasNotRecognizedArgs() &&  
                input.hasArgsQty(1) &&
                wordIsAcceptable(input.currentArg());
    }
}
