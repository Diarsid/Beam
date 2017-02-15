/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class ScheduledEntityArguments {
    
    private final String time;
    private final String text;
    
    public ScheduledEntityArguments(String time, String text) {
        this.time = time;
        this.text = text;
    }

    public String getTime() {
        return this.time;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.time);
        hash = 67 * hash + Objects.hashCode(this.text);
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
        final ScheduledEntityArguments other = ( ScheduledEntityArguments ) obj;
        if ( !Objects.equals(this.time, other.time) ) {
            return false;
        }
        if ( !Objects.equals(this.text, other.text) ) {
            return false;
        }
        return true;
    }
}
