/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.interpreterholder;

import diarsid.beam.core.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.InterpreterHolderModule;

/**
 *
 * @author Diarsid
 */
public class InterpreterHolderModuleWorker implements InterpreterHolderModule {
    
    private final Interpreter interpreter;
    
    public InterpreterHolderModuleWorker() {
        this.interpreter = new Interpreter();
    }

    @Override
    public Interpreter getInterpreter() {
        return this.interpreter;
    }
}
