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
public class MutableString {
    
    private String string;
    
    private MutableString() {
        this.string = "";
    }
    
    private MutableString(String s) {
        this.string = s;
    }
    
    public static MutableString emptyMutableString() {
        return new MutableString();
    }
    
    public static MutableString mutableString(String s) {
        return new MutableString(s);
    }

    public String get() {
        return this.string;
    }
    
    public String getAndEmpty() {
        try {
            return this.string;
        } finally {
            this.string = "";
        }
    }

    public void muteTo(String string) {
        this.string = string;
    }
    
    public void empty() {
        this.string = "";
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
        final MutableString other = ( MutableString ) obj;
        if ( !Objects.equals(this.string, other.string) ) {
            return false;
        }
        return true;
    }
}
