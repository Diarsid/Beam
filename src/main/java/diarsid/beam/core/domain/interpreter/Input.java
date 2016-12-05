/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.interpreter;

import java.util.List;

import diarsid.beam.core.domain.commands.OperationType;

import static diarsid.beam.core.domain.commands.OperationType.UNDEFINED;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsFullWordIgnoreCase;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.indexOfIgnoreCase;
import static diarsid.beam.core.util.StringUtils.splitBySpacesToList;

/**
 *
 * @author Diarsid
 */
public class Input {
    
    private List<String> splited;
    private OperationType recognizedOperation;
    private int processedParamIndex;
    
    public Input(String input) {
        this.splited = splitBySpacesToList(input);
        this.recognizedOperation = UNDEFINED;
        this.processedParamIndex = 0;
    }
    
    public void toNextArg() {
        if ( this.processedParamIndex < this.splited.size() - 1 ) {
            this.processedParamIndex++;
        }        
    }
    
    public String argToRecognize() {
        return this.splited.get(this.processedParamIndex);
    }
    
    public List<String> args() {
        return this.splited;
    }
    
    public String argAt(int i) {
        return this.splited.get(i);
    }
    
    public boolean hasArgsQty(int qty) {
        return ( this.splited.size() == qty );
    }
    
    public boolean hasNotRecognizedArgs() {
        return ( this.processedParamIndex < ( this.splited.size() - 1 ) );
    }
    
    public boolean hasMoreArgsThan(int qty) {
        return ( this.splited.size() > qty );
    }
    
    public boolean hasLessArgsThan(int qty) {
        return ( this.splited.size() < qty );
    }
    
    public boolean hasAnyArgsAfter(String arg) {
        if ( containsFullWordIgnoreCase(this.splited, arg) ) {
            return this.hasMoreArgsThan(indexOfIgnoreCase(this.splited, arg) + 1);
        } else {
            return false;
        }
    }
    
    public OperationType recognizedOperation() {
        return this.recognizedOperation;
    }
    
    public boolean isOperationRecognized() {
        return this.recognizedOperation != UNDEFINED ;
    }
    
    public boolean isOperationNotRecognized() {
        return this.recognizedOperation == UNDEFINED ;
    }

    public void recognizedAs(OperationType operation) {
        this.recognizedOperation = operation;
    }   
    
}
