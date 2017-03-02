/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableArgument;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH;
import static diarsid.beam.core.base.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.base.util.PathUtils.extractTargetFromPath;
import static diarsid.beam.core.base.util.Requirements.requireNonEmpty;


public class OpenPathCommand implements ExtendableCommand {
    
    private final ExtendableArgument locationArgument;
    private final ExtendableArgument targetArgument;
    
    public OpenPathCommand(String originalPath) {
        requireNonEmpty(originalPath, "original path cannot be empty.");
        this.locationArgument = new ExtendableArgument(extractLocationFromPath(originalPath));
        this.targetArgument = new ExtendableArgument(extractTargetFromPath(originalPath));
    }
    
    public OpenPathCommand(String originalPath, String extendedPath) {
        requireNonEmpty(originalPath, "original path cannot be empty.");
        this.locationArgument = new ExtendableArgument(
                extractLocationFromPath(originalPath), 
                extractLocationFromPath(extendedPath));
        this.targetArgument = new ExtendableArgument(
                extractTargetFromPath(originalPath), 
                extractTargetFromPath(extendedPath));
    }

    public ExtendableArgument location() {
        return this.locationArgument;
    }

    public ExtendableArgument target() {
        return this.targetArgument;
    }

    @Override
    public CommandType type() {
        return OPEN_PATH;
    }
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.stringify(), variantIndex);
    }

    @Override
    public String stringify() {
        return "open " + this.stringifyOriginalArgs();
    }

    @Override
    public String stringifyOriginalArgs() {
        return this.locationArgument.originalArg() + "/" + this.targetArgument.originalArg();
    }

    @Override
    public String stringifyExtendedArgs() {
        if ( this.locationArgument.hasExtended() && this.targetArgument.hasExtended() ) {
            return this.locationArgument.extendedArg()+ "/" + this.targetArgument.extendedArg();
        } else {
            return "";
        }        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.locationArgument);
        hash = 97 * hash + Objects.hashCode(this.targetArgument);
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
        final OpenPathCommand other = ( OpenPathCommand ) obj;
        if ( !Objects.equals(this.locationArgument, other.locationArgument) ) {
            return false;
        }
        if ( !Objects.equals(this.targetArgument, other.targetArgument) ) {
            return false;
        }
        return true;
    }
}
