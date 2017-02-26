/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import java.util.Objects;

import diarsid.beam.core.base.control.io.commands.exceptions.EmptyArgumentException;

/**
 *
 * @author Diarsid
 */
public class ExtendableArgument {
    
    private final String originalArgument;
    private String extendedArgument;
    
    public ExtendableArgument(String originalArgument) {
        this.onlyNonEmptyArgument(originalArgument);
        this.originalArgument = originalArgument;
        this.extendedArgument = "";
    }
    
    public ExtendableArgument(String originalArgument, String improvedArgument) {
        this.onlyNonEmptyArgument(originalArgument);
        this.originalArgument = originalArgument;
        this.onlyNonEmptyArgument(improvedArgument);
        this.extendedArgument = improvedArgument;
    }
    
    private void onlyNonEmptyArgument(String arg) {
        if ( arg.isEmpty() ) {
            throw new EmptyArgumentException();
        }
    }

    public String getExtended() {
        return this.extendedArgument;
    }

    public void setExtended(String extendedArgument) {
        this.extendedArgument = extendedArgument;
    }

    public String getOriginal() {
        return this.originalArgument;
    }
    
    public boolean hasExtended() {
        return ! this.extendedArgument.isEmpty();
    }
    
    public boolean doesNotHaveExtended() {
        return this.extendedArgument.isEmpty();
    }
    
    public String get() {
        if ( this.hasExtended() ) {
            return this.extendedArgument;
        } else {
            return this.originalArgument;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.originalArgument);
        hash = 71 * hash + Objects.hashCode(this.extendedArgument);
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
        final ExtendableArgument other = ( ExtendableArgument ) obj;
        if ( !Objects.equals(this.originalArgument, other.originalArgument) ) {
            return false;
        }
        if ( !Objects.equals(this.extendedArgument, other.extendedArgument) ) {
            return false;
        }
        return true;
    }
}