/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.interpreter.recognizers;


import diarsid.beam.core.domain.interpreter.Input;
import diarsid.beam.core.domain.interpreter.IntermediateRecognizer;
import diarsid.beam.core.domain.interpreter.Recognition;

import static diarsid.beam.core.domain.interpreter.Recognition.NOT_RECOGNIZED;

/**
 *
 * @author Diarsid
 */
public class InputCorrectnessRecognizer extends IntermediateRecognizer {
        
    @Override
    public Recognition assess(Input input) {
        if ( input.hasMoreArgsThan(0) ) {
            return super.delegateRecognitionForward(input);
        } else {
            return NOT_RECOGNIZED;
        }       
    }
}
