/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;
import diarsid.beam.core.base.control.io.base.interaction.CallbackEvent;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.analyze.variantsweight.Variant;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.util.PathUtils.extractTargetFromPath;
import static diarsid.beam.core.base.util.PathUtils.joinPathFrom;
import static diarsid.beam.core.base.util.PathUtils.joinToPathFrom;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class LocationSubPath extends Location {
    
    private final String pattern;
    private final String subPath;

    public LocationSubPath(
            String pattern, String locationName, String locationPath, String subPath) {
        super(locationName, locationPath);
        this.pattern = pattern;
        if ( lower(subPath).startsWith(lower(locationName)) ) {
            subPath = extractTargetFromPath(subPath);
        }    
        this.subPath = subPath;
    } 
    
    @Override
    public boolean hasSubPath() {
        return true;
    }    
    
    @Override
    public String name() {
        return joinToPathFrom(super.name(), this.subPath);
    }
    
    @Override
    public String path() {
        return joinToPathFrom(super.path(), this.subPath);
    }

    @Override
    public Message toMessage() {
        return info(asList(this.name(), "  path: " + this.path()));
    }
    
    @Override
    public void openAsync(
            String target, 
            CallbackEmpty successCallback,
            CallbackEvent failCallback) {
        super.openAsync(joinToPathFrom(this.subPath, target), successCallback, failCallback);
    }
    
    @Override
    public void openAsync(
            CallbackEmpty successCallback,
            CallbackEvent failCallback) {
        super.openAsync(this.subPath, successCallback, failCallback);
    }
    
    @Override
    public boolean has(String target) {
        return Files.exists(joinPathFrom(super.path(), this.subPath, target));
    }   
    
    @Override
    public String relativePathTo(String target) {
        return joinToPathFrom(super.name(), this.subPath, target);
    }
    
    public String pattern() {
        return this.pattern;
    }
    
    public String variantDisplayName() {
        return format("'%s' is %s/%s", 
                this.pattern, super.name(), this.subPath);
    }

    public String subPath() {
        return this.subPath;
    }
    
    public String fullName() {
        return joinToPathFrom(super.name(), this.subPath);
    }
    
    public String fullPath() {
        return joinToPathFrom(super.path(), this.subPath);
    }
    
    public Path realPath() {
        return joinPathFrom(super.path(), this.subPath);
    }
    
    public boolean pointsToDirectory() {
        return pathIsDirectory(joinPathFrom(super.path(), this.subPath));
    }

    public boolean notPointsToDirectory() {
        return ! pathIsDirectory(joinPathFrom(super.path(), this.subPath));
    }    
    
    @Override
    public Variant toVariant(int variantIndex) {
        String combinedPath = joinToPathFrom(super.name(), this.subPath);
        return new Variant(
                combinedPath, 
                this.variantDisplayName(), 
                variantIndex);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(super.name());
        hash = 53 * hash + Objects.hashCode(super.path());
        hash = 53 * hash + Objects.hashCode(this.subPath);
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
        final LocationSubPath other = ( LocationSubPath ) obj;
        if ( !Objects.equals(this.name(), other.name()) ) {
            return false;
        }
        if ( !Objects.equals(this.path(), other.path()) ) {
            return false;
        }
        if ( !Objects.equals(this.subPath, other.subPath) ) {
            return false;
        }
        return true;
    }
    
}
