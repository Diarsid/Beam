/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class ActionChoice implements ActionInfo {
    
    private static final String DO_NOT_CHOOSE_AMONG_VARIANTS;
    static {
        DO_NOT_CHOOSE_AMONG_VARIANTS = "NO_CHOICE";
    }
    
    private final String actionArgument;
    private final List<String> actionVariants;
    private final String madeChoice;
    
    private ActionChoice(String actionArgument, List<String> actionVariants, String madeChoice) {
        this.actionArgument = actionArgument;
        this.actionVariants = actionVariants;
        this.madeChoice = madeChoice;
    }
    
    static ActionChoice formulateChoiceFor(ActionRequest actionRequest, String madeChoice) {
        return new ActionChoice(
                actionRequest.getActionArgument(), 
                actionRequest.getActionVariants(),
                madeChoice);
    }
    
    static ActionChoice formulateNoChoiceFor(ActionRequest actionRequest) {
        return new ActionChoice(
                actionRequest.getActionArgument(), 
                actionRequest.getActionVariants(), 
                DO_NOT_CHOOSE_AMONG_VARIANTS);
    }
    
    static boolean ifUserDoNotWantToResolveTheeseActions(String choice) {
        return DO_NOT_CHOOSE_AMONG_VARIANTS.equals(choice);
    }
    
    @Override
    public String getActionArgument() {
        return this.actionArgument;
    }

    @Override
    public List<String> getActionVariants() {
        return this.actionVariants;
    }

    public String getMadeChoice() {
        return this.madeChoice;
    }
    
    @Override
    public String toString() {
        return "ActionChoice[" + this.actionArgument + "->" + 
                this.actionVariants + "->" + this.madeChoice + "]";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.actionArgument);
        hash = 37 * hash + Objects.hashCode(this.actionVariants);
        hash = 37 * hash + Objects.hashCode(this.madeChoice);
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
        final ActionChoice other = ( ActionChoice ) obj;
        if ( !Objects.equals(this.actionArgument, other.actionArgument) ) {
            return false;
        }
        if ( !Objects.equals(this.madeChoice, other.madeChoice) ) {
            return false;
        }
        if ( !Objects.equals(this.actionVariants, other.actionVariants) ) {
            return false;
        }
        return true;
    }
}
