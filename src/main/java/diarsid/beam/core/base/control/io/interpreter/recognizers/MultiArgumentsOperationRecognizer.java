/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.PrioritizedRecognizer;


public class MultiArgumentsOperationRecognizer extends PrioritizedRecognizer {
    
    private final CommandType commandType;
    
    public MultiArgumentsOperationRecognizer(CommandType commandType) {
        this.commandType = commandType;
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            return new MultiStringCommand(this.commandType, input.allRemainingArgs());
        } else {
            return new MultiStringCommand(this.commandType);
        }
    }
}
