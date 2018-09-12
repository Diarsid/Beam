/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import java.util.Objects;


/**
 *
 * @author Diarsid
 */
public class SimilarityData {
    
    private final String target;
    private final String pattern;
    private final long pairHash;
    private final boolean isSimilar;

    public SimilarityData(String target, String pattern, Long hash, boolean isSimilar) {
        this.target = target;
        this.pattern = pattern;
        this.pairHash = hash;
        this.isSimilar = isSimilar;
    }

    public String target() {
        return this.target;
    }

    public String pattern() {
        return this.pattern;
    }

    public boolean isSimilar() {
        return this.isSimilar;
    }
    
    public long hash() {
        return this.pairHash;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.target);
        hash = 89 * hash + Objects.hashCode(this.pattern);
        hash = 89 * hash + ( int ) (this.pairHash ^ (this.pairHash >>> 32));
        hash = 89 * hash + (this.isSimilar ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final SimilarityData other = ( SimilarityData ) obj;
        if ( this.pairHash != other.pairHash ) {
            return false;
        }
        if ( this.isSimilar != other.isSimilar ) {
            return false;
        }
        if ( !Objects.equals(this.target, other.target) ) {
            return false;
        }
        if ( !Objects.equals(this.pattern, other.pattern) ) {
            return false;
        }
        return true;
    }    
    
}
