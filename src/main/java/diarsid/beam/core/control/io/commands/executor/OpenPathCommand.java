/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands.executor;

import java.util.Objects;

import diarsid.beam.core.control.io.commands.Argument;
import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.CommandType;

import static diarsid.beam.core.control.io.commands.CommandType.OPEN_PATH;
import static diarsid.beam.core.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.util.PathUtils.extractTargetFromPath;


public class OpenPathCommand implements ArgumentedCommand {
    
    private final Argument locationArgument;
    private final Argument targetArgument;
    
    public OpenPathCommand(String originalPath) {
        this.locationArgument = new Argument(extractLocationFromPath(originalPath));
        this.targetArgument = new Argument(extractTargetFromPath(originalPath));
    }
    
    public OpenPathCommand(String originalPath, String extendedPath) {
        this.locationArgument = new Argument(
                extractLocationFromPath(originalPath), 
                extractTargetFromPath(originalPath));
        this.targetArgument = new Argument(
                extractLocationFromPath(extendedPath), 
                extractTargetFromPath(extendedPath));
    }

    public Argument location() {
        return this.locationArgument;
    }

    public Argument target() {
        return this.targetArgument;
    }

    @Override
    public CommandType type() {
        return OPEN_PATH;
    }

    @Override
    public String stringifyOriginal() {
        return this.locationArgument.getOriginal() + "/" + this.targetArgument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.locationArgument.getExtended()+ "/" + this.targetArgument.getExtended();
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
