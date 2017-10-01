/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class HelpInfo implements Help, Serializable {
    
    private final List<String> help;

    HelpInfo(List<String> help) {
        this.help = help;
    }

    @Override
    public boolean isInfo() {
        return true;
    }

    @Override
    public boolean isKey() {
        return false;
    }

    public List<String> getLines() {
        return this.help;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.help);
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
        final HelpInfo other = ( HelpInfo ) obj;
        if ( !Objects.equals(this.help, other.help) ) {
            return false;
        }
        return true;
    }
}
