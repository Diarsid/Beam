/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;

/**
 *
 * @author Diarsid
 */
public class BatchedCommand implements ConvertableToVariant {
    
    private final Batch enclosingBatch;
    private final ExtendableCommand command;
    private final int orderInBatch;
    
    public BatchedCommand(Batch batch, int orderInBatch, ExtendableCommand command) {
        this.enclosingBatch = batch;
        this.command = command;
        this.orderInBatch = orderInBatch;
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return this.command.toVariant(variantIndex);
    }
    
    public int orderInBatch() {
        return this.orderInBatch;
    }

    public Batch batch() {
        return this.enclosingBatch;
    }

    public ExtendableCommand unwrap() {
        return this.command;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.enclosingBatch.name());
        hash = 61 * hash + Objects.hashCode(this.command);
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
        final BatchedCommand other = ( BatchedCommand ) obj;
        if ( !Objects.equals(this.enclosingBatch.name(), other.enclosingBatch.name()) ) {
            return false;
        }
        if ( !Objects.equals(this.enclosingBatch.batchedCommands().size(), other.enclosingBatch.batchedCommands().size()) ) {
            return false;
        }
        if ( !Objects.equals(this.command, other.command) ) {
            return false;
        }
        return true;
    }
}
