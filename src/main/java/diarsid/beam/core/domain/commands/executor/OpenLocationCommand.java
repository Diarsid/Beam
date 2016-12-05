/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.commands.executor;

import diarsid.beam.core.domain.commands.Argument;
import diarsid.beam.core.domain.commands.ExecutorCommand;
import diarsid.beam.core.domain.commands.OperationType;

import static diarsid.beam.core.domain.commands.OperationType.OPEN_LOCATION;


public class OpenLocationCommand implements ExecutorCommand {
    
    private final Argument locationArgument;
    
    public OpenLocationCommand(String originalLocation) {
        this.locationArgument = new Argument(originalLocation);
    }
    
    public OpenLocationCommand(String originalLocation, String extendedLocation) {
        this.locationArgument = new Argument(originalLocation, extendedLocation);
    }
    
    public Argument location() {
        return this.locationArgument;
    }

    @Override
    public OperationType getOperation() {
        return OPEN_LOCATION;
    }

    @Override
    public String stringifyOriginal() {
        return this.locationArgument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.locationArgument.getExtended();
    }
}
