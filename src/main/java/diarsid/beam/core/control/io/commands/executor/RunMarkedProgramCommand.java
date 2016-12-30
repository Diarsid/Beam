/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands.executor;

import diarsid.beam.core.control.io.commands.Argument;
import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.CommandType;
import diarsid.beam.core.control.io.commands.exceptions.EmptyArgumentException;

import static diarsid.beam.core.control.io.commands.CommandType.RUN_MARKED_PROGRAM;


public class RunMarkedProgramCommand implements ArgumentedCommand {
    
    private final Argument programArgument;
    private final String mark;
    
    public RunMarkedProgramCommand(String program, String mark) {
        this.programArgument = new Argument(program);
        if ( mark.isEmpty() ) {
            throw new EmptyArgumentException();
        }
        this.mark = mark.toLowerCase();
    }
    
    public RunMarkedProgramCommand(String program, String extendedProgram, String mark) {
        this.programArgument = new Argument(program, extendedProgram);
        if ( mark.isEmpty() ) {
            throw new EmptyArgumentException();
        }
        this.mark = mark.toLowerCase();
    }
    
    public Argument program() {
        return this.programArgument;
    }

    @Override
    public CommandType type() {
        return RUN_MARKED_PROGRAM;
    }
    
    public String getMark() {
        return this.mark;
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
