/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import java.util.Objects;

import diarsid.beam.core.domain.entities.NamedEntityType;


public abstract class EntityInvocationCommand implements ExtendableCommand {
    
    private final ExtendableArgument argument;
    private boolean isNew;
    private boolean isSuccessfull;
    
    public EntityInvocationCommand(String argument) {
        this.argument = new ExtendableArgument(argument);
        this.isNew = true;
        this.isSuccessfull = false;
    }
    
    public EntityInvocationCommand(String argument, String extendedArgument) {
        this.argument = new ExtendableArgument(argument, extendedArgument);
        this.isNew = true;
        this.isSuccessfull = true;
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
        final EntityInvocationCommand other = ( EntityInvocationCommand ) obj;
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
    public ExtendableCommand setNew() {
        this.isNew = true;
        return this;
    }

    @Override
    public ExtendableCommand setStored() {
        this.isNew = false;
        return this;
    }

    @Override
    public ExtendableCommand setTargetFound() {
        this.isSuccessfull = true;
        return this;
    }
    
    @Override
    public ExtendableCommand setTargetNotFound() {
        this.isSuccessfull = false;
        return this;
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
