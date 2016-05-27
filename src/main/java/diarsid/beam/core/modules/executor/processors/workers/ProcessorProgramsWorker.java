/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.processors.ProcessorPrograms;
import diarsid.beam.core.modules.executor.workflow.OperationResult;

import static diarsid.beam.core.modules.executor.workflow.OperationResult.failByInvalidLogic;

/**
 *
 * @author Diarsid
 */
class ProcessorProgramsWorker implements ProcessorPrograms {
    
    private final IoInnerModule ioEngine;
    private final OS system;
    
    
    ProcessorProgramsWorker(IoInnerModule io, OS system) {
        this.ioEngine = io;
        this.system = system;
    }
    
    @Override
    public List<OperationResult> runPrograms(List<String> params) {
        // command pattern: run [program_1] [program_2] [program_3]...
        List<OperationResult> results = new ArrayList<>();
        for (int i = 1; i < params.size(); i++) {
            results.add(this.system.runProgram(params.get(i)));
        }
        return results;
    }
            
    @Override
    public OperationResult runMarkedProgram(String mark, List<String> commandParams) {
        if (commandParams.size() == 2){
            return this.system.runProgram(commandParams.get(1)+"-"+mark);
        } else {
            this.ioEngine.reportMessage("Unrecognizable command.");
            return failByInvalidLogic();
        }
    }
}
