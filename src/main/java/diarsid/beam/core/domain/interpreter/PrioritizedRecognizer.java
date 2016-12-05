/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.interpreter;

import static diarsid.beam.core.domain.interpreter.RecognizerPriority.MEDIUM;


public abstract class PrioritizedRecognizer implements Recognizer {
    
    private int priority;
    
    public PrioritizedRecognizer() {        
        this.priority = MEDIUM.value();
    }

    @Override
    public abstract Recognition assess(Input input);

    @Override
    public PrioritizedRecognizer withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public PrioritizedRecognizer withPriority(RecognizerPriority priority) {
        this.priority = priority.value();
        return this;
    }
}
