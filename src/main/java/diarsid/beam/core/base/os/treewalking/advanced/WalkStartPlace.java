/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;

import diarsid.beam.core.application.environment.Catalog;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.support.objects.Possible;
import diarsid.support.objects.StatefulClearable;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Flows.voidFlowDone;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.util.PathUtils.joinToPath;
import static diarsid.beam.core.base.util.PathUtils.notExistsInFileSystem;
import static diarsid.support.strings.StringUtils.nonEmpty;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
class WalkStartPlace implements StatefulClearable {

    private final Possible<String> absoluteRoot;
    private final Possible<Location> location;
    private final Possible<LocationSubPath> locationSubPath;
    private final Possible<Catalog> catalog;      
    private final Possible<String> relativeRoot;
    private Boolean isPatternPath;
    
    WalkStartPlace() {
        this.absoluteRoot = possibleButEmpty();
        this.location = possibleButEmpty();
        this.locationSubPath = possibleButEmpty();
        this.catalog = possibleButEmpty();
        this.relativeRoot = possibleButEmpty();
        this.isPatternPath = null;
    }
    
    VoidFlow check() {
        if ( this.absoluteRoot.notMatch(root -> nonEmpty(root)) ) {
            return voidFlowFail("root must be specified!");
        } 
        if ( notExistsInFileSystem(this.absoluteRoot.orThrow()) ) {
            return voidFlowFail(format("%s does not exist!", this.absoluteRoot));
        }
        return voidFlowDone();
    }
    
    File absoluteRootAsFile() {
        return new File(this.absoluteRoot.orThrow());
    }    
    
    String absoluteRoot() {
        return this.absoluteRoot.orThrow();
    }
    
    String relativeRoot() {
        return this.relativeRoot.orThrow();
    }
    
    void muteUsing(String foundTarget) {
        if ( this.relativeRoot.isPresent() ) {
            this.relativeRoot.resetTo(joinToPath(this.relativeRoot.orThrow(), foundTarget));
        } else {
            this.relativeRoot.resetTo(foundTarget);
        }
        this.absoluteRoot.resetTo(joinToPath(this.absoluteRoot.orThrow(), foundTarget));  
    }
    
    String name() {
        String where;
        
        if ( this.isPatternPath ) {
            if ( this.location.isPresent() ) {
                where = this.location.orThrow().name();
                if ( this.relativeRoot.isPresent() ) {
                    where = joinToPath(where, this.relativeRoot.orThrow());
                }                
            } else if ( this.locationSubPath.isPresent() ) {
                where = locationSubPath.orThrow().fullName();
                if ( this.relativeRoot.isPresent() ) {
                    where = joinToPath(where, this.relativeRoot.orThrow());
                }
            } else if ( this.catalog.isPresent() ) { 
                where = this.catalog.orThrow().name();
                if ( this.relativeRoot.isPresent() ) {
                    where = joinToPath(where, this.relativeRoot.orThrow());
                }
            } else {
                where = this.absoluteRoot.orThrow();
            }      
        } else {
            if ( this.location.isPresent() ) {
                where = this.location.orThrow().name();
            } else if ( this.locationSubPath.isPresent() ) {
                where = this.locationSubPath.orThrow().fullName();
            } else if ( this.catalog.isPresent() ) { 
                where = this.catalog.orThrow().name();
            } else {
                where = this.absoluteRoot.orThrow();
            }
        }
        
        return where;
    }
    
    void setIsPatternPath(boolean isPatternPath) {
        this.isPatternPath = isPatternPath;
    }
    
    void setWhereToSearch(Catalog where) {
        this.catalog.resetTo(where);
        this.absoluteRoot.resetTo(where.path().toString());
    }
    
    void setWhereToSearch(String where) {
        this.absoluteRoot.resetTo(where);
    }
    
    void setWhereToSearch(Location where) {
        this.location.resetTo(where);
        this.absoluteRoot.resetTo(where.path());
    }
    
    void setWhereToSearch(LocationSubPath where) {
        this.locationSubPath.resetTo(where);
        this.absoluteRoot.resetTo(where.fullPath());
    }
    
    @Override
    public void clear() {
        this.absoluteRoot.nullify();
        this.location.nullify();
        this.locationSubPath.nullify();
        this.catalog.nullify();
        this.relativeRoot.nullify();
        this.isPatternPath = null;
    }
}
