/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.EmptyCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.Recognizer;


public final class EmptyCommandProducer implements Recognizer {
    
    private final CommandType operationType;
    
    public EmptyCommandProducer(CommandType type) {
        this.operationType = type;
    }

    @Override
    public Command assess(Input input) {
        return new EmptyCommand(this.operationType);
    }
}
