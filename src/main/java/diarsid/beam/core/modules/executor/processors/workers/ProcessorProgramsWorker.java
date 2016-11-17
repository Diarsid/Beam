/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.context.ExecutorContext;
import diarsid.beam.core.modules.executor.processors.ProcessorPrograms;

/**
 *
 * @author Diarsid
 */
class ProcessorProgramsWorker implements ProcessorPrograms {
    
    private final IoInnerModule ioEngine;
    private final OS system;    
    private final ExecutorContext context;
    
    ProcessorProgramsWorker(IoInnerModule io, OS system, ExecutorContext context) {
        this.ioEngine = io;
        this.system = system;
        this.context = context;
    }
    
    @Override
    public void runProgram(String programName) {
        // command pattern: run [program_name]
        this.system.runProgram(programName);
    }
            
    @Override
    public void runMarkedProgram(String mark, String programName) {
        // command pattern: [start|stop] [program]
        this.system.runMarkedProgram(programName, mark);
    }
}
