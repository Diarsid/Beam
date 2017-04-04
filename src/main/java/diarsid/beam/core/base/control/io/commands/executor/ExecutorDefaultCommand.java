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

import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.domain.entities.NamedEntityType.UNDEFINED_ENTITY;


public class ExecutorDefaultCommand extends InvocationEntityCommand {

    public ExecutorDefaultCommand(String argument) {
        super(argument);
    }
    
    public ExecutorDefaultCommand(String argument, String extended) {
        super(argument, extended);
    }

    @Override
    public CommandType type() {
        return EXECUTOR_DEFAULT;
    }
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.stringify(), variantIndex);
    }

    @Override
    public String stringify() {
        return super.originalArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return UNDEFINED_ENTITY;
    }
}
