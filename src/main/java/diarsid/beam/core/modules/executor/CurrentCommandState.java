/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class CurrentCommandState {
    
    private String command;
    private final List<CommandChoice> madeChoices;
    
    CurrentCommandState(List<String> commandParams) {
        this.command = String.join(" ", commandParams);
        this.madeChoices = new ArrayList<>();
    }
    
    CurrentCommandState(String command) {
        this.command = command;
        this.madeChoices = new ArrayList<>();
    }
    
    public CurrentCommandState(String command, List<CommandChoice> choices) {
        this.command = command;
        this.madeChoices = choices;
    }
    
    void adjustCommand(String command) {
        this.command = command;
    }
    
    void addChoice(String patternToResolve, String madeChoice) {
        this.madeChoices.add(new CommandChoice(
                patternToResolve, madeChoice, this.madeChoices.size()));
    }

    public List<CommandChoice> getMadeChoices() {
        return this.madeChoices;
    }
    
    boolean hasChoices() {
        return ( ! this.madeChoices.isEmpty() );
    }
        
    public String getCommandString() {
        return String.join(" ", this.command);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.command);
        hash = 37 * hash + Objects.hashCode(this.madeChoices);
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
        final CurrentCommandState other = ( CurrentCommandState ) obj;
        if ( !Objects.equals(this.command, other.command) ) {
            return false;
        }
        if ( !Objects.equals(this.madeChoices, other.madeChoices) ) {
            return false;
        }
        return true;
    }
}
