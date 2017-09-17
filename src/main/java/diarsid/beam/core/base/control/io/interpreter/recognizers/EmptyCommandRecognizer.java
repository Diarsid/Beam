/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.NodeRecognizer;

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.lowerThan;

/**
 *
 * @author Diarsid
 */
public class EmptyCommandRecognizer extends NodeRecognizer {
    
    private final CommandType commandType;
    
    EmptyCommandRecognizer(CommandType commandType) {
        this.commandType = commandType;
        super.priority(lowerThan(LOWEST));
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            return undefinedCommand();
        } else {
            return new EmptyCommand(this.commandType);
        }        
    }    
}
