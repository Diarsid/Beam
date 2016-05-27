/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.workflow;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public final class CommandChoice {
    
    private final String pattern;
    private final String madeChoice;
    private final int choiceNumber;
    
    public CommandChoice(String pattern, String choice, int choiceNumber) {
        this.pattern = pattern;
        this.madeChoice = choice;
        this.choiceNumber = choiceNumber;
    }

    public String getPattern() {
        return pattern;
    }

    public String getMadeChoice() {
        return madeChoice;
    }
    
    public int getChoiceNumber() {
        return this.choiceNumber;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.pattern);
        hash = 97 * hash + Objects.hashCode(this.madeChoice);
        hash = 97 * hash + this.choiceNumber;
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
        final CommandChoice other = ( CommandChoice ) obj;
        if ( this.choiceNumber != other.choiceNumber ) {
            return false;
        }
        if ( !Objects.equals(this.pattern, other.pattern) ) {
            return false;
        }
        if ( !Objects.equals(this.madeChoice, other.madeChoice) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CommandChoice{" + 
                "pattern=" + pattern + 
                ", madeChoice=" + madeChoice + 
                ", choiceNumber=" + choiceNumber + '}';
    }
}
