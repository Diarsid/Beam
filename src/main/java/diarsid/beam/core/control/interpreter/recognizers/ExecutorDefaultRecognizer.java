/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.control.interpreter.ControlKeys.wordIsAcceptable;


public class ExecutorDefaultRecognizer extends NodeRecognizer {
    
    public ExecutorDefaultRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() && wordIsAcceptable(input.currentArg()) ) {
            return new ExecutorDefaultCommand(input.currentArg());
        } else {
            return undefinedCommand();
        }
    }
}
