/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.webpages;

import java.util.Objects;

import diarsid.beam.core.domain.entities.WebPlace;

/**
 *
 * @author Diarsid
 */
public class WebDirectoryNameAndPlace {
    
    private final String name;
    private final WebPlace place;  

    public WebDirectoryNameAndPlace(String name, WebPlace place) {
        this.name = name;
        this.place = place;
    }

    public String name() {
        return this.name;
    }

    public WebPlace place() {
        return this.place;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.place);
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
        final WebDirectoryNameAndPlace other = ( WebDirectoryNameAndPlace ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( this.place != other.place ) {
            return false;
        }
        return true;
    }
    
    
}
