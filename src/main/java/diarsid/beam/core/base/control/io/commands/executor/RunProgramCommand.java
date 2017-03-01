/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommonCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;


public class RunProgramCommand extends ExtendableCommonCommand {
        
    public RunProgramCommand(String program) {
        super(program);
    }
    
    public RunProgramCommand(String program, String extendedProgram) {
        super(program, extendedProgram);
    }

    @Override
    public CommandType type() {
        return RUN_PROGRAM;
    }
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.stringify(), variantIndex);
    }

    @Override
    public String stringify() {
        return "run " + super.stringifyOriginalArgs();
    }
}
