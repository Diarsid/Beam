/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands;

import diarsid.beam.core.control.commands.exceptions.EmptyArgumentException;

/**
 *
 * @author Diarsid
 */
public class Argument {
    
    private final String originalArgument;
    private String extendedArgument;
    
    public Argument(String originalArgument) {
        this.onlyNonEmptyArgument(originalArgument);
        this.originalArgument = originalArgument;
        this.extendedArgument = "";
    }
    
    public Argument(String originalArgument, String improvedArgument) {
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
}
