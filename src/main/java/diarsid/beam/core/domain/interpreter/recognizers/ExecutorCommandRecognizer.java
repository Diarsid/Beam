/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.interpreter.recognizers;

import diarsid.beam.core.domain.interpreter.Input;
import diarsid.beam.core.domain.interpreter.IntermediateRecognizer;
import diarsid.beam.core.domain.interpreter.Recognition;

import static diarsid.beam.core.domain.commands.OperationType.CALL_BATCH;
import static diarsid.beam.core.domain.commands.OperationType.OPEN_LOCATION;
import static diarsid.beam.core.domain.commands.OperationType.RUN_MARKED_PROGRAM;
import static diarsid.beam.core.domain.commands.OperationType.RUN_PROGRAM;
import static diarsid.beam.core.domain.commands.OperationType.SEE_WEBPAGE;
import static diarsid.beam.core.domain.interpreter.Recognition.NOT_RECOGNIZED;
import static diarsid.beam.core.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class ExecutorCommandRecognizer extends IntermediateRecognizer {

    @Override
    public Recognition assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            switch ( lower(input.argToRecognize()) ) {
                case "bat" :
                case "exe" :
                case "exec" :
                case "call" : {
                    input.recognizedAs(CALL_BATCH);
                    input.toNextArg();
                    return super.delegateRecognitionForward(input);
                }
                case "see" :
                case "www" : {
                    input.recognizedAs(SEE_WEBPAGE);
                    input.toNextArg();
                    return super.delegateRecognitionForward(input);
                }
                case "op" :
                case "open" : {
                    input.recognizedAs(OPEN_LOCATION);
                    input.toNextArg();
                    return super.delegateRecognitionForward(input);
                }
                case "run" : {
                    input.recognizedAs(RUN_PROGRAM);
                    input.toNextArg();
                    return super.delegateRecognitionForward(input);
                }
                case "start" :
                case "stop" : {
                    input.recognizedAs(RUN_MARKED_PROGRAM);
                    input.toNextArg();
                    return super.delegateRecognitionForward(input);
                }    
                default : {
                    return NOT_RECOGNIZED;
                }
            }
        } else {
            return NOT_RECOGNIZED;
        }        
    }
}
