/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.cache;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.function.BiFunction;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPersistableCacheData;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoPeriodically;
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class PersistentAnalyzeCache<T> implements AnalyzeCache<T> {
    
    private final Initiator initiator;
    private final BiFunction<String, String, T> analyzeFunction;
    private final BiFunction<String, String, Long> hashFunction;
    private final InMemoryAnalyzeCache<T> inMemoryCache;
    private final int algorithmVersion;
    private final List<PersistableCacheData<T>> buffer;
    private ResponsiveDaoPersistableCacheData<T> dao;
    private ScheduledFuture execution;

    public PersistentAnalyzeCache(
            Initiator initiator,
            BiFunction<String, String, T> analyzeFunction,
            BiFunction<String, String, Long> hashFunction, 
            int algorithmVersion) {
        this.initiator = initiator;
        this.analyzeFunction = analyzeFunction;
        this.hashFunction = hashFunction;
        this.inMemoryCache = new InMemoryAnalyzeCache<>(hashFunction);
        this.algorithmVersion = algorithmVersion;
        this.buffer = new ArrayList<>();        
    }
    
    public void initPersistenceWith(ResponsiveDaoPersistableCacheData<T> dao) {
        Runnable persistCacheOperation = () -> {
            synchronized ( this.buffer ) {
                if ( nonEmpty(this.buffer) ) {
                    this.dao.persistAll(this.initiator, this.buffer, this.algorithmVersion);
                    this.buffer.clear();
                }                        
            }
        };
        
        synchronized ( this ) {
            if ( isNull(this.dao) ) {
                this.dao = dao;
                this.execution = asyncDoPeriodically(
                        "persistent cacheable cache saving", 
                        persistCacheOperation, 
                        10, 
                        SECONDS);
            }
        }
        
        Map<Long, T> loadedHashes = this.dao.loadAllHashesWith(this.initiator, this.algorithmVersion);
        this.inMemoryCache.addAll(loadedHashes);

        Map<Long, T> reassesedHashes = this.dao.reassessAllHashesOlderThan(
                this.initiator, this.algorithmVersion, this.analyzeFunction);
        if ( nonEmpty(reassesedHashes) ) {
            logFor(this).info(format(
                    "Reassesed %s cached %s for algorithm version %s", 
                    reassesedHashes.size(), 
                    reassesedHashes.values().iterator().next().getClass().getSimpleName(),
                    this.algorithmVersion));
        }
        this.inMemoryCache.addAll(reassesedHashes);
    }

    @Override
    public T searchNullableCachedFor(String target, String pattern) {
        return this.inMemoryCache.searchNullableCachedFor(target, pattern);
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
