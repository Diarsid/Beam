/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.creation.CreateWebDirectoryCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.Recognizer;
import diarsid.beam.core.domain.entities.WebPlacement;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.control.interpreter.ControlKeys.wordIsAcceptable;
import static diarsid.beam.core.domain.entities.WebPlacement.argToPlacement;


public class CreateWebDirectoryRecognizer implements Recognizer {
    
    public CreateWebDirectoryRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            switch ( input.remainingArgsQty() ) {
                case 1 : {
                    WebPlacement place = argToPlacement(input.currentArg());
                    if ( nonNull(place) ) {
                        return new CreateWebDirectoryCommand("", place);
                    } else if ( wordIsAcceptable(input.currentArg()) ) {
                        return new CreateWebDirectoryCommand(input.currentArg());
                    }
                }
                case 2 : {                        
                    String arg0 = input.currentArg();
                    String arg1 = input.toNextArg().currentArg();
                    
                    WebPlacement place = argToPlacement(arg0);                    
                    if ( nonNull(place) ) {
                        return this.commandWithOrWithoutName(arg1, place);
                    } else if ( nonNull(place = argToPlacement(arg1)) ) {
                        return this.commandWithOrWithoutName(arg0, place);
                    } else {
                        return undefinedCommand();
                    }
                }
                default : {
                    return undefinedCommand();
                }
            }  
        } else {
            return new CreateWebDirectoryCommand("");
        }
    }
    
    private Command commandWithOrWithoutName(String name, WebPlacement place) {
        if ( wordIsAcceptable(name) ) {
            return new CreateWebDirectoryCommand(name, place);
        } else {
            return new CreateWebDirectoryCommand("", place);
        }
    }
}
