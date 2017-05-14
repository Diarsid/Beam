/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Objects;

import static diarsid.beam.core.base.util.Requirements.requireNonEmpty;

/**
 *
 * @author Diarsid
 */
public class ExtendableArgument {
    
    private final String originalArgument;
    private String extendedArgument;
    
    public ExtendableArgument(String originalArgument) {
        requireNonEmpty(originalArgument, "command original argument cannot be empty.");
        this.originalArgument = originalArgument;
        this.extendedArgument = "";
    }
    
    public ExtendableArgument(String originalArgument, String extendedArgument) {
        requireNonEmpty(originalArgument, "command original argument cannot be empty.");
        requireNonEmpty(extendedArgument, "command extended argument cannot be empty.");
        this.originalArgument = originalArgument;
        this.extendedArgument = extendedArgument;
    }

    public String extended() {
        return this.extendedArgument;
    }

    public void setExtended(String extendedArgument) {
        this.extendedArgument = extendedArgument;
    }
    
    public void unextend() {
        this.extendedArgument = "";
    }

    public String original() {
        return this.originalArgument;
    }
    
    public boolean isExtended() {
        return ! this.extendedArgument.isEmpty();
    }
    
    public boolean isNotExtended() {
        return this.extendedArgument.isEmpty();
    }
    
    public String get() {
        if ( this.isExtended() ) {
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
