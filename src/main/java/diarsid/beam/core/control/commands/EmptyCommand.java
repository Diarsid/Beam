/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands;

import static diarsid.beam.core.control.commands.CommandType.UNDEFINED;


public class EmptyCommand implements Command {
    
    private static final Command UNDEFINED_COMMAND;
    static {
        UNDEFINED_COMMAND = new EmptyCommand(UNDEFINED);
    }
    
    private final CommandType type;
    
    public EmptyCommand(CommandType type) {
        this.type = type;   
    }

    public static Command undefinedCommand() {
        return UNDEFINED_COMMAND;
    }

    @Override
    public CommandType getType() {
        return this.type;
    }
    
    public boolean isDefined() {
        return ( this.type != UNDEFINED );
    }
    
    public boolean isNotDefined() {
        return ( this.type == UNDEFINED );
    }
}
