/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.indexOfLastPathSeparator;
import static diarsid.beam.core.domain.entities.NamedEntityType.PROGRAM;

/**
 *
 * @author Diarsid
 */
public class Program implements NamedEntity {
    
    private final ProgramsCatalog programsCatalog;
    private final String fullName;
    
    public Program(ProgramsCatalog programsCatalog, String fullName) {
        this.programsCatalog = programsCatalog;
        this.fullName = fullName;
    }
    
    public String simpleName() {
        if ( containsPathSeparator(this.fullName) ) {
            return this.fullName.substring(
                    indexOfLastPathSeparator(this.fullName) + 1, 
                    this.fullName.length());
        } else {
            return this.fullName;
        }        
    }
    
    public String subPath() {
        if ( containsPathSeparator(this.fullName) ) {
            return this.fullName.substring(0, indexOfLastPathSeparator(this.fullName));
        } else {
            return "";
        }
    }

    @Override
    public String name() {
        return this.fullName;
    }
    
    public void runAsync(CallbackEmpty successCallback, CallbackEvent failCallback) {
        asyncDo(() -> {
            File program = this.programsCatalog.asFile(this);
            if ( program.exists() && program.isFile() ) {
                try {
                    Desktop.getDesktop().open(program);  
                    successCallback.call();
                } catch (IOException | IllegalArgumentException e) {
                    failCallback.onEvent("..." + e.getMessage());
                    logError(this.getClass(), e);
                }
            } else {
                failCallback.onEvent("...program " + this.fullName + " not found in programs.");
            }            
        });
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(
                this.fullName, format("%s (%s)", this.fullName, "Program"), variantIndex);
    }

    @Override
    public NamedEntityType type() {
        return PROGRAM;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.programsCatalog);
        hash = 71 * hash + Objects.hashCode(this.fullName);
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
        final Program other = ( Program ) obj;
        if ( !Objects.equals(this.fullName, other.fullName) ) {
            return false;
        }
        if ( !Objects.equals(this.programsCatalog, other.programsCatalog) ) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return format("%s (%s)", this.fullName, this.type().displayName());
    }
    
}
