/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

/**
 *
 * @author Diarsid
 */
public class HelpKey implements Help {
    
    private final int key;

    HelpKey(int key) {
        this.key = key;
    }

    @Override
    public boolean isInfo() {
        return false;
    }

    @Override
    public boolean isKey() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.key;
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
        final HelpKey other = ( HelpKey ) obj;
        if ( this.key != other.key ) {
            return false;
        }
        return true;
    }
}
