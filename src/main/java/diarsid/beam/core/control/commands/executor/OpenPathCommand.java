/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.executor;

import diarsid.beam.core.control.commands.Argument;
import diarsid.beam.core.control.commands.CommandType;

import static diarsid.beam.core.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.util.PathUtils.extractTargetFromPath;

import diarsid.beam.core.control.commands.ArgumentedCommand;

import static diarsid.beam.core.control.commands.CommandType.OPEN_PATH;


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
    public CommandType getType() {
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
}
