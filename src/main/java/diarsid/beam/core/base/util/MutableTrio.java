/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class MutableTrio<FIRST, SECOND, THIRD> {
    
    private FIRST first;
    private SECOND second;
    private THIRD third;

    public MutableTrio() {
    }

    public MutableTrio(FIRST first, SECOND second, THIRD third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public FIRST first() {
        return this.first;
    }

    public SECOND second() {
        return this.second;
    }

    public THIRD third() {
        return this.third;
    }

    public MutableTrio<FIRST, SECOND, THIRD> setFirst(FIRST first) {
        this.first = first;
        return this;
    }

    public MutableTrio<FIRST, SECOND, THIRD> setSecond(SECOND second) {
        this.second = second;
        return this;
    }

    public MutableTrio<FIRST, SECOND, THIRD> setThird(THIRD third) {
        this.third = third;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.first);
        hash = 53 * hash + Objects.hashCode(this.second);
        hash = 53 * hash + Objects.hashCode(this.third);
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
        final MutableTrio<?, ?, ?> other = ( MutableTrio<?, ?, ?> ) obj;
        if ( !Objects.equals(this.first, other.first) ) {
            return false;
        }
        if ( !Objects.equals(this.second, other.second) ) {
            return false;
        }
        if ( !Objects.equals(this.third, other.third) ) {
            return false;
        }
        return true;
    }
    
}
