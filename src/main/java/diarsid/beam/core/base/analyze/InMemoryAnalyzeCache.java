/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;



/**
 *
 * @author Diarsid
 */
public class InMemoryAnalyzeCache<T> implements AnalyzeCache<T> {
    
    private final BiFunction<String, String, Long> hashFunction;
    private final Map<Long, T> cache;
    private final Object cacheLock;

    public InMemoryAnalyzeCache(BiFunction<String, String, Long> hashFunction) {
        this.hashFunction = hashFunction;
        this.cache = new HashMap<>();
        this.cacheLock = new Object();
    }    
    
    @Override
    public T searchCachedFor(String target, String pattern) {
        Long pairHash = this.hashFunction.apply(target, pattern);
        
        synchronized ( this.cacheLock ) {
            return this.cache.get(pairHash);
        }
    }
    
    boolean notCached(Long pairHash) {
        return ! this.cache.containsKey(pairHash);
    }
    
    void addHashToCache(Long pairHash, T cacheable) {
        synchronized ( this.cacheLock ) {
            this.cache.put(pairHash, cacheable);
        }
    }
    
    @Override
    public void addToCache(String target, String pattern, T cacheable) {
        Long pairHash = this.hashFunction.apply(target, pattern);
        this.addHashToCache(pairHash, cacheable);
    }
    
    public void addAll(Map<Long, T> otherCache) {
        synchronized ( this.cacheLock ) {
            this.cache.putAll(otherCache);
        }
    }
}
