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
    
    BOOKMARKS ("bookmarks"),
    WEBPANEL ("webpanel"),
    UNDEFINED_PLACE ("undefined");    
    
    private final String place;
    
    private WebPlace(String place) {    
        this.place = place;
    }   
    
    public boolean isUndefined() {
        return this.equals(UNDEFINED_PLACE);
    }
    
    public boolean isDefined() {
        return ! this.equals(UNDEFINED_PLACE);
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
