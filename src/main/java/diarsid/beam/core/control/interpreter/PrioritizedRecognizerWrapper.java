/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import diarsid.beam.core.control.commands.Command;


public class PrioritizedRecognizerWrapper extends PrioritizedRecognizer {
    
    private final Recognizer recognizer;
    
    public PrioritizedRecognizerWrapper(Recognizer recognizer) {
        this.recognizer = recognizer;
    }

    @Override
    public Command assess(Input input) {
        return this.recognizer.assess(input);
    }
    
    public static PrioritizedRecognizerWrapper prioritized(Recognizer recognizer) {
        return new PrioritizedRecognizerWrapper(recognizer);
    }
}
