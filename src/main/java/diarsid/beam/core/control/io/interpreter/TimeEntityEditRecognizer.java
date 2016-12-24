/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.interpreter;

import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.commands.CommandType;
import diarsid.beam.core.control.io.commands.TimeEntityEditCommand;


public class TimeEntityEditRecognizer implements Recognizer {
    
    private final CommandType type;
    private final ScheduledEntityArgumentsDetector detector;
    
    public TimeEntityEditRecognizer(CommandType type) {
        this.type = type;
        this.detector = new ScheduledEntityArgumentsDetector();
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            ScheduledEntityArguments args = this.detector.findTimeAndTextArgsFrom(input);
            return new TimeEntityEditCommand(args.getTime(), args.getText(), this.type);
        } else {
            return new TimeEntityEditCommand("", "", this.type);
        }
    }
}
