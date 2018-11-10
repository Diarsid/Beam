/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class PersistableCacheData<T> {    
    
    private final String target;
    private final String pattern;
    private final long pairHash;
    private final T cacheable;

    PersistableCacheData(String target, String pattern, long pairHash, T cacheable) {
        this.target = target;
        this.pattern = pattern;
        this.pairHash = pairHash;
        this.cacheable = cacheable;
    }

    public String target() {
        return this.target;
    }

    public String pattern() {
        return this.pattern;
    }

    public T cacheable() {
        return this.cacheable;
    }
    
    public long hash() {
        return this.pairHash;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.target);
        hash = 31 * hash + Objects.hashCode(this.pattern);
        hash = 31 * hash + ( int ) (this.pairHash ^ (this.pairHash >>> 32));
        hash = 31 * hash + Objects.hashCode(this.cacheable);
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
        final PersistableCacheData<?> other = ( PersistableCacheData<?> ) obj;
        if ( this.pairHash != other.pairHash ) {
            return false;
        }
        if ( !Objects.equals(this.target, other.target) ) {
            return false;
        }
        if ( !Objects.equals(this.pattern, other.pattern) ) {
            return false;
        }
        if ( !Objects.equals(this.cacheable, other.cacheable) ) {
            return false;
        }
        return true;
    }
    
}
