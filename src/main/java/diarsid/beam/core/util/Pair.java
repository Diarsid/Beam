/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class Pair <K, V> {
    
    private final K k;
    private final V v;
    
    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K first() {
        return this.k;
    }

    public V second() {
        return this.v;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.k);
        hash = 97 * hash + Objects.hashCode(this.v);
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
        final Pair<?, ?> other = ( Pair<?, ?> ) obj;
        if ( !Objects.equals(this.k, other.k) ) {
            return false;
        }
        if ( !Objects.equals(this.v, other.v) ) {
            return false;
        }
        return true;
    }
}
