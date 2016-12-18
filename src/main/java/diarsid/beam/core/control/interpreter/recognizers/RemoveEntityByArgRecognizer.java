package diarsid.beam.core.control.interpreter.recognizers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.SingleStringCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.PrioritizedRecognizer;

import static diarsid.beam.core.control.interpreter.ControlKeys.wordIsAcceptable;


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
                        return new SingleStringCommand("", this.type);
                    }
                }
            } else {
                return this.commandWithOrWithoutArgument(input.currentArg());
            }            
        } else {
            return new SingleStringCommand("", this.type);
        }        
    }

    private Command commandWithOrWithoutArgument(String arg) {
        if ( wordIsAcceptable(arg) ) {
            return new SingleStringCommand(arg, this.type);
        } else {
            return new SingleStringCommand("", this.type);
        }
    }
}
