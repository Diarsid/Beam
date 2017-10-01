/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import java.io.Serializable;
import java.util.List;

import diarsid.beam.core.base.util.ParseableEnum;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static diarsid.beam.core.base.util.EnumUtils.argMatchesEnum;


/**
 *
 * @author Diarsid
 */
public enum WebPlace implements Serializable, ParseableEnum {
    
    BOOKMARKS (
            "Bookmarks", 
            asList("bookmarks", "bookm", "bkmarks", "bmarks", "marks")),
    WEBPANEL (
            "WebPanel", 
            asList("webpanel", "panel", "wpanel")),
    UNDEFINED_PLACE (
            "undefined", 
            emptyList());    
    
    private final String displayName;
    private final List<String> keyWords;
    
    private WebPlace(String place, List<String> keyWords) {    
        this.displayName = place;
        this.keyWords = keyWords;
    }   
    
    public String displayName() {
        return this.displayName;
    }
    
    @Override
    public List<String> keyWords() {
        return this.keyWords;
    }
    
    public boolean isUndefined() {
        return this.equals(UNDEFINED_PLACE);
    }
    
    public boolean isDefined() {
        return ! this.equals(UNDEFINED_PLACE);
    }
    
    public boolean is(WebPlace another) {
        if ( this.isUndefined() || another.isUndefined() ) {
            return false;
        }
        return this.equals(another);
    }

    // TODO MEDIUM add tests
    public static WebPlace parsePlace(String arg) {
        if ( argMatchesEnum(arg, WEBPANEL) ) {
            return WEBPANEL;
        } else if ( argMatchesEnum(arg, BOOKMARKS) ) {
            return BOOKMARKS;
        } else {
            return UNDEFINED_PLACE;
        }
    }
}
