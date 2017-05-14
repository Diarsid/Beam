/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.PrioritizedRecognizer;


public class ArgumentsRecognizer extends PrioritizedRecognizer {
    
    private final CommandType commandType;
    
    ArgumentsRecognizer(CommandType commandType) {
        this.commandType = commandType;
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            return new ArgumentsCommand(this.commandType, input.allRemainingArgs());
        } else {
            return new ArgumentsCommand(this.commandType);
        }
    }
}
