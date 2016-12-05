/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.interpreter.recognizers;

import diarsid.beam.core.domain.commands.executor.CallBatchCommand;
import diarsid.beam.core.domain.interpreter.Input;
import diarsid.beam.core.domain.interpreter.PrioritizedRecognizer;
import diarsid.beam.core.domain.interpreter.Recognition;

import static diarsid.beam.core.domain.interpreter.Recognition.NOT_RECOGNIZED;
import static diarsid.beam.core.domain.interpreter.Recognition.RECOGNIZED;


public class BatchCallArgsRecognizer extends PrioritizedRecognizer {
    
    public BatchCallArgsRecognizer() {
    }

    @Override
    public Recognition assess(Input input) {
        if ( input.isOperationRecognized() && input.hasNotRecognizedArgs() ) {
            CallBatchCommand command = new CallBatchCommand(input.argToRecognize());
            return RECOGNIZED;
        } else {
            return NOT_RECOGNIZED;
        }
    }
}
