/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;
import java.util.Objects;

import diarsid.beam.core.util.RandomHexadecimalStringGenerator;

/**
 *
 * @author Diarsid
 */
public final class Initiator implements Serializable {
    
    private final static RandomHexadecimalStringGenerator GENERATOR;
    
    static {
        GENERATOR = new RandomHexadecimalStringGenerator();
    }
    
    private final String id;
    
    public Initiator() {
        this.id = GENERATOR.randomString(7);
    }
    
    public String getId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.id);
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
        final Initiator other = ( Initiator ) obj;
        if ( !Objects.equals(this.id, other.id) ) {
            return false;
        }
        return true;
    }
}
