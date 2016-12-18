/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.EditEntityCommand;
import diarsid.beam.core.control.commands.EditableTarget;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.PrioritizedRecognizer;

import static diarsid.beam.core.control.commands.EditableTarget.TARGET_UNDEFINED;
import static diarsid.beam.core.control.commands.EditableTarget.argToTarget;
import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.control.interpreter.ControlKeys.wordIsAcceptable;


public class EditEntityRecognizer extends PrioritizedRecognizer {
    
    private final CommandType type;
    
    public EditEntityRecognizer(CommandType type) {
        this.type = type;
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            switch ( input.remainingArgsQty() ) {
                case 1 : {
                    EditableTarget target = argToTarget(input.currentArg());
                    if ( target.isDefined() ) {
                        return new EditEntityCommand("", target, this.type);
                    } else if ( wordIsAcceptable(input.currentArg()) ) {
                        return new EditEntityCommand(input.currentArg(), TARGET_UNDEFINED, this.type);
                    }
                }
                case 2 : {                        
                    String arg0 = input.currentArg();
                    String arg1 = input.toNextArg().currentArg();
                    
                    EditableTarget target = argToTarget(arg0);                    
                    if ( target.isDefined() ) {
                        return this.commandWithOrWithoutName(arg1, target);
                    } else if ( (target = argToTarget(arg1)).isDefined() ) {
                        return this.commandWithOrWithoutName(arg0, target);
                    } else {
                        return undefinedCommand();
                    }
                }
                default : {
                    return undefinedCommand();
                }
            }
        } else {
            return new EditEntityCommand("", TARGET_UNDEFINED, this.type);
        }
    }
    
    private Command commandWithOrWithoutName(String name, EditableTarget target) {
        if ( wordIsAcceptable(name) ) {
            return new EditEntityCommand(name, target, this.type);
        } else {
            return new EditEntityCommand("", target, this.type);
        }
    }
}
