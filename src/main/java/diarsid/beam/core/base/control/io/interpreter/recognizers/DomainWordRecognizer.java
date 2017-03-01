/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.NodeRecognizer;

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.domainWordIsAcceptable;


public class DomainWordRecognizer extends NodeRecognizer {
    
    public DomainWordRecognizer() {
    }
    
    private boolean argIsSimpleAcceptableWord(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                domainWordIsAcceptable(input.currentArg());
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
