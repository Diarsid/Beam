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
public class StringHolder {
    
    private String string;
    
    public StringHolder() {
        this.string = "";
    }

    public String get() {
        return this.string;
    }

    public void set(String string) {
        this.string = string;
    }
    
    public boolean isEmpty() {
        return this.string.isEmpty();
    }
    
    public boolean isNotEmpty() {
        return ! this.string.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.string);
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
        final StringHolder other = ( StringHolder ) obj;
        if ( !Objects.equals(this.string, other.string) ) {
            return false;
        }
        return true;
    }
}
