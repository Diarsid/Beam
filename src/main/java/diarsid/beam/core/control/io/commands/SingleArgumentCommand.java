/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;

import java.util.Objects;


public abstract class SingleArgumentCommand implements ArgumentedCommand {
    
    private final Argument argument;
    
    public SingleArgumentCommand(String argument) {
        this.argument = new Argument(argument);
    }
    
    public SingleArgumentCommand(String argument, String extendedArgument) {
        this.argument = new Argument(argument, extendedArgument);
    }

    @Override
    public String stringifyOriginal() {
        return this.argument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.argument.getExtended();
    }
    
    public final Argument argument() {
        return this.argument;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.argument);
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
        final SingleArgumentCommand other = ( SingleArgumentCommand ) obj;
        if ( !Objects.equals(this.argument, other.argument) ) {
            return false;
        }
        return true;
    }
    
    
}
