/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import java.util.Objects;


public class SingleStringCommand implements Command {
    
    private final String arg;
    private final CommandType type;
    
    public SingleStringCommand(CommandType type, String arg) {
        this.arg = arg;
        this.type = type;
    }
    
    public SingleStringCommand(CommandType type) {
        this.arg = "";
        this.type = type;
    }

    public String getArg() {
        return this.arg;
    }

    public boolean hasArg() {
        return ( ! this.arg.isEmpty() );
    }
    
    public boolean hasNoArg() {
        return this.arg.isEmpty();
    }

    @Override
    public CommandType type() {
        return this.type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.arg);
        hash = 53 * hash + Objects.hashCode(this.type);
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
        final SingleStringCommand other = ( SingleStringCommand ) obj;
        if ( !Objects.equals(this.arg, other.arg) ) {
            return false;
        }
        if ( this.type != other.type ) {
            return false;
        }
        return true;
    }
    
    
}
