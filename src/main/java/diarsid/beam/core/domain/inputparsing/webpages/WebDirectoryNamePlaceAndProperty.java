/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.webpages;

import java.util.Objects;

import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;

/**
 *
 * @author Diarsid
 */
public class WebDirectoryNamePlaceAndProperty {
    
    private final String name;
    private final WebPlace place;
    private final EntityProperty property;

    public WebDirectoryNamePlaceAndProperty(String name, WebPlace place, EntityProperty property) {
        this.name = name;
        this.place = place;
        this.property = property;
    }

    public String name() {
        return this.name;
    }

    public WebPlace place() {
        return this.place;
    }

    public EntityProperty property() {
        return this.property;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.place);
        hash = 29 * hash + Objects.hashCode(this.property);
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
        final WebDirectoryNamePlaceAndProperty other = ( WebDirectoryNamePlaceAndProperty ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( this.place != other.place ) {
            return false;
        }
        if ( this.property != other.property ) {
            return false;
        }
        return true;
    }
}
