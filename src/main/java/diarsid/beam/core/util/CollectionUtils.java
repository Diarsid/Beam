/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 *
 * @author Diarsid
 */
public class CollectionUtils {
    
    private CollectionUtils() {
    }
    
    public static <T> Set<T> toSet(T[] array) {
        return new HashSet<>(asList(array));
    }
    
    public static <T> Set<T> toUnmodifiableSet(T[] array) {
        return unmodifiableSet(new HashSet<>(asList(array)));
    }
    
    public static <T> List<T> toUnmodifiableList(T... array) {
        return unmodifiableList(asList(array));
    }
}
