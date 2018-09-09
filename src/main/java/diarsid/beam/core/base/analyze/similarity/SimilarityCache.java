/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import java.util.function.BiFunction;

/**
 *
 * @author Diarsid
 */
interface SimilarityCache {
    
    static BiFunction<String, String, Long> SIMILARITY_PAIR_HASH = (target, pattern) -> {
        long hash = 5;
        hash = 97 * hash + target.hashCode();
        hash = 97 * hash + pattern.hashCode();
        return hash;
    };
    
    CachedSimilarity searchSimilarityForPair(String target, String pattern);
    
    void addToCache(String target, String pattern, boolean similar);
}
