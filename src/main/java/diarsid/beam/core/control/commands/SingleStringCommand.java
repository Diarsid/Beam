/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands;

import static java.util.Objects.isNull;


public class SingleStringCommand implements Command {
    
    private final String arg;
    private final CommandType type;
    
    public SingleStringCommand(String arg, CommandType type) {
        this.onlyNonNullArgument(arg);
        this.arg = arg;
        this.type = type;
    }
    
    private void onlyNonNullArgument(String arg) {
        if ( isNull(arg) ) {
            throw new NullPointerException("Command argument cannot be null.");
        }
    }

    public String getArg() {
        return this.arg;
    }

    public boolean hasArg() {
        return ( ! this.arg.isEmpty() );
    }

    @Override
    public CommandType getType() {
        return this.type;
    }
}
