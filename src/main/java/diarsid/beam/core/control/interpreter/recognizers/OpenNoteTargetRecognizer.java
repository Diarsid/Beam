/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.SingleStringCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static diarsid.beam.core.control.commands.CommandType.OPEN_TARGET_IN_NOTE;
import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;


public class OpenNoteTargetRecognizer extends NodeRecognizer {
    
    public OpenNoteTargetRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            return new SingleStringCommand(input.currentArg(), OPEN_TARGET_IN_NOTE);
        } else {
            return undefinedCommand();
        }
    }
}
