/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.EditEntityCommand;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.PrioritizedRecognizer;
import diarsid.beam.core.base.control.io.interpreter.StreamArgumentsInterceptor;

import static diarsid.beam.core.base.control.io.commands.EditableTarget.TARGET_UNDEFINED;
import static diarsid.beam.core.base.control.io.commands.EditableTarget.targetOf;
import static diarsid.beam.core.base.control.io.interpreter.StreamArgumentsInterceptor.ArgumentType.EDITABLE_TARGET;
import static diarsid.beam.core.base.control.io.interpreter.StreamArgumentsInterceptor.ArgumentType.SIMPLE_WORD;


public class EditEntityRecognizer extends PrioritizedRecognizer {
    
    private final CommandType type;
    
    public EditEntityRecognizer(CommandType type) {
        this.type = type;
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            StreamArgumentsInterceptor args = new StreamArgumentsInterceptor();
            input.allRemainingArgs()
                    .stream()
                    .filter(arg -> args.interceptArgumentOfType(arg, EDITABLE_TARGET).ifContinue())
                    .filter(arg -> args.interceptArgumentOfType(arg, SIMPLE_WORD).ifContinue())
                    .count();
            
            return new EditEntityCommand(
                    args.of(SIMPLE_WORD), 
                    targetOf(args.of(EDITABLE_TARGET)), 
                    this.type);
        } else {
            return new EditEntityCommand("", TARGET_UNDEFINED, this.type);
        }
    }
}
