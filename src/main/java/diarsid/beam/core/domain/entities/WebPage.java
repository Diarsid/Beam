package diarsid.beam.core.domain.entities;

import java.io.Serializable;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.Integer.MIN_VALUE;

import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.NamedEntityType.WEBPAGE;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Diarsid
 */
public class WebPage 
        implements 
                NamedEntity, 
                Orderable, 
                Serializable, 
                ConvertableToVariant {    
    
    // page info
    private final String name;
    private final String shortcuts;
    private final String url;
    private int pageOrder;
    
    // directory info page belongs to
    private final WebPlace place;
    private final String directoryName;    
    private final int directoryOrder;
    
        
    WebPage(
            String name, 
            String shortcuts,             
            String url,
            int pageOrder,
            WebPlace place,
            String directoryName,
            int directoryOrder) {        
        this.name = name;
        this.shortcuts = shortcuts;
        this.url = url;
        this.place = place;
        this.directoryName = directoryName;
        this.pageOrder = pageOrder;
        this.directoryOrder = directoryOrder;
    }
    
    WebPage(
            String name, 
            String shortcuts,             
            String url,
            WebPlace place,
            String directoryName) {        
        this.name = name;
        this.shortcuts = shortcuts;
        this.url = url;
        this.place = place;
        this.directoryName = directoryName;
        this.pageOrder = MIN_VALUE;
        this.directoryOrder = MIN_VALUE;
    }

    @Override
    public String name() {
        return this.name;
    }
    
    @Override
    public NamedEntityType entityType() {
        return WEBPAGE;
    }
    
    @Override
    public int order() {
        return this.pageOrder;
    }
    
    @Override
    public void setOrder(int newOrder) {
        this.pageOrder = newOrder;
    }

    @Override
    public WebPlace place() {
        return this.place;
    }
    
    public WebDirectory directory() {
        return new WebDirectory(this.directoryName, this.place, this.directoryOrder);
    }
    
    public boolean isConsistent() {
        return  
                this.pageOrder != MIN_VALUE && 
                this.directoryOrder != MIN_VALUE;
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(
                this.name, 
                this.name + " :: " + this.directoryName + " :: " + lower(this.place.name()), 
                variantIndex);        
    }
    
    public String shortcuts() {
        return this.shortcuts;
    }

    public String url() {
        return this.url;
    }

    public String directoryName() {
        return this.directoryName;
    }
    
    public int directoryOrder() {
        return this.directoryOrder;
    }
    
    public void incrementPageOrder() {
        this.pageOrder++;
    }
    
    public void decrementPageOrder() {
        this.pageOrder--;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.shortcuts);
        hash = 17 * hash + Objects.hashCode(this.url);
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
        final WebPage other = ( WebPage ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( !Objects.equals(this.shortcuts, other.shortcuts) ) {
            return false;
        }
        if ( !Objects.equals(this.url, other.url) ) {
            return false;
        }
        return true;
    }
    
}
