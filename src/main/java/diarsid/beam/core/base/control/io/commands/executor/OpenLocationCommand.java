/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.InvocationEntityCommand;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;


public class OpenLocationCommand extends InvocationEntityCommand {
        
    public OpenLocationCommand(String originalLocation) {
        super(originalLocation);
    }
    
    public OpenLocationCommand(String originalLocation, String extendedLocation) {
        super(originalLocation, extendedLocation);
    }
    
    @Override
    public CommandType type() {
        return OPEN_LOCATION;
    }
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.stringify(), variantIndex);
    }

    @Override
    public String stringify() {
        return "open " + super.originalArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return LOCATION;
    }
}
