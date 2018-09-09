/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

/**
 *
 * @author Diarsid
 */
public enum CachedSimilarity {
    
    CACHED_PAIR_SIMILAR (true, true),
    CACHED_PAIR_NOT_SIMILAR (true, false),
    CACHED_PAIR_NOT_FOUND (false, false);
    
    private final boolean found;
    private final boolean isSimilar;

    private CachedSimilarity(boolean found, boolean isSimilar) {
        this.found = found;
        this.isSimilar = isSimilar;
    }

    public boolean isFound() {
        return this.found;
    }
    
    public boolean isNotFound() {
        return ! this.found;
    }

    public boolean isSimilar() {
        return this.isSimilar;
    }
    
    public boolean isNotSimilar() {
        return ! this.isSimilar;
    }
}
