/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.io.Serializable;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToMessage;
import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TextMessage;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.format;

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
                ConvertableToVariant,
                ConvertableToMessage {
    
    private final String name;
    private final WebPlace place;
    private int order;
    
    WebDirectory(String name, WebPlace place) {
        this.name = name;
        this.place = place;
        this.order = MIN_VALUE;
    }

    WebDirectory(String name, WebPlace place, int order) {
        this.name = name;
        this.place = place;
        this.order = order;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public NamedEntityType entityType() {
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
                this.name,
                this.name + " :: " + lower(this.place.name()), 
                variantIndex);
    }

    @Override
    public Message toMessage() {
        return new TextMessage(format(
                "%s (%d) %s", this.name, this.order, lower(this.place.name())));
    }
    
    public boolean isConsistent() {
        return this.order != MIN_VALUE ;
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        final WebDirectory other = ( WebDirectory ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( this.place != other.place ) {
            return false;
        }
        return true;
    }
}
