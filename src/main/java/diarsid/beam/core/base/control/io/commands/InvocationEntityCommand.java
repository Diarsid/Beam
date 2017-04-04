/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import java.util.Objects;

import diarsid.beam.core.domain.entities.NamedEntityType;


public abstract class InvocationEntityCommand implements ExtendableCommand {
    
    private final ExtendableArgument argument;
    private boolean isNew;
    private boolean isSuccessfull;
    
    public InvocationEntityCommand(String argument) {
        this.argument = new ExtendableArgument(argument);
        this.isNew = true;
        this.isSuccessfull = false;
    }
    
    public InvocationEntityCommand(String argument, String extendedArgument) {
        this.argument = new ExtendableArgument(argument, extendedArgument);
        this.isNew = false;
        this.isSuccessfull = false;
    }
    
    public abstract NamedEntityType subjectedEntityType();
    
    @Override
    public String originalArgument() {
        return this.argument.original();
    }

    @Override
    public String extendedArgument() {
        return this.argument.extended();
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
        final InvocationEntityCommand other = ( InvocationEntityCommand ) obj;
        if ( !Objects.equals(this.argument, other.argument) ) {
            return false;
        }
        return true;
    }    

    @Override
    public boolean wasNotUsedBefore() {
        return this.isNew;
    }

    @Override
    public boolean wasUsedBeforeAndStored() {
        return ! this.isNew;
    }

    @Override
    public void setNew() {
        this.isNew = true;
    }

    @Override
    public void setStored() {
        this.isNew = false;
    }

    @Override
    public void setTargetFound() {
        this.isSuccessfull = true;
    }
    
    @Override
    public void setTargetNotFound() {
        this.isSuccessfull = false;
    }
    
    @Override
    public boolean isTargetFound() {
        return this.isSuccessfull;
    }
    
    @Override
    public boolean isTargetNotFound() {
        return ! this.isSuccessfull;
    }   
}
