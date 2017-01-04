/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.Objects;

import diarsid.beam.core.control.io.commands.ArgumentedCommand;

/**
 *
 * @author Diarsid
 */
public class BatchedCommand {
    
    private final Batch enclosingBatch;
    private final ArgumentedCommand command;
    private final int orderInBatch;
    
    public BatchedCommand(Batch batch, int orderInBatch, ArgumentedCommand command) {
        this.enclosingBatch = batch;
        this.command = command;
        this.orderInBatch = orderInBatch;
    }
    
    public int orderInBatch() {
        return this.orderInBatch;
    }

    public Batch batch() {
        return this.enclosingBatch;
    }

    public ArgumentedCommand command() {
        return this.command;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.enclosingBatch.getName());
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
        if ( !Objects.equals(this.enclosingBatch.getName(), other.enclosingBatch.getName()) ) {
            return false;
        }
        if ( !Objects.equals(this.enclosingBatch.getCommands().size(), other.enclosingBatch.getCommands().size()) ) {
            return false;
        }
        if ( !Objects.equals(this.command, other.command) ) {
            return false;
        }
        return true;
    }
}
