/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.starter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Diarsid
 */
class Flags {
    
    private static final Set<Flag> ALL_FLAGS;
    static {
        Set<Flag> flags = new HashSet<>();
        flags.addAll(asList(FlagLaunchable.values()));     
        ALL_FLAGS = unmodifiableSet(flags);
    }
    
    private Flags() {
    }
    
    public static Optional<Flag> flagOf(String possibleFlag) {        
        return ALL_FLAGS
                .stream()
                .filter(flag -> flag.text().equalsIgnoreCase(possibleFlag))
                .findFirst();
    }
    
    public static List<String> formatToPrintables(Flag[] flags) {
        int maxFlagTextLength = stream(flags)
                .mapToInt(flag -> flag.text().length())
                .max()
                .getAsInt();
        return stream(flags)
                .map(flag -> {
                    return format(
                            "    %-[L]s  %s".replace("[L]", String.valueOf(maxFlagTextLength)), 
                            flag.text(), 
                            flag.description());
                })
                .collect(toList());        
    }
}
