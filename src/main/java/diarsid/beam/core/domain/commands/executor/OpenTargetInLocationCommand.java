/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.commands.executor;

import diarsid.beam.core.domain.commands.Argument;
import diarsid.beam.core.domain.commands.ExecutorCommand;
import diarsid.beam.core.domain.commands.OperationType;

import static diarsid.beam.core.domain.commands.OperationType.OPEN_TARGET_IN_LOCATION;
import static diarsid.beam.core.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.util.PathUtils.extractTargetFromPath;


public class OpenTargetInLocationCommand implements ExecutorCommand {
    
    private final Argument locationArgument;
    private final Argument targetArgument;
    
    public OpenTargetInLocationCommand(String originalPath) {
        this.locationArgument = new Argument(extractLocationFromPath(originalPath));
        this.targetArgument = new Argument(extractTargetFromPath(originalPath));
    }
    
    public OpenTargetInLocationCommand(String originalPath, String extendedPath) {
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
    public OperationType getOperation() {
        return OPEN_TARGET_IN_LOCATION;
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
