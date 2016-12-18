/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import diarsid.beam.core.control.Initiator;

/**
 *
 * @author Diarsid
 */
public class CommandLineProcessor {
    
    private final Initiator initiator;
    private final Interpreter interpreter;
    private final CoreCommandDispatcher dispatcher;
    
    public CommandLineProcessor(Initiator initiator, Interpreter interpreter, CoreCommandDispatcher dispatcher) {
        this.initiator = initiator;
        this.interpreter = interpreter;
        this.dispatcher = dispatcher;
    }
    
    public void process(String commandLine) {
        this.dispatcher.dispatch(
                this.initiator,
                this.interpreter.interprete(commandLine));
    }
}
