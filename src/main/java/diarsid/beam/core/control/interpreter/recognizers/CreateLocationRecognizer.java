/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.creation.CreateLocationCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.Recognizer;
import diarsid.beam.core.control.interpreter.StreamArgumentsInterceptor;

import static diarsid.beam.core.control.interpreter.StreamArgumentsInterceptor.ArgumentType.FILE_PATH;
import static diarsid.beam.core.control.interpreter.StreamArgumentsInterceptor.ArgumentType.SIMPLE_WORD;


public class CreateLocationRecognizer implements Recognizer {
    
    public CreateLocationRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            StreamArgumentsInterceptor args = new StreamArgumentsInterceptor();
            input.allRemainingArgs()
                    .stream()
                    .filter(arg -> args.interceptArgumentOfType(arg, FILE_PATH).ifContinue())
                    .filter(arg -> args.interceptArgumentOfType(arg, SIMPLE_WORD).ifContinue())
                    .count();
            
            return new CreateLocationCommand(args.of(SIMPLE_WORD), args.of(FILE_PATH));
        } else {
            return new CreateLocationCommand("", "");
        }
    }
}
