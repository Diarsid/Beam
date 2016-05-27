/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;

import static diarsid.beam.core.modules.executor.OperationResult.failByInvalidLogic;

/**
 *
 * @author Diarsid
 */
class ProcessorPrograms {
    
    private final IoInnerModule ioEngine;
    private final OS system;
    
    
    ProcessorPrograms(IoInnerModule io, OS system) {
        this.ioEngine = io;
        this.system = system;
    }
    
    List<OperationResult> runPrograms(List<String> params) {
        // command pattern: run [program_1] [program_2] [program_3]...
        List<OperationResult> results = new ArrayList<>();
        for (int i = 1; i < params.size(); i++) {
            results.add(this.system.runProgram(params.get(i)));
        }
        return results;
    }
            
    OperationResult runMarkedProgram(String mark, List<String> commandParams) {
        if (commandParams.size() == 2){
            return this.system.runProgram(commandParams.get(1)+"-"+mark);
        } else {
            this.ioEngine.reportMessage("Unrecognizable command.");
            return failByInvalidLogic();
        }
    }
}
