/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.creation.CreateTaskCommand;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.Recognizer;
import diarsid.beam.core.base.control.io.interpreter.ScheduledEntityArguments;
import diarsid.beam.core.base.control.io.interpreter.ScheduledEntityArgumentsDetector;


public class CreateTaskRecognizer implements Recognizer {
    
    private final ScheduledEntityArgumentsDetector detector;
    
    public CreateTaskRecognizer() {
        this.detector = new ScheduledEntityArgumentsDetector();
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            ScheduledEntityArguments args = this.detector.findTimeAndTextArgsFrom(input);
            return new CreateTaskCommand(args.getTime(), args.getText());
        } else {
            return new CreateTaskCommand("", "");
        }
    }
}
