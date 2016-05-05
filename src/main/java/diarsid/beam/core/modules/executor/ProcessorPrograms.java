/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;

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
    
    void runProgram(List<String> params) {
        // command pattern: run [program_1] [program_2] [program_3]...
        for (int i = 1; i < params.size(); i++) {
            this.system.runProgram(params.get(i));
        }
    }
            
    void runMarkedProgram(String mark, List<String> commandParams) {
        if (commandParams.size() == 2){
            this.system.runProgram(commandParams.get(1)+"-"+mark);
        } else {
            this.ioEngine.reportMessage("Unrecognizable command.");
        }
    }
}
