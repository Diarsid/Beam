/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;


import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.modules.data.DaoSimilarityCache;

import static java.util.concurrent.TimeUnit.SECONDS;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoPeriodically;

/**
 *
 * @author Diarsid
 */
class PersistentSimilarityCache implements SimilarityCache {
    
    private final InMemorySimilarityCache inMemoryCache;
    private final DaoSimilarityCache daoSimilarityCache;
    private final int algorithmVersion;
    private final List<SimilarityData> buffer;

    public PersistentSimilarityCache(
            DaoSimilarityCache daoSimilarityCache, 
            InMemorySimilarityCache inMemoryCache, 
            int algorithmVersion) {
        this.inMemoryCache = inMemoryCache;
        this.daoSimilarityCache = daoSimilarityCache;        
        this.algorithmVersion = algorithmVersion;
        this.buffer = new ArrayList<>();
        asyncDoPeriodically(
                "persistent similarity cache saving", 
                () -> {
                    synchronized ( this.buffer ) {
                        if ( nonEmpty(this.buffer) ) {
                            this.daoSimilarityCache.persistAll(this.buffer, this.algorithmVersion);
                            this.buffer.clear();
                        }                        
                    }
                }, 
                10, 
                SECONDS);
    }

    @Override
    public CachedSimilarity searchSimilarityForPair(String target, String pattern) {
        return this.inMemoryCache.searchSimilarityForPair(target, pattern);
    }

    @Override
    public void addToCache(String target, String pattern, boolean similar) {
        this.inMemoryCache.addToCache(target, pattern, similar);
        this.persist(target, pattern, similar);
    }
    
    private void persist(String target, String pattern, boolean similar) {
        synchronized ( this.buffer ) {
            this.buffer.add(new SimilarityData(target, pattern, similar));
        }
    }
    
}
