/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.interpreter.recognizers;

import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.commands.creation.CreateWebDirectoryCommand;
import diarsid.beam.core.control.io.interpreter.Input;
import diarsid.beam.core.control.io.interpreter.Recognizer;
import diarsid.beam.core.control.io.interpreter.StreamArgumentsInterceptor;

import static diarsid.beam.core.control.io.interpreter.StreamArgumentsInterceptor.ArgumentType.SIMPLE_WORD;
import static diarsid.beam.core.control.io.interpreter.StreamArgumentsInterceptor.ArgumentType.WEB_PLACEMENT;


public class CreateWebDirectoryRecognizer implements Recognizer {
    
    public CreateWebDirectoryRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            StreamArgumentsInterceptor args = new StreamArgumentsInterceptor();
            input.allRemainingArgs()
                    .stream()
                    .filter(arg -> args.interceptArgumentOfType(arg, WEB_PLACEMENT).ifContinue())
                    .filter(arg -> args.interceptArgumentOfType(arg, SIMPLE_WORD).ifContinue())
                    .count();
            
            return new CreateWebDirectoryCommand(args.of(SIMPLE_WORD), args.of(WEB_PLACEMENT));            
        } else {
            return new CreateWebDirectoryCommand("");
        }
    }
}
