/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.EntityInvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;


public class OpenLocationTargetCommand extends EntityInvocationCommand {
    
    public OpenLocationTargetCommand(String originalTarget) {
        super(originalTarget);
    }
    
    public OpenLocationTargetCommand(String originalTarget, String extendedTarget) {
        super(originalTarget, extendedTarget);
    }

    @Override
    public CommandType type() {
        return OPEN_LOCATION_TARGET;
    }
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.stringify(), variantIndex);
    }

    @Override
    public String stringify() {
        return "open " + this.originalArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
