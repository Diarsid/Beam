/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommonCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.START_PROGRAM;

/**
 *
 * @author Diarsid
 */
public class StartProgramCommand extends ExtendableCommonCommand {
    
    public StartProgramCommand(String program) {
        super(program);
    }
    
    public StartProgramCommand(String program, String extendedProgram) {
        super(program, extendedProgram);
    }

    @Override
    public CommandType type() {
        return START_PROGRAM;
    }
}
