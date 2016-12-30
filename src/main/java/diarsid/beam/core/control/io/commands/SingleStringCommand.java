/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;

import static diarsid.beam.core.util.StringUtils.nonNullNonEmpty;


public abstract class SingleStringCommand extends StringCommand {
    
    private String arg;
    private final CommandType type;
    
    public SingleStringCommand(String arg, CommandType type) {
        super();
        super.onlyNonNullArgument(arg);
        this.arg = arg;
        this.type = type;
    }

    public String getArg() {
        return this.arg;
    }

    public boolean hasArg() {
        return ( ! this.arg.isEmpty() );
    }
    
    public void resetArg(String newArg) {
        if ( nonNullNonEmpty(newArg) ) {
            this.arg = newArg;
        }
    }
    
    public boolean hasNoArg() {
        return this.arg.isEmpty();
    }

    @Override
    public CommandType type() {
        return this.type;
    }
}
