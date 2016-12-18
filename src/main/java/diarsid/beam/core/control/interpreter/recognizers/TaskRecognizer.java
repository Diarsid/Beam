/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;

import static com.sun.xml.internal.bind.v2.schemagen.Util.equalsIgnoreCase;


public class TaskRecognizer extends NodeRecognizer {
    
    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() && equalsIgnoreCase(input.currentArg(), "task") ) {
            input.toNextArg();
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }
    }
}
