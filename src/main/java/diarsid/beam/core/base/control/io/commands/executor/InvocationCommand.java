/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.STORED;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_NOT_FOUND;


public abstract class InvocationCommand implements ExecutorCommand {
    
    private final ExtendableArgument argument;
    private InvocationCommandLifePhase lifePhase;
    private InvocationCommandTargetState targetState;
    
    public InvocationCommand(String argument) {
        this.argument = new ExtendableArgument(argument);
        this.lifePhase = NEW;
        this.targetState = TARGET_NOT_FOUND;
    }
    
    public InvocationCommand(
            String argument, 
            String extendedArgument, 
            InvocationCommandLifePhase lifePhase, 
            InvocationCommandTargetState targetState) {
        this.argument = new ExtendableArgument(argument, extendedArgument);
        this.lifePhase = lifePhase;
        this.targetState = targetState;
    }
    
    public abstract NamedEntityType subjectedEntityType();
    
    public static List<Variant> toVariants(List<InvocationCommand> commands) {
        AtomicInteger counter = new AtomicInteger(0);
        return commands
                .stream()
                .map(command -> command.toVariant(counter.getAndIncrement()))
                .collect(toList());
    }
    
    @Override
    public boolean isInvocation() {
        return true;
    }    
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.bestArgument(), this.stringify(), variantIndex);
    }
    
    @Override
    public String originalArgument() {
        return this.argument.original();
    }

    public String extendedArgument() {
        return this.argument.extended();
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
}
