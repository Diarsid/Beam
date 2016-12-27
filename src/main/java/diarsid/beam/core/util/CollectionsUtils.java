/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class CollectionsUtils {
    
    private CollectionsUtils() {
    }
    
    public static boolean nonEmpty(Collection c) {
        return ( ! c.isEmpty() );
    }
    
    public static boolean nonNullNonEmpty(Collection c) {
        return ( 
                nonNull(c) && 
                ! c.isEmpty() );
    }
    
    public static boolean containsOne(Collection c) {
        return c.size() == 1;
    }
    
    public static <T> T getOne(Collection<T> c) {
        return c.stream()
                .findFirst()
                .orElseThrow(() -> new NullPointerException(
                        "Passed collection is implied to contain exactly one element."));
    }

    public static <T> Set<T> toSet(T[] array) {
        return new HashSet<>(asList(array));
    }

    public static <T> List<T> toUnmodifiableList(T... array) {
        return unmodifiableList(asList(array));
    }

    public static <T> Set<T> toUnmodifiableSet(T[] array) {
        return unmodifiableSet(new HashSet<>(asList(array)));
    }
}
