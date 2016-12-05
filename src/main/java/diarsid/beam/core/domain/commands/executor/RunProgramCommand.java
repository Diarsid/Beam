/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.commands.executor;

import diarsid.beam.core.domain.commands.Argument;
import diarsid.beam.core.domain.commands.ExecutorCommand;
import diarsid.beam.core.domain.commands.OperationType;

import static diarsid.beam.core.domain.commands.OperationType.RUN_PROGRAM;


public class RunProgramCommand implements ExecutorCommand {
    
    private final Argument programArgument;
    
    public RunProgramCommand(String program) {
        this.programArgument = new Argument(program);
    }
    
    public RunProgramCommand(String program, String extendedProgram) {
        this.programArgument = new Argument(program, extendedProgram);
    }
    
    public Argument program() {
        return this.programArgument;
    }

    @Override
    public OperationType getOperation() {
        return RUN_PROGRAM;
    }

    @Override
    public String stringifyOriginal() {
        return this.programArgument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.programArgument.getExtended();
    }
}
