/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Diarsid
 */
public class CollectionsUtils {
    
    private CollectionsUtils() {
    }
    
    public static boolean nonEmpty(Collection c) {
        return nonNull(c) && ! c.isEmpty();
    }
    
    public static boolean nonEmpty(Map map) {
        return nonNull(map) && ! map.isEmpty();
    }
    
    public static boolean isNotEmpty(Collection c) {
        return nonNull(c) && ! c.isEmpty();
    }
    
    public static boolean isNotEmpty(Map map) {
        return nonNull(map) && ! map.isEmpty();
    }
    
    public static boolean hasAny(Collection c) {
        return ( ! c.isEmpty() );
    }
    
    public static boolean nonNullNonEmpty(Collection c) {
        return ( 
                nonNull(c) && 
                ! c.isEmpty() );
    }
    
    public static boolean hasOne(Collection c) {
        return c.size() == 1;
    }
    
    public static boolean hasMany(Collection c) {
        return c.size() > 1;
    }
    
    public static <T> T getOne(Collection<T> c) {
        if ( c.size() != 1 ) {
            throw new IllegalArgumentException(
                    "Passed collection is implied to contain exactly one element.");
        } 
        
        if ( c instanceof List ) {
            T t = ( (List<T>) c ).get(0);
            if ( isNull(t) ) {
                throw new IllegalArgumentException(
                        "Passed collection is implied to contain exactly one element on index 0.");
            } 
            return t;
        } else {
            return c.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Passed collection is implied to contain exactly one element."));
        }
    }
    
    public static <T> T sortAndGetFirstFrom(List<T> list, Comparator<T> comparatorT) {
        if ( nonEmpty(list) ) {
            sort(list, comparatorT);
            return list.get(0);
        } else {
            throw new IllegalArgumentException("cannot get first sorted from empty List.");
        }        
    }

    public static <T> Set<T> toSet(T... array) {
        return new HashSet<>(asList(array));
    }

    public static <T> List<T> toUnmodifiableList(T... array) {
        return unmodifiableList(asList(array));
    }
    
    public static <T> List<T> toUnmodifiableList(List<T> list, List<T>... lists) {
        List<T> joined = new ArrayList<>(list);
        stream(lists).forEach(streamedList -> joined.addAll(streamedList));
        return unmodifiableList(joined);
    }

    public static <T> Set<T> toUnmodifiableSet(T[] array) {
        return unmodifiableSet(new HashSet<>(asList(array)));
    }
    
    public static <T> List<T> arrayListOf(T... t) {
        return new ArrayList<>(asList(t));
    }
    
    public static <T> List<T> joinLists(List<T>... lists) {
        return stream(lists)
                .flatMap(list -> list.stream())
                .collect(toList());
    }
    
    public static <K, V> void mergeInMapWithArrayLists(Map<K, List<V>> map, K key, V value) {
        if ( map.containsKey(key) ) {
            map.get(key).add(value);
        } else {
            map.put(key, arrayListOf(value));
        }
    }
    
    public static <T> Optional<T> optionalGet(List<T> list, int i) {
        if ( i > -1 && i < list.size() ) {
            return Optional.of(list.get(i));
        } else {
            return Optional.empty();
        }
    }
    
    public static void shrink(List list, int size) {
        if ( list.size() > size ) {
            for (int i = size; i < list.size();) {
                list.remove(i);
            }
        } 
    } 
    
    public static int first(int[] ints) {
        return ints[0];
    }
    
    public static int last(int[] ints) {
        return ints[ints.length - 1];
    }
    
    public static <T> T last(T[] ts) {
        return ts[ts.length - 1];
    }
    
    public static <T> T last(List<T> list) {
        if ( list.isEmpty() ) {
            throw new IllegalArgumentException(
                    "Passed list is implied to contain at least one element.");
        }
        
        return list.get(list.size() - 1);
    }
    
    public static <T> T lastFrom(List<T> list) {
        return last(list);
    }
    
    public static boolean indexInRange(int index, Collection collection) {
        return ( index > -1 ) && ( index < collection.size() );
    }
    
    public static void removeLastFrom(List list) {
        if ( list.isEmpty() ) {
            return;
        }
        list.remove(list.size() - 1);
    }
}
