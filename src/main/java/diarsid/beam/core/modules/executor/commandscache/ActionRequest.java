/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class ActionRequest implements ActionInfo {
        
    private final String actionArgument;
    private final List<String> actionVariants;
    
    private ActionRequest(String actionArgument, List<String> actionVariants) {
        this.actionArgument = actionArgument;
        this.actionVariants = actionVariants;
    }
    
    static ActionRequest actionRequestOf(String actionArgument, Collection<String> actionVariants) {
        return new ActionRequest(actionArgument, new ArrayList<>(actionVariants));
    }

    @Override
    public String getActionArgument() {
        return this.actionArgument;
    }

    @Override
    public List<String> getActionVariants() {
        return this.actionVariants;
    }
    
    @Override
    public String toString() {
        return "ActionRequest[" + this.actionArgument + "->" + 
                this.actionVariants + "]";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.actionArgument);
        hash = 97 * hash + Objects.hashCode(this.actionVariants);
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
        final ActionRequest other = ( ActionRequest ) obj;
        if ( !Objects.equals(this.actionArgument, other.actionArgument) ) {
            return false;
        }
        if ( !Objects.equals(this.actionVariants, other.actionVariants) ) {
            return false;
        }
        return true;
    }    
}
