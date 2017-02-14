package diarsid.beam.core.base.control.io.interpreter.recognizers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.PrioritizedRecognizer;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.wordIsAcceptable;


public class RemoveEntityByArgRecognizer extends PrioritizedRecognizer {
    
    public static enum ArgumentsMode {
        JOIN_ALL_REMAINING_ARGS,
        USE_FIRST_REMAINING_ARG
    }
    
    private final CommandType type;    
    private final ArgumentsMode argumentsMode;
    
    public RemoveEntityByArgRecognizer(CommandType type, ArgumentsMode argumentsMode) {
        this.type = type;
        this.argumentsMode = argumentsMode;
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            if ( input.remainingArgsQty() > 1 ) {
                switch ( this.argumentsMode ) {
                    case JOIN_ALL_REMAINING_ARGS : {
                        return this.commandWithOrWithoutArgument(input.allRemainingArgsString());
                    }
                    case USE_FIRST_REMAINING_ARG : {
                        return this.commandWithOrWithoutArgument(input.currentArg());
                    }
                    default : {
                        return new RemoveEntityCommand("", this.type);
                    }
                }
            } else {
                return this.commandWithOrWithoutArgument(input.currentArg());
            }            
        } else {
            return new RemoveEntityCommand("", this.type);
        }        
    }

    private Command commandWithOrWithoutArgument(String arg) {
        if ( wordIsAcceptable(arg) ) {
            return new RemoveEntityCommand(arg, this.type);
        } else {
            return new RemoveEntityCommand("", this.type);
        }
    }
}
