/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands;


public class SingleStringCommand extends StringCommand {
    
    private final String arg;
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

    @Override
    public CommandType getType() {
        return this.type;
    }
}
