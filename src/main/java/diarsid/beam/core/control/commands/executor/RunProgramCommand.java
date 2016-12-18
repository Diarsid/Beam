/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.executor;

import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.SingleArgumentCommand;

import static diarsid.beam.core.control.commands.CommandType.RUN_PROGRAM;


public class RunProgramCommand extends SingleArgumentCommand {
        
    public RunProgramCommand(String program) {
        super(program);
    }
    
    public RunProgramCommand(String program, String extendedProgram) {
        super(program, extendedProgram);
    }

    @Override
    public CommandType getType() {
        return RUN_PROGRAM;
    }
}
