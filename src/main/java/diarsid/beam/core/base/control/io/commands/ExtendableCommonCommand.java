/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import java.util.Objects;


public abstract class ExtendableCommonCommand implements ExtendableCommand {
    
    private final ExtendableArgument argument;
    
    public ExtendableCommonCommand(String argument) {
        this.argument = new ExtendableArgument(argument);
    }
    
    public ExtendableCommonCommand(String argument, String extendedArgument) {
        this.argument = new ExtendableArgument(argument, extendedArgument);
    }

    @Override
    public String stringifyOriginal() {
        return this.argument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.argument.getExtended();
    }
    
    public final ExtendableArgument argument() {
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
        final ExtendableCommonCommand other = ( ExtendableCommonCommand ) obj;
        if ( !Objects.equals(this.argument, other.argument) ) {
            return false;
        }
        return true;
    }
    
    
}
