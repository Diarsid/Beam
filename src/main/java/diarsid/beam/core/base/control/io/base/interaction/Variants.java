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

import static diarsid.beam.core.base.control.io.base.interaction.Variants.View.SHOW_VARIANT_TYPE;

/**
 *
 * @author Diarsid
 */
public class Variants {
    
    public static enum View {
        SHOW_VARIANT_TYPE,
        HIDE_VARIANT_TYPE
    }
    
    private Variants() {        
    }
    
    public static List<Variant> commandsToVariants(List<InvocationCommand> commands) {
        AtomicInteger counter = new AtomicInteger(0);
        return commands
                .stream()
                .map(command -> command.toVariant(counter.getAndIncrement()))
                .collect(toList());
    }
    
    public static List<Variant> commandsToVariants(
            List<InvocationCommand> commands, View conversion) {
        if ( conversion.equals(SHOW_VARIANT_TYPE) ) {
            return commandsToVariants(commands);
        } else {            
            AtomicInteger counter = new AtomicInteger(0);
            return commands
                    .stream()
                    .map(command -> command.toVariantHidingCommandWord(counter.getAndIncrement()))
                    .collect(toList());
        }
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
    
    public static List<Variant> toVariants(List<? extends ConvertableToVariant> convertables) {
        AtomicInteger counter = new AtomicInteger(0);
        return convertables
                .stream()
                .map(convertable -> convertable.toVariant(counter.getAndIncrement()))
                .collect(toList());
    }
}
