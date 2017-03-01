/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.common;

import java.util.Objects;

import diarsid.beam.core.domain.entities.metadata.EntityProperty;

/**
 *
 * @author Diarsid
 */
public class PropertyAndText {
    
    private final EntityProperty property;
    private final String text;

    public PropertyAndText(EntityProperty property, String text) {
        this.property = property;
        this.text = text;
    }

    public EntityProperty property() {
        return this.property;
    }

    public String text() {
        return this.text;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.property);
        hash = 73 * hash + Objects.hashCode(this.text);
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
        final PropertyAndText other = ( PropertyAndText ) obj;
        if ( !Objects.equals(this.text, other.text) ) {
            return false;
        }
        if ( this.property != other.property ) {
            return false;
        }
        return true;
    }
}
