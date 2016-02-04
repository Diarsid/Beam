/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.entities;

import java.io.Serializable;

/**
 *
 * @author Diarsid
 */
public enum WebPagePlacement implements Serializable {
    
    BOOKMARKS ("bookmarks"),
    WEBPANEL ("webpanel");
    
    private final String place;
    
    private WebPagePlacement(String place) {    
        this.place = place;
    }    
}
