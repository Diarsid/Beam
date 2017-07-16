/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
class JsonError {
    
    private final int status;
    private final String json;

    JsonError(int status, String json) {
        this.status = status;
        this.json = json;
    }

    public int status() {
        return status;
    }

    public String json() {
        return json;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.status;
        hash = 29 * hash + Objects.hashCode(this.json);
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
        final JsonError other = ( JsonError ) obj;
        if ( this.status != other.status ) {
            return false;
        }
        if ( !Objects.equals(this.json, other.json) ) {
            return false;
        }
        return true;
    }
}
