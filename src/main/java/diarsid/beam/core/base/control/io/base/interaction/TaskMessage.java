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
public class TaskMessage implements Serializable {
    
    private final String time;
    private final List<String> content;

    public TaskMessage(String time, List<String> content) {
        this.time = time;
        this.content = content;
    }
        
    public String time() {
        return this.time;
    }

    public List<String> text() {
        return this.content;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.time);
        hash = 67 * hash + Objects.hashCode(this.content);
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
        final TaskMessage other = ( TaskMessage ) obj;
        if ( !Objects.equals(this.time, other.time) ) {
            return false;
        }
        if ( !Objects.equals(this.content, other.content) ) {
            return false;
        }
        return true;
    }
    
    
}
