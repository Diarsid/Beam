/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.interpreter;

import diarsid.beam.core.control.io.commands.Command;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.control.io.commands.EmptyCommand.undefinedCommand;


public abstract class NodeRecognizer extends PrioritizedRecognizer {
      
    private Recognizer recognizer;
    
    public NodeRecognizer() {
    }
    
    private void onlyIfRecognizerHasNotBeenSet() {
        if ( nonNull(this.recognizer) ) {
            throw new RecognizerException(
                    this.getClass().getSimpleName() + 
                            " has been already set with internal recognizer.");
        }
    }
    
    public final NodeRecognizer pointsTo(Recognizer recognizer) {
        this.onlyIfRecognizerHasNotBeenSet();
        this.recognizer = recognizer;
        return this;
    }
   
    public final NodeRecognizer branchesTo(PrioritizedRecognizer... recognizers) {
        this.onlyIfRecognizerHasNotBeenSet();
        this.recognizer = new RecognizersCluster(recognizers);
        return this;
    }
        
    protected final Command delegateRecognitionForward(Input input) {
        if ( this.hasRecognizer() ) {
            return this.recognizer.assess(input);
        } else {
            return undefinedCommand();
        }
    }

    public boolean hasRecognizer() {
        return nonNull(this.recognizer);
    }
    
    @Override
    public final NodeRecognizer priority(RecognizerPriority priority) {
        super.priority(priority);
        return this;
    }
    
    @Override
    public final NodeRecognizer priority(int priority) {
        super.priority(priority);
        return this;
    }

}
