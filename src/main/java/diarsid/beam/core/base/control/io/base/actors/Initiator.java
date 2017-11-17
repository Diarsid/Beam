/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.actors;

import java.io.Serializable;
import java.util.Objects;

import static diarsid.beam.core.base.util.StringUtils.randomString;

/**
 *
 * @author Diarsid
 */
public final class Initiator implements Serializable {
    
    private final String id;
    private final int engineNumber;
    private final OuterIoEngineType outerIoEngineType;
    
    public Initiator(int engineNumber, OuterIoEngineType outerIoEngineType) {
        this.id = randomString(7);
        this.engineNumber = engineNumber;
        this.outerIoEngineType = outerIoEngineType;
    }
    
    public OuterIoEngineType outerIoEngineType() {
        return this.outerIoEngineType;
    }
    
    public int engineNumber() {
        return this.engineNumber;
    }
    
    public String identity() {
        return this.id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + this.engineNumber;
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
        if ( this.engineNumber != other.engineNumber ) {
            return false;
        }
        if ( !Objects.equals(this.id, other.id) ) {
            return false;
        }
        return true;
    }
}
