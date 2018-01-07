/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.PrioritizedRecognizer;

import static diarsid.beam.core.base.control.io.commands.Commands.createExecutorOrUndefinedCommandFrom;
import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;

/**
 *
 * @author Diarsid
 */
public class ExecutorRecognizer extends PrioritizedRecognizer {
    
    private final CommandType commandType;
    private final String additionToCommand;
    
    ExecutorRecognizer(CommandType commandType) {
        this.commandType = commandType;
        this.additionToCommand = "";
    }
    
    ExecutorRecognizer(CommandType commandType, String addition) {
        this.commandType = commandType;
        this.additionToCommand = addition;
    }

    @Override
    public Command assess(Input input) {
        if ( input.currentArgIsMeaningfull() ) {
            // WARN!!! unsafe behaviour changing: currentArg() -> allRemainingArgsString()
//            return createExecutorOrUndefinedCommandFrom(
//                    this.commandType, input.currentArg() + this.additionToCommand);
            return createExecutorOrUndefinedCommandFrom(
                    this.commandType, input.allRemainingArgsString() + this.additionToCommand);
        } else {
            return undefinedCommand();
        }
    }
}
