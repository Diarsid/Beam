/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.STORED;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_NOT_FOUND;


public abstract class InvocationCommand implements ExecutorCommand {
    
    private final ExtendableArgument argument;
    private InvocationCommandLifePhase lifePhase;
    private InvocationCommandTargetState targetState;
    
    InvocationCommand(String argument) {
        this.argument = new ExtendableArgument(argument);
        this.lifePhase = NEW;
        this.targetState = TARGET_NOT_FOUND;
    }
    
    InvocationCommand(
            String argument, 
            String extendedArgument, 
            InvocationCommandLifePhase lifePhase, 
            InvocationCommandTargetState targetState) {
        this.argument = new ExtendableArgument(argument, extendedArgument);
        this.lifePhase = lifePhase;
        this.targetState = targetState;
    }
    
    public abstract NamedEntityType subjectedEntityType();
    
    @Override
    public final boolean isInvocation() {
        return true;
    }    
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.bestArgument(), this.stringify(), variantIndex);
    }
    
    public Variant toVariantHidingCommandWord(int variantIndex) {
        return new Variant(this.bestArgument(), this.bestArgument(), variantIndex);
    }
    
    @Override
    public final Message toMessage() {
        return info(this.toMessageString());
    }
    
    public final String toMessageString() {
        return format("%s -> %s", this.argument.original(), this.stringify());
    }
    
    @Override
    public String originalArgument() {
        return this.argument.original();
    }

    public String extendedArgument() {
        return this.argument.extended();
    }
    
    public boolean isExtendedArgument(String other) {
        return this.argument.extended().equalsIgnoreCase(other);
    }
    
    public String bestArgument() {
        return this.argument.isExtended() ? this.argument.extended() : this.argument.original();
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
        final InvocationCommand other = ( InvocationCommand ) obj;
        if ( !Objects.equals(this.argument, other.argument) ) {
            return false;
        }
        return true;
    }    

    public boolean wasNotUsedBefore() {
        return this.lifePhase.equals(NEW);
    }

    public boolean wasUsedBeforeAndStored() {
        return this.lifePhase.equals(STORED);
    }

    public InvocationCommand setNew() {
        this.lifePhase = NEW;
        return this;
    }

    public InvocationCommand setStored() {
        this.lifePhase = STORED;
        return this;
    }

    public InvocationCommand setTargetFound() {
        this.targetState = TARGET_FOUND;
        return this;
    }
    
    public InvocationCommand setTargetNotFound() {
        this.targetState = TARGET_NOT_FOUND;
        return this;
    }
    
    public boolean isTargetFound() {
        return this.targetState.equals(TARGET_FOUND);
    }
    
    public boolean isTargetNotFound() {
        return this.targetState.equals(TARGET_NOT_FOUND);
    }          

    @Override
    public String toString() {
        return format("%s[%s:%s]", 
                this.getClass().getSimpleName(),
                this.argument().original(), 
                this.argument().extended());
    }
}
