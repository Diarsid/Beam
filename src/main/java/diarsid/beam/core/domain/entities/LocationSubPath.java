/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.PathUtils.combineAsPathFrom;
import static diarsid.beam.core.base.util.PathUtils.combinePathFrom;
import static diarsid.beam.core.base.util.PathUtils.extractTargetFromPath;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class LocationSubPath implements ConvertableToVariant {
    
    private final String locationName;
    private final String locationPath;
    private final String subPath;

    public LocationSubPath(String locationName, String locationPath, String subPath) {
        this.locationName = locationName;
        this.locationPath = locationPath;
        if ( lower(subPath).startsWith(lower(locationName)) ) {
            subPath = extractTargetFromPath(subPath);
        }    
        this.subPath = subPath;
    }    
    
    public Location location() {
        return new Location(this.locationName, this.locationPath);
    }    

    public String locationName() {
        return this.locationName;
    }

    public String locationPath() {
        return this.locationPath;
    }

    public String subPath() {
        return this.subPath;
    }
    
    public boolean pointsToDirectory() {
        return pathIsDirectory(combinePathFrom(this.locationPath, this.subPath));
    }    
    
    @Override
    public Variant toVariant(int variantIndex) {
        String combinedPath = combineAsPathFrom(this.locationName, this.subPath);
        return new Variant(
                combinedPath, 
                format("%s (Location subpath)", combinedPath), 
                variantIndex);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.locationName);
        hash = 53 * hash + Objects.hashCode(this.locationPath);
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
        if ( !Objects.equals(this.locationName, other.locationName) ) {
            return false;
        }
        if ( !Objects.equals(this.locationPath, other.locationPath) ) {
            return false;
        }
        if ( !Objects.equals(this.subPath, other.subPath) ) {
            return false;
        }
        return true;
    }
    
}
