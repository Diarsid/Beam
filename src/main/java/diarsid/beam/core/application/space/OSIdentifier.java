/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.space;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class OSIdentifier {
    
    private final String osName;

    OSIdentifier(String osName) {
        this.osName = osName;
    }

    public String getOsName() {
        return osName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.osName);
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
        final OSIdentifier other = ( OSIdentifier ) obj;
        if ( !Objects.equals(this.osName, other.osName) ) {
            return false;
        }
        return true;
    }
    
    
}
