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
public class WebPageNameUrlAndPlace {
    
    private final String name;
    private final String url;
    private final WebPlace place;

    WebPageNameUrlAndPlace(String name, String url, WebPlace place) {
        this.name = name;
        this.url = url;
        this.place = place;
    }

    public String name() {
        return this.name;
    }

    public String url() {
        return this.url;
    }

    public WebPlace place() {
        return this.place;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.url);
        hash = 97 * hash + Objects.hashCode(this.place);
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
        final WebPageNameUrlAndPlace other = ( WebPageNameUrlAndPlace ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( !Objects.equals(this.url, other.url) ) {
            return false;
        }
        if ( !Objects.equals(this.place, other.place) ) {
            return false;
        }
        return true;
    }
    
    
}
