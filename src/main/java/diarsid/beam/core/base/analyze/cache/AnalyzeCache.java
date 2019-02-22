/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.cache;

import java.util.function.BiFunction;

import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public interface AnalyzeCache<T> {
    
    static BiFunction<String, String, Long> PAIR_HASH_FUNCTION = (target, pattern) -> {
        long hash = 5;
        hash = 97 * hash + lower(target).hashCode();
        hash = 97 * hash + lower(pattern).hashCode();
        return hash;
    };
    
    T searchNullableCachedFor(String pattern, String target);
    
    void addToCache(String pattern, String target, T cacheable);
    
}
