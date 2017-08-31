/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import java.io.Serializable;

import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public enum WebPlace implements Serializable {
    
    BOOKMARKS ("Bookmarks"),
    WEBPANEL ("WebPanel"),
    UNDEFINED_PLACE ("undefined");    
    
    private final String displayName;
    
    private WebPlace(String place) {    
        this.displayName = place;
    }   
    
    public String displayName() {
        return this.displayName;
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

    public static WebPlace parsePlace(String arg) {
        switch ( lower(arg) ) {
            case "webp":
            case "webpanel":
            case "panel": {
                return WEBPANEL;
            }
            case "bookm":
            case "bmark":
            case "bmarks":
            case "bookmarks":
            case "bookmark": {
                return BOOKMARKS;
            }
            default: {
                return UNDEFINED_PLACE;
            }
        }
    }
}
