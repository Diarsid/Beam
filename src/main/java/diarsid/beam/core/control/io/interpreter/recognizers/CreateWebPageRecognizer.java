/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.interpreter.recognizers;

import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.commands.creation.CreateWebPageCommand;
import diarsid.beam.core.control.io.interpreter.Input;
import diarsid.beam.core.control.io.interpreter.PrioritizedRecognizer;
import diarsid.beam.core.control.io.interpreter.StreamArgumentsInterceptor;

import static diarsid.beam.core.control.io.interpreter.StreamArgumentsInterceptor.ArgumentType.SIMPLE_WORD;
import static diarsid.beam.core.control.io.interpreter.StreamArgumentsInterceptor.ArgumentType.WEB_PATH;
import static diarsid.beam.core.control.io.interpreter.StreamArgumentsInterceptor.ArgumentType.WEB_PLACEMENT;


public class CreateWebPageRecognizer extends PrioritizedRecognizer {
    
    public CreateWebPageRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            StreamArgumentsInterceptor args = new StreamArgumentsInterceptor();
            input.allRemainingArgs()
                    .stream()
                    .filter(arg -> args.interceptArgumentOfType(arg, WEB_PATH).ifContinue())
                    .filter(arg -> args.interceptArgumentOfType(arg, WEB_PLACEMENT).ifContinue())
                    .filter(arg -> args.interceptArgumentOfType(arg, SIMPLE_WORD).ifContinue())
                    .count();
            
//            System.out.println(join(", ", input.allRemainingArgs()));
//            System.out.println(notAccepted);
            
            return new CreateWebPageCommand(
                    args.of(SIMPLE_WORD), 
                    args.of(WEB_PATH), 
                    args.of(WEB_PLACEMENT)
            );
        } else {
            return new CreateWebPageCommand("", "", "");
        }
    }
}
