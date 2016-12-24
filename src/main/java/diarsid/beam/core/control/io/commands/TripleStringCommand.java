/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;


public abstract class TripleStringCommand extends StringCommand {
    
    private final String first;
    private final String second;
    private final String third;
    
    public TripleStringCommand(String first, String second, String third) {
        super();
        super.onlyNonNullArgument(first);
        super.onlyNonNullArgument(second);
        super.onlyNonNullArgument(third);
        this.first = first;
        this.second = second;
        this.third = third;
    }

    protected String getFirst() {
        return this.first;
    }

    protected String getSecond() {
        return this.second;
    }

    protected String getThird() {
        return this.third;
    }
    
    protected boolean hasFirst() {
        return ! this.first.isEmpty();
    }
    
    protected boolean hasSecond() {
        return ! this.second.isEmpty();
    }
    
    protected boolean hasThird() {
        return ! this.third.isEmpty();
    }

}
