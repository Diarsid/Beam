/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import static diarsid.beam.core.base.util.StringUtils.nonNullNonEmpty;


public abstract class DoubleStringCommand extends StringCommand {
    
    private String first;
    private String second;
    
    protected DoubleStringCommand(String first, String second) {
        super();
        super.onlyNonNullArgument(first);
        super.onlyNonNullArgument(second);
        this.first = first;        
        this.second = second;
    }
    
    protected final boolean hasFirst() {
        return ! this.first.isEmpty();
    }
    
    protected final boolean hasSecond() {
        return ! this.second.isEmpty();
    }
    
    protected final boolean hasNotFirst() {
        return this.first.isEmpty();
    }
    
    protected final boolean hasNotSecond() {
        return this.second.isEmpty();
    }

    protected String getFirst() {
        return first;
    }

    protected String getSecond() {
        return second;
    }
    
    protected void resetFirst(String newFirst) {
        if ( nonNullNonEmpty(newFirst) ) {
            this.first = newFirst;
        }
    }
    
    protected void resetSecond(String newSecond) {
        if ( nonNullNonEmpty(newSecond) ) {
            this.second = newSecond;
        }
    }    
}
