/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.ConvertableToMessage;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.DesktopUtil.openWithDesktop;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;
import static diarsid.beam.core.base.util.PathUtils.joinPathFrom;

/**
 *
 * @author Diarsid
 */
public class Location 
        implements 
                NamedEntity, 
                ConvertableToMessage, 
                Serializable {

    private final String name;
    private final String path;
    
    public Location(String name, String path) {
        this.name = name;
        this.path = path;
    }    
    
    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Message toMessage() {
        return info(asList(this.name, "  path: " + this.path));
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.name, format("%s (%s)", this.name, "Location"), variantIndex);
    }

    @Override
    public NamedEntityType type() {
        return LOCATION;
    }
    
    public void openAsync(
            String target, 
            CallbackEmpty successCallback,
            CallbackEvent failCallback) {
        asyncDo(() -> {
            File location = new File(this.path);
            File finalTarget = new File(this.path + "/" + target);
            if ( location.exists() && location.isDirectory() ) {
                if ( finalTarget.exists() ) {
                    try {
                        openWithDesktop(finalTarget);   
                        successCallback.call();
                    } catch (IOException | IllegalArgumentException e) {
                        failCallback.onEvent(format("cannot open '%s' in %s", target, this.name));
                        logError(this.getClass(), e);
                    }
                } else {
                    failCallback.onEvent("target not found.");
                    debug(format("Target '%s' not found in %s.", target, this.name));
                }                
            } else {
                failCallback.onEvent("location real place not found.");
                debug(format("%s path '%s' does not exist.", this.name, this.path));
            }            
        });
    }
    
    public boolean has(String target) {
        return Files.exists(joinPathFrom(this.path, target));
    }
    
    public void openAsync(
            CallbackEmpty successCallback,
            CallbackEvent failCallback) {
        asyncDo(() -> {
            File location = new File(this.path);
            if ( location.exists() && location.isDirectory() ) {
                try {
                    openWithDesktop(location);   
                    successCallback.call();
                } catch (IOException | IllegalArgumentException e) {
                    failCallback.onEvent(format("cannot open %s", this.name));
                    logError(this.getClass(), e);
                }
            } else {
                failCallback.onEvent("location real place not found.");
                debug(format("%s path '%s' does not exist.", this.name, this.path));
            }            
        });
    }

    public String path() {
        return this.path;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.path);
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
        final Location other = (Location) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return format("%s (%s)", this.name, this.type().displayName());
    }
}
