/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands;

import static java.util.Objects.isNull;


public abstract class DoubleStringCommand implements Command {
    
    private final String first;
    private final String second;
    
    protected DoubleStringCommand(String first, String second) {
        this.onlyNonNullArgument(first);
        this.onlyNonNullArgument(second);
        this.first = first;        
        this.second = second;
    }
    
    private void onlyNonNullArgument(String arg) {
        if ( isNull(arg) ) {
            throw new NullPointerException("Command argument cannot be null.");
        }
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

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }
    
}
