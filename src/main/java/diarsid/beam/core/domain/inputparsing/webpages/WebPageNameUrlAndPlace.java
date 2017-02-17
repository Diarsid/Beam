/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.webpages;

import java.util.Objects;

import diarsid.beam.core.domain.entities.WebPlace;

import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
public class WebPageNameUrlAndPlace {
    
    private String name;
    private String url;
    private WebPlace place;
    private boolean rejected;

    WebPageNameUrlAndPlace(String name, String url, WebPlace place) {
        this.name = name;
        this.url = url;
        this.place = place;
        this.rejected = false;
    }
    
    public boolean isActual() {
        return ! this.rejected;
    }
    
    public boolean isRejected() {
        return this.rejected;
    }
    
    public void ifTrueReject(boolean condition) {
        this.rejected = condition;
    }
    
    public boolean hasName() {
        return ! this.name.isEmpty();
    }
    
    public boolean hasNotName() {
        return this.name.isEmpty();
    }
    
    public boolean hasUrl() {
        return ! this.url.isEmpty();
    }
    
    public boolean hasNotUrl() {
        return this.url.isEmpty();
    }
    
    public boolean hasPlace() {
        return this.place != null;
    }
    
    public boolean hasNotPlace() {
        return this.place == null;
    }

    public String getName() {
        return this.name;
    }

    public void ifTrueSetName(boolean condition, String name) {
        if ( condition ) {
            this.name = name;
        }        
    }

    public String getUrl() {
        return this.url;
    }

    public void ifTrueSetUrl(boolean condition, String url) {
        if ( condition ) {
            this.url = url;
        }            
    }

    public WebPlace getPlace() {
        return this.place;
    }

    public void ifTrueSetPlaceOf(boolean condition, String place) {
        if ( condition ) {
            this.place = parsePlace(place);
        }    
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
