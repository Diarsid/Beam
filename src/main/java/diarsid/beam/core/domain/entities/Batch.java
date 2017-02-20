/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;

import diarsid.beam.core.base.control.io.commands.ExtendableCommand;

/**
 *
 * @author Diarsid
 */
public class Batch implements NamedEntity {
    
    private final String name;
    private final List<BatchedCommand> commands;
    
    public Batch(String name, List<ExtendableCommand> commands) {
        this.name = name;
        AtomicInteger counter = new AtomicInteger(0);
        this.commands = commands
                .stream()
                .filter(command -> command.type().isNot(CALL_BATCH))
                .map(command -> new BatchedCommand(this, counter.getAndIncrement(), command))
                .collect(toList());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public NamedEntityType getEntityType() {
        return BATCH;
    }
    
    public int getCommandsQty() {
        return this.commands.size();
    }

    public List<BatchedCommand> getCommands() {
        return this.commands;
    }
    
    public List<String> stringifyCommands() {
        return this.commands
                        .stream()
                        .map(batchedCommand -> batchedCommand.command().stringifyOriginal())
                        .collect(toList());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.name);
        hash = 11 * hash + Objects.hashCode(this.commands);
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
        final Batch other = ( Batch ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( !Objects.equals(this.commands, other.commands) ) {
            return false;
        }
        return true;
    }
}
