/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import java.util.Objects;

import static diarsid.beam.core.base.control.io.commands.CommandType.UNDEFINED;


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
    public CommandType type() {
        return this.type;
    }
    
    public boolean isDefined() {
        return ( this.type != UNDEFINED );
    }
    
    public boolean isNotDefined() {
        return ( this.type == UNDEFINED );
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final EmptyCommand other = ( EmptyCommand ) obj;
        if ( this.type != other.type ) {
            return false;
        }
        return true;
    }
}
