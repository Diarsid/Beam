/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;


import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.NodeRecognizer;

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;

/**
 *
 * @author Diarsid
 */
public class InputCorrectnessRecognizer extends NodeRecognizer {
        
    @Override
    public Command assess(Input input) {
        if ( input.hasMoreArgsThan(0) ) {
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }       
    }
}
