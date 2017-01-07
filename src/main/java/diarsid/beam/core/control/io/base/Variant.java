/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class Variant implements Serializable {
    
    private final String text;
    private final String displayText;
    
    public Variant(String text) {
        this.text = text;
        this.displayText = "";
    }
    
    public Variant(String text, String displayText) {
        this.text = text;
        this.displayText = displayText;
    }
    
    public boolean hasDisplayText() {
        return ! this.displayText.isEmpty();
    }

    public String getText() {
        return this.text;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.text);
        hash = 89 * hash + Objects.hashCode(this.displayText);
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
        final Variant other = ( Variant ) obj;
        if ( !Objects.equals(this.text, other.text) ) {
            return false;
        }
        if ( !Objects.equals(this.displayText, other.displayText) ) {
            return false;
        }
        return true;
    }
}
