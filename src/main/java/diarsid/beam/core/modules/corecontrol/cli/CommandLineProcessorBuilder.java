/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import diarsid.beam.core.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.InterpreterHolderModule;
import diarsid.beam.core.modules.IoModule;

/**
 *
 * @author Diarsid
 */
public class CommandLineProcessorBuilder {
    
    public CommandLineProcessorBuilder() {
    }
    
    public CommandLineProcessor build(
            IoModule ioModule, 
            InterpreterHolderModule interpreterModule, 
            DomainKeeperModule domainModule) {
        Interpreter interpreter = interpreterModule.getInterpreter();
        DomainModuleToCliAdapter domainToCliAdapter = 
                new DomainModuleToCliAdapter(domainModule, ioModule.getInnerIoEngine());
        CliCommandDispatcher commandDispatcher = 
                new CliCommandDispatcher(
                        ioModule, domainToCliAdapter);
        CommandLineProcessor cli = new CommandLineProcessor(interpreter, commandDispatcher);
        return cli;
    }
}
