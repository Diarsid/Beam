/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.interpreter;

/**
 *
 * @author Diarsid
 */
public interface Recognizer extends Comparable<Recognizer> {
    
    Recognition assess(Input input);
    
    Recognizer withPriority(int priority);
    
    Recognizer withPriority(RecognizerPriority priority);
    
    int getPriority();
    
    @Override
    public default int compareTo(Recognizer other) {
        if ( other.getPriority() > this.getPriority() ) {
            return -1;
        } else if ( other.getPriority() < this.getPriority() ) {
            return 1;
        } else {
            return this.hashCode() > other.hashCode() ? -1 : 1;
        }
    }
}
