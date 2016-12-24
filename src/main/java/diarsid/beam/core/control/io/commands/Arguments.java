/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.control.io.commands.exceptions.ArgumentsParsingException;
import diarsid.beam.core.control.io.commands.exceptions.ParsingSingleArgumentFromMultipleException;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Diarsid
 */
public class Arguments {    
    
    private static final String STRINGIFY_PATTERN;    
    private static final String ARGUMENT_STRINGIFY_SEPARATOR;    
    
    static {
        STRINGIFY_PATTERN = "{x}";
        ARGUMENT_STRINGIFY_SEPARATOR = ";"; 
    }
    
    
    private Arguments() {
    }
    
        
    public static Argument parseSingleFrom(String stringified) {
        singleArgumentOnly(stringified);
        return new Argument(stringified.replace("{", "").replace("}", ""));
    }

    private static void singleArgumentOnly(String stringified) 
            throws ParsingSingleArgumentFromMultipleException {
        if ( isMultiArgument(stringified) ) {
            throw new ParsingSingleArgumentFromMultipleException();
        }
    }
    
    public static Argument parseSingleFrom(String stringifiedOriginal, String stringifiedExtended) {
        singleArgumentOnly(stringifiedOriginal);
        singleArgumentOnly(stringifiedExtended);
        return new Argument(
                stringifiedOriginal.replace("{", "").replace("}", ""), 
                stringifiedExtended.replace("{", "").replace("}", ""));
    }
    
    public static String stringifyOriginalsOf(Argument... args) {
        return stream(args)
                .map(arg -> arg.getOriginal())
                .collect(joining(ARGUMENT_STRINGIFY_SEPARATOR));
    }
    
    public static String stringifyExtendedsOf(Argument... args) {
        return stream(args)
                .map(arg -> arg.getExtended())
                .collect(joining(ARGUMENT_STRINGIFY_SEPARATOR));
    }
    
    public static boolean isMultiArgument(String perhapsJoinedArgs) {
        return perhapsJoinedArgs.contains(ARGUMENT_STRINGIFY_SEPARATOR);
    }
    
    public static List<Argument> parseMultipleFrom(String joinedArgs) {
        return stream(joinedArgs.split(";"))
                .map(stringified -> parseSingleFrom(stringified))
                .collect(toList());
    }
    
    public static List<Argument> parseMultipleFrom(
            String joinedOriginalArgs, String joinedExtendedArgs) {
        String[] originals = joinedOriginalArgs.split(ARGUMENT_STRINGIFY_SEPARATOR);
        String[] extendeds = joinedExtendedArgs.split(ARGUMENT_STRINGIFY_SEPARATOR);
        if ( originals.length != extendeds.length ) {
            throw new ArgumentsParsingException();
        }
        List<Argument> args = new ArrayList<>();
        for (int i = 0; i < originals.length; i++) {
            args.add(parseSingleFrom(originals[i], extendeds[i]));
        }
        return args;
    }
}
