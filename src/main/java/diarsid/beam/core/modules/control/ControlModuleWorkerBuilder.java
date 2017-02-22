/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control;

import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.control.cli.CommandLineProcessorBuilder;

import com.drs.gem.injector.module.GemModuleBuilder;

import diarsid.beam.core.modules.ControlModule;

/**
 *
 * @author Diarsid
 */
public class ControlModuleWorkerBuilder implements GemModuleBuilder<ControlModule> {
    
    private final IoModule ioModule;
    private final ApplicationComponentsHolderModule appComponentsHolderModule;
    private final DomainKeeperModule domainModule;
    
    public ControlModuleWorkerBuilder(
            IoModule ioModule, 
            ApplicationComponentsHolderModule appComponentsHolderModule,
            DomainKeeperModule domainModule) {
        this.ioModule = ioModule;
        this.appComponentsHolderModule = appComponentsHolderModule;
        this.domainModule = domainModule;
    }

    @Override
    public ControlModule buildModule() {
        CommandLineProcessorBuilder cliBuilder = new CommandLineProcessorBuilder();
        CommandLineProcessor cli = cliBuilder.build(
                this.ioModule, 
                this.appComponentsHolderModule,
                this.domainModule);
//        OuterIoEngine nativeConsole = new NativeConsoleBuilder().build(cli);
//        this.ioModule.registerOuterIoEngine(nativeConsole);
        ControlModule coreControlModule = new ControlModuleWorker(this.ioModule, cli);
        return coreControlModule;
    }
}
