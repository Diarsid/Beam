/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;


public abstract class DoubleStringCommand extends StringCommand {
    
    private final String first;
    private final String second;
    
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

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }
    
}
