/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import diarsid.beam.core.modules.data.DaoPersistableCacheData;

import static java.util.concurrent.TimeUnit.SECONDS;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoPeriodically;

/**
 *
 * @author Diarsid
 */
public class PersistentAnalyzeCache<T> implements AnalyzeCache<T> {
    
    private final BiFunction<String, String, Long> hashFunction;
    private final InMemoryAnalyzeCache<T> inMemoryCache;
    private final DaoPersistableCacheData<T> daoPersistableCacheData;
    private final int algorithmVersion;
    private final List<PersistableCacheData<T>> buffer;

    public PersistentAnalyzeCache(
            DaoPersistableCacheData<T> daoPersistableCacheData, 
            BiFunction<String, String, Long> hashFunction, 
            int algorithmVersion) {
        this.hashFunction = hashFunction;
        this.inMemoryCache = new InMemoryAnalyzeCache<>(hashFunction);
        this.daoPersistableCacheData = daoPersistableCacheData;        
        this.algorithmVersion = algorithmVersion;
        this.buffer = new ArrayList<>();
        asyncDoPeriodically(
                "persistent cacheable cache saving", 
                () -> {
                    synchronized ( this.buffer ) {
                        if ( nonEmpty(this.buffer) ) {
                            this.daoPersistableCacheData.persistAll(this.buffer, this.algorithmVersion);
                            this.buffer.clear();
                        }                        
                    }
                }, 
                10, 
                SECONDS);
    }

    @Override
    public T searchCachedFor(String target, String pattern) {
        return this.inMemoryCache.searchCachedFor(target, pattern);
    }

    @Override
    public void addToCache(String target, String pattern, T cacheable) {
        Long pairHash = this.hashFunction.apply(target, pattern);
        if ( this.inMemoryCache.notCached(pairHash) ) {
            this.inMemoryCache.addHashToCache(pairHash, cacheable);
            this.persist(target, pattern, pairHash, cacheable);
        }        
    }
    
    private void persist(String target, String pattern, Long pairHash, T cacheable) {
        synchronized ( this.buffer ) {
            this.buffer.add(new PersistableCacheData<>(target, pattern, pairHash, cacheable));
        }
    }
    
}
