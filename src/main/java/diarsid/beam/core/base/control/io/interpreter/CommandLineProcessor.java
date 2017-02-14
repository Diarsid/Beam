/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import diarsid.beam.core.base.control.io.base.actors.Initiator;

/**
 *
 * @author Diarsid
 */
public class CommandLineProcessor {
    
    private final Interpreter interpreter;
    private final CommandDispatcher dispatcher;
    
    public CommandLineProcessor(Interpreter interpreter, CommandDispatcher dispatcher) {
        this.interpreter = interpreter;
        this.dispatcher = dispatcher;
    }
    
    public void process(Initiator initiator, String commandLine) {
        this.dispatcher.dispatch(
                initiator, this.interpreter.interprete(commandLine));
    }
}
