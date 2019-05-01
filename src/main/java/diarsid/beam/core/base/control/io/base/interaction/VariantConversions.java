/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.View.SHOW_VARIANT_TYPE;

/**
 *
 * @author Diarsid
 */
public class VariantConversions {
    
    public static enum View {
        SHOW_VARIANT_TYPE,
        HIDE_VARIANT_TYPE
    }
    
    private VariantConversions() {        
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
    
    public static Variant stringToVariant(String s) {
        return new Variant(s, 0);
    }
    
    public static List<Variant> namedStringsToVariants(
            List<String> stringNames, 
            List<String> variantStrings) {
        if ( stringNames.size() != variantStrings.size() ) {
            throw new IllegalArgumentException(
                    "variant strings and string names differ in length!");
        }
        List<Variant> variants = new ArrayList<>();
        int size = variantStrings.size();
        String variantString;
        String stringName;
        for (int i = 0; i < size; i++) {
            stringName = stringNames.get(i);
            variantString = variantStrings.get(i);
            variants.add(new Variant(variantString, stringName, i));
        }
        return variants;
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
