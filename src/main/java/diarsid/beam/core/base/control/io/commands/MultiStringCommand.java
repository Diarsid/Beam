/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import java.util.List;
import java.util.Objects;

import static java.lang.String.join;
import static java.util.Collections.emptyList;


public class MultiStringCommand implements Command {
    
    private final List<String> arguments;
    private final CommandType type;
    
    public MultiStringCommand(CommandType type, List<String> arguments) {
        this.arguments = arguments;
        this.type = type;
    }
    
    public MultiStringCommand(CommandType type) {
        this.arguments = emptyList();
        this.type = type;
    }
    
    public boolean hasArguments() {
        return ! this.arguments.isEmpty();
    }
    
    public boolean hasNoArguments() {
        return this.arguments.isEmpty();
    }

    public List<String> arguments() {
        return this.arguments;
    }
    
    public String joinedArguments() {
        return join(" ", this.arguments);
    }

    @Override
    public CommandType type() {
        return this.type;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.arguments);
        hash = 71 * hash + Objects.hashCode(this.type);
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
        final MultiStringCommand other = ( MultiStringCommand ) obj;
        if ( !Objects.equals(this.arguments, other.arguments) ) {
            return false;
        }
        if ( this.type != other.type ) {
            return false;
        }
        return true;
    }
}
