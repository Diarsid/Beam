/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.analyze.similarity.CachedSimilarity.CACHED_PAIR_NOT_FOUND;
import static diarsid.beam.core.base.analyze.similarity.CachedSimilarity.CACHED_PAIR_NOT_SIMILAR;
import static diarsid.beam.core.base.analyze.similarity.CachedSimilarity.CACHED_PAIR_SIMILAR;

/**
 *
 * @author Diarsid
 */
class InMemorySimilarityCache implements SimilarityCache {
    
    private final Map<Long, Boolean> cache;
    private final Object cacheLock;

    InMemorySimilarityCache() {
        this.cache = new HashMap<>();
        this.cacheLock = new Object();
    }    
    
    @Override
    public CachedSimilarity searchSimilarityForPair(String target, String pattern) {
        Long pairHash = SIMILARITY_PAIR_HASH.apply(target, pattern);
        Boolean similar;
        
        synchronized ( this.cacheLock ) {
            similar = this.cache.get(pairHash);
        }
        
        CachedSimilarity result;
        if ( nonNull(similar) ) {
            result = similar ? CACHED_PAIR_SIMILAR : CACHED_PAIR_NOT_SIMILAR;
        } else {
            result = CACHED_PAIR_NOT_FOUND;
        }
        
        return result;
    }
    
    boolean notCached(Long pairHash) {
        return ! this.cache.containsKey(pairHash);
    }
    
    void addHashToCache(Long pairHash, boolean similar) {
        synchronized ( this.cacheLock ) {
            this.cache.put(pairHash, similar);
        }
    }
    
    @Override
    public void addToCache(String target, String pattern, boolean similar) {
        Long pairHash = SIMILARITY_PAIR_HASH.apply(target, pattern);
        this.addHashToCache(pairHash, similar);
    }
    
    void addAll(Map<Long, Boolean> otherCache) {
        synchronized ( this.cacheLock ) {
            this.cache.putAll(otherCache);
        }
    }
}
