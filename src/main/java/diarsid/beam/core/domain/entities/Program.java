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
import diarsid.beam.core.base.control.io.base.interaction.ConvertableToMessage;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.infoWithHeader;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.indexOfLastPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.domain.entities.NamedEntityType.PROGRAM;

/**
 *
 * @author Diarsid
 */
public class Program 
        implements 
                NamedEntity, 
                ConvertableToMessage {
    
    private final ProgramsCatalog programsCatalog;
    private final String fileName;
    private final String name;
    
    public Program(String name, String fileName, ProgramsCatalog programsCatalog) {
        this.name = name;
        this.fileName = fileName;
        this.programsCatalog = programsCatalog;
    }
    
    public String simpleName() {
        if ( containsPathSeparator(this.name) ) {
            return this.name.substring(
                    indexOfLastPathSeparator(this.name) + 1, 
                    this.name.length());
        } else {
            return this.name;
        }        
    }
    
    public String subPath() {
        if ( containsPathSeparator(this.name) ) {
            return this.name.substring(0, indexOfLastPathSeparator(this.name));
        } else {
            return "";
        }
    }

    @Override
    public String name() {
        return this.name;
    }
    
    public void runAsync(CallbackEmpty successCallback, CallbackEvent failCallback) {
        asyncDo(() -> {
            File program = this.programsCatalog.path().resolve(this.fileName).toFile();
            if ( program.exists() && program.isFile() ) {
                try {
                    Desktop.getDesktop().open(program);  
                    successCallback.call();
                } catch (IOException | IllegalArgumentException e) {
                    failCallback.onEvent(format("...cannot execute '%s'", this.name));
                    logFor(this).error("Cannot execute file: " + program.toString(), e);
                }
            } else {
                failCallback.onEvent(
                        format("...program '%s' not found in programs.", this.fileName));
            }            
        });
    }

    @Override
    public Message toMessage() {
        return infoWithHeader(
                this.toString(), 
                "file " + this.fileName, 
                "path " + normalizeSeparators(this.programsCatalog.path().toString()));
    }   

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(
                this.name, format("%s (%s)", this.name, "Program"), variantIndex);
    }

    @Override
    public NamedEntityType type() {
        return PROGRAM;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.programsCatalog);
        hash = 71 * hash + Objects.hashCode(this.fileName);
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
        if ( !Objects.equals(this.fileName, other.fileName) ) {
            return false;
        }
        if ( !Objects.equals(this.programsCatalog, other.programsCatalog) ) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return format("%s (%s)", this.name, this.type().displayName());
    } 
}
