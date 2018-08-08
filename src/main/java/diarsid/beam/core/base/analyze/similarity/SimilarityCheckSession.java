/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import java.util.HashMap;
import java.util.Map;

import diarsid.beam.core.base.objects.PooledReusable;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Logging.logFor;


/**
 *
 * @author Diarsid
 */
public class SimilarityCheckSession extends PooledReusable {
    
    static {
        PooledReusable.createPoolFor(
                SimilarityCheckSession.class, () -> new SimilarityCheckSession());
    }
    
    private final Map<Integer, Boolean> cachedSimilarResults;
    private int freshCount;
    private int usedCount;

    private SimilarityCheckSession() {
        this.cachedSimilarResults = new HashMap<>();
        logFor(this).info("[SIMILARITY SESSION] [START]");
    }

    @Override
    protected void clearForReuse() {
        this.close();
    }
    
    public boolean isSimilar(String target, String pattern) {
        Integer pairHash = pairHash(target, pattern);
        if ( this.cachedSimilarResults.containsKey(pairHash) ) {
            this.usedCount++;
            return this.cachedSimilarResults.get(pairHash);
        } else {
            this.freshCount++;
            boolean similar = Similarity.isSimilar(target, pattern);
            this.cachedSimilarResults.put(pairHash, similar);
            return similar;
        }    
    }
    
    public void close() {
        this.freshCount++;
        this.usedCount++;
        this.cachedSimilarResults.clear();
        logFor(this).info(format("[SIMILARITY SESSION] [END] fresh: %s, duplicates: %s", 
                                 this.freshCount, this.usedCount));
    }
    
    private static int pairHash(String s1, String s2) {
        return s1.hashCode() * 17 + s2.hashCode() * 31;
    }
    
}
