/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DomainKeeperModule;
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
            ApplicationComponentsHolderModule appComponentsHolderModule, 
            DomainKeeperModule domainModule) {
        Interpreter interpreter = appComponentsHolderModule.getInterpreter();
        DomainModuleToCliAdapter domainToCliAdapter = 
                new DomainModuleToCliAdapter(domainModule, ioModule.getInnerIoEngine());
        CliCommandDispatcher commandDispatcher = 
                new CliCommandDispatcher(
                        ioModule, domainToCliAdapter);
        CommandLineProcessor cli = new CommandLineProcessor(interpreter, commandDispatcher);
        return cli;
    }
}
