/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import java.util.List;

import static java.lang.String.join;

import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.indexOfIgnoreCase;
import static diarsid.beam.core.util.StringUtils.splitBySpacesToList;

/**
 *
 * @author Diarsid
 */
public class Input {
    
    private List<String> splited;
    private int processedParamIndex;
    
    public Input(String input) {
        this.splited = splitBySpacesToList(input);
        this.processedParamIndex = 0;
    }
    
    public Input toNextArg() {
        this.processedParamIndex++;        
        return this;
    }
    
    private String safeArgGetByIndex(int i) {
        if ( i >= this.splited.size() ) {
            return this.splited.get(this.splited.size() - 1);
        } else {
            return this.splited.get(i);
        }
    }
    
    public String currentArg() {
        return this.safeArgGetByIndex(this.processedParamIndex);
    }
    
    public List<String> allRemainingArgs() {
        return this.safeGetArgsSublist();        
    }
    
    public String allRemainingArgsString() {
        return join(" ", this.safeGetArgsSublist());
    }
    
    public boolean hasRemainingArgsQty(int number) {
        return ( this.splited.size() - this.processedParamIndex == number );
    }
    
    public int remainingArgsQty() {
        return this.splited.size() - this.processedParamIndex;
    }

    private List<String> safeGetArgsSublist() {
        if ( this.processedParamIndex >= this.splited.size() ) {
            return this.splited.subList(this.splited.size() - 1, this.splited.size());
        } else {
            return this.splited.subList(this.processedParamIndex, this.splited.size());
        }        
    }
    
    public List<String> args() {
        return this.splited;
    }
    
    public void resetCurrentArg(String arg) {
        if ( this.processedParamIndex >= this.splited.size() ) {
            this.splited.set((this.splited.size() - 1), arg);
        } else {
            this.splited.set(this.processedParamIndex, arg);
        }        
    }
    
    public String argAt(int i) {
        return this.safeArgGetByIndex(i);
    }
    
    public boolean hasArgsQty(int qty) {
        return ( this.splited.size() == qty );
    }
    
    public boolean hasNotRecognizedArgs() {
        return ( this.processedParamIndex < ( this.splited.size() ) );
    }
    
    public boolean hasMoreArgsThan(int qty) {
        return ( this.splited.size() > qty );
    }
    
    public boolean hasLessArgsThan(int qty) {
        return ( this.splited.size() < qty );
    }
    
    public boolean hasAnyArgsAfter(String arg) {
        if ( containsWordInIgnoreCase(this.splited, arg) ) {
            return this.hasMoreArgsThan(indexOfIgnoreCase(this.splited, arg) + 1);
        } else {
            return false;
        }
    }    
}
