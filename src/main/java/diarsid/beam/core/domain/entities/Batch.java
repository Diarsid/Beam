/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToMessage;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.infoWithHeader;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;

/**
 *
 * @author Diarsid
 */
public class Batch 
        implements 
                NamedEntity, 
                ConvertableToMessage {
    
    private final String name;
    private final List<BatchedCommand> commands;
    
    public Batch(String name, List<ExecutorCommand> commands) {
        this.name = name;
        AtomicInteger counter = new AtomicInteger(0);
        this.commands = commands
                .stream()
                .filter(command -> command.type().isNot(CALL_BATCH))
                .map(command -> new BatchedCommand(this, counter.getAndIncrement(), command))
                .collect(toList());
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public NamedEntityType type() {
        return BATCH;
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.name, format("%s (%s)", this.name, "Batch"), variantIndex);
    }
    
    @Override
    public Message toMessage() {
        AtomicInteger counter = new AtomicInteger(1);
        List<String> batchText = this.stringifyCommands()
                .stream()
                .map(command -> format(" %d) %s", counter.getAndIncrement(), command))
                .collect(toList());
        return infoWithHeader(this.name, batchText);
    }
    
    public int getCommandsQty() {
        return this.commands.size();
    }

    public List<BatchedCommand> batchedCommands() {
        return this.commands;
    }
    
    public List<String> stringifyCommands() {
        return this.commands
                        .stream()
                        .map(batchedCommand -> batchedCommand.unwrap().stringify())
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
    
    @Override
    public String toString() {
        return format("%s (%s)", this.name, this.type().displayName());
    }
}
