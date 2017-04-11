/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class Variant implements Serializable, Comparable<Variant> {
    
    private final String text;
    private final String displayText;
    private final int variantIndex;
    
    public Variant(String text, int variantIndex) {
        this.text = text;
        this.displayText = "";
        this.variantIndex = variantIndex;
    }
    
    public Variant(String text, String displayText, int variantIndex) {
        this.text = text;
        this.displayText = displayText;
        this.variantIndex = variantIndex;
    }
    
    public boolean hasDisplayText() {
        return ! this.displayText.isEmpty();
    }

    public String text() {
        return this.text;
    }

    public String getDisplayText() {
        return this.displayText;
    }
    
    public int index() {
        return this.variantIndex;
    }

    @Override
    public int compareTo(Variant other) {
        if ( this.variantIndex > other.variantIndex ) {
            return 1;
        } else if ( this.variantIndex < other.variantIndex ) {
            return -1;
        } else {
            return 0;
        }
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
