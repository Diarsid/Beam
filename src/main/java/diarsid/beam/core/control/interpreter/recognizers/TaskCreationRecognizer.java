/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.creation.CreateTaskCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.Recognizer;

import static java.lang.Character.isDigit;


public class TaskCreationRecognizer implements Recognizer {
    
    public TaskCreationRecognizer() {
    }
    
    private boolean argIsPlusTime(Input input) {
        // matches +1, +10, +15, +123 ...
        return input.currentArg().matches("[+]\\d*");
    }
    
    private boolean argStartsWithDigit(Input input) {
        return isDigit(input.currentArg().charAt(0));
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            if ( this.argIsPlusTime(input) || this.argStartsWithDigit(input) ) {
                String timeString = input.currentArg();
                if ( input.toNextArg().hasNotRecognizedArgs() ) {
                    if ( this.argStartsWithDigit(input) ) {
                        timeString = timeString + " " + input.currentArg();
                        if ( input.toNextArg().hasNotRecognizedArgs() ) {
                            return new CreateTaskCommand(
                                    timeString, input.allRemainingArgsString());
                        } else {
                            return new CreateTaskCommand(timeString, "");
                        }
                    } else {
                        return new CreateTaskCommand(
                                timeString, input.allRemainingArgsString());
                    }
                } else {
                    return new CreateTaskCommand(timeString, "");
                }
            } else {
                return new CreateTaskCommand("", input.allRemainingArgsString());
            }
        } else {
            return new CreateTaskCommand("", "");
        }
    }
}
