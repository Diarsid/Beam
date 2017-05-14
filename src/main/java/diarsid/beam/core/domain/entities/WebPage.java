package diarsid.beam.core.domain.entities;

import java.awt.Desktop;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.ConvertableToMessage;
import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TextMessage;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.format;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.logError;
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
                ConvertableToVariant, 
                ConvertableToMessage,
                Comparable<WebPage> {    
    
    // page info
    private final String name;
    private final String shortcuts;
    private final String url;
    private int pageOrder;
    
    // directory info page belongs to
    private final int directoryId;   
        
    WebPage(
            String name, 
            String shortcuts,             
            String url,
            int pageOrder,
            int dirId) {        
        this.name = name;
        this.shortcuts = shortcuts;
        this.url = url;
        this.directoryId = dirId;
        this.pageOrder = pageOrder;
    }
    
    WebPage(
            String name, 
            String shortcuts,             
            String url,
            int dirId) {        
        this.name = name;
        this.shortcuts = shortcuts;
        this.url = url;
        this.directoryId = dirId;
        this.pageOrder = MIN_VALUE;
    }

    @Override
    public String name() {
        return this.name;
    }
    
    @Override
    public NamedEntityType type() {
        return WEBPAGE;
    }
    
    @Override
    public int order() {
        return this.pageOrder;
    }
    
    public void browseAsync(
            CallbackEmpty calbackOnSuccess, 
            CallbackEvent callbackOnFail) {
        asyncDo(() -> {
            try {
                Desktop.getDesktop().browse(new URI(this.url));
                calbackOnSuccess.call();
            } catch (URISyntaxException|IOException ex) {
                logError(this.getClass(), ex.getMessage());
                callbackOnFail.onEvent(ex.getMessage());
            } 
        });
    }
    
    public int directoryId() {
        return this.directoryId;
    }
    
    @Override
    public void setOrder(int newOrder) {
        this.pageOrder = newOrder;
    }
    
    public boolean isConsistent() {
        return this.pageOrder != MIN_VALUE;
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.name, format("%s (%s)", this.name, "WebPage"), variantIndex);        
    }

    @Override
    public Message toMessage() {
        List<String> message = new ArrayList<>();
        message.add(this.name);
        message.add("  url:   " + this.url);
        message.add("  alias: " + this.shortcuts);
        return new TextMessage(message);
    } 
    
    @Override
    public int compareTo(WebPage another) {
        if ( this.order() < another.order() ) {
            return -1;
        } else if ( this.order() > another.order() ) {
            return 1;
        } else {
            return 0;
        }
    }
    
    public String shortcuts() {
        return this.shortcuts;
    }

    public String url() {
        return this.url;
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
