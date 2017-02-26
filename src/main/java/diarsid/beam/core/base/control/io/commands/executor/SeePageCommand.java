/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Objects;

import diarsid.beam.core.base.control.io.commands.ExtendableArgument;
import diarsid.beam.core.base.control.io.commands.CommandType;

import static diarsid.beam.core.base.control.io.commands.CommandType.SEE_WEBPAGE;

import diarsid.beam.core.base.control.io.commands.ExtendableCommand;


public class SeePageCommand implements ExtendableCommand {
    
    private final ExtendableArgument pageArgument;
    
    public SeePageCommand(String pageName) {
        this.pageArgument = new ExtendableArgument(pageName);
    }
    
    public SeePageCommand(String pageName, String extendedPageName) {
        this.pageArgument = new ExtendableArgument(pageName, extendedPageName);
    }
    
    public ExtendableArgument page() {
        return this.pageArgument;
    }

    @Override
    public CommandType type() {
        return SEE_WEBPAGE;
    }

    @Override
    public String stringifyOriginal() {
        return this.pageArgument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.pageArgument.getExtended();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.pageArgument);
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
        final SeePageCommand other = ( SeePageCommand ) obj;
        if ( !Objects.equals(this.pageArgument, other.pageArgument) ) {
            return false;
        }
        return true;
    }
}