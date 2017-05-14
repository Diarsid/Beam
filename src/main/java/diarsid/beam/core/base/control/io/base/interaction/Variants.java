/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Diarsid
 */
public class Variants {
    
    private Variants() {        
    }
    
    public static List<Variant> commandsToVariants(List<InvocationCommand> commands) {
        AtomicInteger counter = new AtomicInteger(0);
        return commands
                .stream()
                .map(command -> command.toVariant(counter.getAndIncrement()))
                .collect(toList());
    }
    
    public static List<Variant> stringsToVariants(List<String> variantStrings) {
        AtomicInteger counter = new AtomicInteger(0);
        return variantStrings
                .stream()
                .map(string -> new Variant(string, counter.getAndIncrement()))
                .collect(toList());
    }
    
    public static List<Variant> entitiesToVariants(List<? extends NamedEntity> entites) {
        AtomicInteger counter = new AtomicInteger(0);
        return entites
                .stream()
                .map(entity -> entity.toVariant(counter.getAndIncrement()))
                .collect(toList());
    }
}
