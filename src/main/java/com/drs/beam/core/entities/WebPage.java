package com.drs.beam.core.entities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author Diarsid
 */
public class WebPage implements Serializable {
    // Fields =============================================================================
    
    private final String name;
    private final String shortcuts;
    private final String urlAddress;
    private final WebPagePlacement placement;
    private final String directory;
    private final String browser;
    
    // Constructors =======================================================================
    
    public WebPage(
            String name, 
            String shortcuts,             
            String urlAddress, 
            WebPagePlacement placement,
            String directory, 
            String browser) {
        
        this.name = name;
        this.shortcuts = shortcuts;
        this.urlAddress = urlAddress;
        this.placement = placement;
        this.directory = directory;
        this.browser = browser;
    }

    public String getName() {
        return name;
    }

    public String getShortcuts() {
        return shortcuts;
    }

    public String getUrlAddress() {
        return urlAddress;
    }

    public WebPagePlacement getPlacement() {
        return placement;
    }

    public String getDirectory() {
        return directory;
    }

    public String getBrowser() {
        return browser;
    }
    
    public boolean useDefaultBrowser(){
        return "default".equals(this.browser);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.shortcuts);
        hash = 53 * hash + Objects.hashCode(this.urlAddress);
        hash = 53 * hash + Objects.hashCode(this.placement);
        hash = 53 * hash + Objects.hashCode(this.directory);
        hash = 53 * hash + Objects.hashCode(this.browser);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebPage other = (WebPage) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.shortcuts, other.shortcuts)) {
            return false;
        }
        if (!Objects.equals(this.urlAddress, other.urlAddress)) {
            return false;
        }
        if (this.placement != other.placement) {
            return false;
        }
        if (!Objects.equals(this.directory, other.directory)) {
            return false;
        }
        if (!Objects.equals(this.browser, other.browser)) {
            return false;
        }
        return true;
    }
}
