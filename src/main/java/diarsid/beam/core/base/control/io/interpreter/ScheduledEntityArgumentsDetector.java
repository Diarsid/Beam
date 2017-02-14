/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import static java.lang.Character.isDigit;

/**
 *
 * @author Diarsid
 */
public class ScheduledEntityArgumentsDetector {
    
    public ScheduledEntityArgumentsDetector() {
    }
    
    private boolean argIsPlusTime(Input input) {
        // matches +1, +10, +15, +123 ...
        return input.currentArg().matches("[+]\\d*");
    }
    
    private boolean argStartsWithDigit(Input input) {
        return isDigit(input.currentArg().charAt(0));
    }
    
    public ScheduledEntityArguments findTimeAndTextArgsFrom(Input input) {
        if ( this.argIsPlusTime(input) || this.argStartsWithDigit(input) ) {
            String timeString = input.currentArg();
            if ( input.toNextArg().hasNotRecognizedArgs() ) {
                if ( this.argStartsWithDigit(input) ) {
                    timeString = timeString + " " + input.currentArg();
                    if ( input.toNextArg().hasNotRecognizedArgs() ) {
                        return new ScheduledEntityArguments(
                                timeString, input.allRemainingArgsString());
                    } else {
                        return new ScheduledEntityArguments(timeString, "");
                    }
                } else {
                    return new ScheduledEntityArguments(
                            timeString, input.allRemainingArgsString());
                }
            } else {
                return new ScheduledEntityArguments(timeString, "");
            }
        } else {
            return new ScheduledEntityArguments("", input.allRemainingArgsString());
        }
    }
}
