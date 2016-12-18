/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import static diarsid.beam.core.control.interpreter.RecognizerPriority.MEDIUM;


public abstract class PrioritizedRecognizer implements 
        Comparable<PrioritizedRecognizer>, 
        Recognizer {
    
    private int priority;
    
    public PrioritizedRecognizer() {
        this.priority = MEDIUM.value();        
    }
    
    
    public final int getPriority() {
        return this.priority;
    }
    
    public PrioritizedRecognizer priority(RecognizerPriority priority) {
        this.priority = priority.value();
        return this;
    }
    
    public PrioritizedRecognizer priority(int priority) {
        this.priority = priority;
        return this;
    }
    
    @Override
    public final int compareTo(PrioritizedRecognizer other) {
        if ( other.getPriority() < this.getPriority() ) {
            return -1;
        } else if ( other.getPriority() > this.getPriority() ) {
            return 1;
        } else {
            return this.hashCode() > other.hashCode() ? -1 : 1;
        }
    }
}
