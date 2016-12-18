/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import java.util.List;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.creation.CreateLocationCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.Recognizer;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.control.interpreter.ControlKeys.wordIsAcceptable;
import static diarsid.beam.core.util.PathUtils.isAcceptableFilePath;


public class LocationCreationRecognizer implements Recognizer {
    
    public LocationCreationRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            System.out.println("LOCATION CREATION");
            List<String> args = input.allRemainingArgs();
            switch ( args.size() ) {
                case 1 : {
                    String arg = args.get(0);
                    if ( isAcceptableFilePath(arg) ) {
                        return new CreateLocationCommand("", arg);
                    } else if ( wordIsAcceptable(arg) ) {
                        return new CreateLocationCommand(arg, "");
                    } else {
                        return undefinedCommand();
                    }
                } 
                case 2 : {
                    String arg0 = args.get(0);
                    String arg1 = args.get(1);
                    if ( isAcceptableFilePath(arg0) && wordIsAcceptable(arg1) ) {
                        // name, path
                        return new CreateLocationCommand(arg1, arg0);
                    } else if ( isAcceptableFilePath(arg1) && wordIsAcceptable(arg0) ) {
                        // path, name
                        return new CreateLocationCommand(arg0, arg1);
                    } else {
                        return undefinedCommand();
                    }
                } 
                default : {
                    return undefinedCommand();
                }
            }
        } else {
            return new CreateLocationCommand("", "");
        }
    }
}
