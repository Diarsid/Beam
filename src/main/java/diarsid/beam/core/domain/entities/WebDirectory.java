/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.Integer.MIN_VALUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.NamedEntityType.WEBDIRECTORY;

/**
 *
 * @author Diarsid
 */
public class WebDirectory 
        implements 
                NamedEntity,
                Orderable, 
                Serializable, 
                ConvertableToVariant {
    
    private final int id;
    private final String name;
    private final WebPlace place;
    private int order;
    private final List<WebPage> pages;
    
    WebDirectory(String name, WebPlace place) {
        this.id = MIN_VALUE;
        this.name = name;
        this.place = place;
        this.order = MIN_VALUE;
        this.pages = unmodifiableList(emptyList());
    }

    WebDirectory(int id, String name, WebPlace place, int order, List<WebPage> pages) {
        this.id = id;
        this.name = name;
        this.place = place;
        this.order = order;
        this.pages = unmodifiableList(pages);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public NamedEntityType getEntityType() {
        return WEBDIRECTORY;
    }

    @Override
    public int order() {
        return this.order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public WebPlace place() {
        return this.place;
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(
                String.valueOf(this.id), 
                this.name + " :: " + lower(this.place.name()), 
                variantIndex);
    }

    public int id() {
        return this.id;
    }

    public List<WebPage> pages() {
        return this.pages;
    }
    
    public boolean isConsistent() {
        return 
                this.order != MIN_VALUE && 
                this.id != MIN_VALUE;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.id;
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.place);
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
        final WebDirectory other = ( WebDirectory ) obj;
        if ( this.id != other.id ) {
            return false;
        }
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( this.place != other.place ) {
            return false;
        }
        return true;
    }
}
