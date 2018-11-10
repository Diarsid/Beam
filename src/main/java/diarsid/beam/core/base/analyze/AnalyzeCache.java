/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze;

import java.util.function.BiFunction;

/**
 *
 * @author Diarsid
 */
public interface AnalyzeCache<T> {
    
    static BiFunction<String, String, Long> PAIR_HASH = (target, pattern) -> {
        long hash = 5;
        hash = 97 * hash + target.hashCode();
        hash = 97 * hash + pattern.hashCode();
        return hash;
    };
    
    T searchCachedFor(String pattern, String target);
    
    void addToCache(String pattern, String target, T cacheable);
}
