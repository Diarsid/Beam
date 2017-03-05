/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.space;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class VirtualRoot {
    
    private final String name;
    private final Path path;

    public VirtualRoot(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public String name() {
        return this.name;
    }

    public Path path() {
        return this.path;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.name);
        hash = 83 * hash + Objects.hashCode(this.path);
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
        final VirtualRoot other = ( VirtualRoot ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( !Objects.equals(this.path, other.path) ) {
            return false;
        }
        return true;
    }
}
