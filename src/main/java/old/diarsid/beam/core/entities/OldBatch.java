/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class OldBatch implements Serializable {
    
    private final String name;
    private final List<String> commands;    
    
    public OldBatch(String name, List<String> commands) {
        this.name = name;
        this.commands = commands;
    }    
    
    public String getName() {
        return name;
    }

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public String toString() {        
        return "StoredExecutorCommand > " + this.name + " : " + this.commands.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.commands);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OldBatch other = (OldBatch) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.commands, other.commands)) {
            return false;
        }
        return true;
    }
}
