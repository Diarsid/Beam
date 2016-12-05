/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.interpreter;

import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;

import static diarsid.beam.core.domain.interpreter.Recognition.NOT_RECOGNIZED;
import static diarsid.beam.core.domain.interpreter.Recognition.RECOGNIZED;

/**
 *
 * @author Diarsid
 */
public abstract class IntermediateRecognizer extends PrioritizedRecognizer {
    
    private final Set<Recognizer> childRecognizers;
    
    public IntermediateRecognizer() {
        this.childRecognizers = new TreeSet<>();
    }
    
    public final IntermediateRecognizer branchesTo(Recognizer... recognizers) {
        this.childRecognizers.addAll(asList(recognizers));
        return this;
    }
    
    protected final Recognition delegateRecognitionForward(Input input) {
        for (Recognizer recognizer : this.childRecognizers) {
            if ( recognizer.assess(input) == RECOGNIZED ) {
                return RECOGNIZED;
            }
        }
        return NOT_RECOGNIZED; 
    }

    @Override
    public abstract Recognition assess(Input input);

    @Override
    public IntermediateRecognizer withPriority(int priority) {
        super.withPriority(priority);
        return this;
    }

    @Override
    public IntermediateRecognizer withPriority(RecognizerPriority priority) {
        super.withPriority(priority);
        return this;
    }
}
