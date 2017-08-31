/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.interpreter.recognizers;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.stream;

/**
 *
 * @author Diarsid
 */
public class ControlWordsContext {    
    
    private final Set<String> controlWords;
    private final Set<String> independentControlWords;
    
    ControlWordsContext() {
        this.controlWords = new HashSet<>();
        this.independentControlWords = new HashSet<>();
    }    
    
    public boolean isControlWord(String word) {
        return this.controlWords.contains(word);
    }
    
    public boolean isDependentControlWord(String word) {
        return this.controlWords.contains(word) 
                && !this.independentControlWords.contains(word);
    }
    
    void add(String word) {
        this.controlWords.add(word);
    }
    
    void add(String... words) {
        stream(words).forEach(word -> this.controlWords.add(word));
    }
    
    void addToIndependent(String word) {
        this.independentControlWords.add(word);
    }
    
    void addToIndependent(String... words) {
        stream(words).forEach(word -> this.independentControlWords.add(word));
    }
}
