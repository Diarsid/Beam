package com.drs.beam.core.entities;

import java.io.Serializable;
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
    private final String urlAddress;
    private final String category;
    private final String browser;
    
    // Constructors =======================================================================
    
    public WebPage(String name, String urlAddress, String category, String browser) {
        this.name = name;
        this.urlAddress = urlAddress;
        this.category = category;
        this.browser = browser;
    }

    // Methods ============================================================================
    public String getName() {
        return name;
    }

    public String getUrlAddress() {
        return urlAddress;
    }

    public String getCategory() {
        return category;
    }

    public String getBrowser() {
        return browser;
    }
    
    public boolean useDefaultBrowser(){
        return "default".equals(this.browser);
    }

    @Override
    public String toString() {
        return "WebPage{" + "name=" + name + ", urlAddress=" + urlAddress + ", category=" + category + ", browser=" + browser + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.name);
        hash = 43 * hash + Objects.hashCode(this.urlAddress);
        hash = 43 * hash + Objects.hashCode(this.category);
        hash = 43 * hash + Objects.hashCode(this.browser);
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
        if (!Objects.equals(this.urlAddress, other.urlAddress)) {
            return false;
        }
        if (!Objects.equals(this.category, other.category)) {
            return false;
        }
        if (!Objects.equals(this.browser, other.browser)) {
            return false;
        }
        return true;
    }
}
