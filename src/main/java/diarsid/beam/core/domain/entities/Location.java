/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;

/**
 *
 * @author Diarsid
 */
public class Location 
        implements 
                NamedEntity, 
                ConvertableToVariant,
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
    public Variant toVariant(int variantIndex) {
        return new Variant(this.name, variantIndex);
    }

    @Override
    public NamedEntityType type() {
        return LOCATION;
    }
    
    public void openAsync(
            String target, 
            CallbackEmpty successCallback,
            CallbackEmpty exceptionCallback, 
            CallbackEmpty locationNotFoundCallback,
            CallbackEmpty targetNotFoundCallback) {
        asyncDo(() -> {
            File location = new File(this.path);
            File finalTarget = new File(this.path + "/" + target);
            if ( location.exists() && location.isDirectory() ) {
                if ( finalTarget.exists() ) {
                    try {
                        Desktop.getDesktop().open(location);   
                        successCallback.call();
                    } catch (IOException | IllegalArgumentException e) {
                        exceptionCallback.call();
                        logError(this.getClass(), e);
                    }
                } else {
                    targetNotFoundCallback.call();
                    debug(format("Target '%s' not found in %s.", target, this.name));
                }                
            } else {
                locationNotFoundCallback.call();
                debug(format("%s path '%s' does not exist.", this.name, this.path));
            }            
        });
    }
    
    public void openAsync(
            CallbackEvent successCallback,
            CallbackEvent failCallback) {
        asyncDo(() -> {
            File location = new File(this.path);
            if ( location.exists() && location.isDirectory() ) {
                try {
                    Desktop.getDesktop().open(location);   
                    successCallback.onEvent("...opening " + this.name);
                } catch (IOException | IllegalArgumentException e) {
                    failCallback.onEvent("cannot open location due to: " + e.getMessage());
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
    public String toString() {
        return this.name + " :: " + this.path;
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
}
