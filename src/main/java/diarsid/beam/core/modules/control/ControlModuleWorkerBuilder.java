/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control;

import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.ControlModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.control.cli.CommandLineProcessorBuilder;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
public class ControlModuleWorkerBuilder implements GemModuleBuilder<ControlModule> {
    
    private final IoModule ioModule;
    private final ApplicationComponentsHolderModule appComponentsHolderModule;
    private final DomainKeeperModule domainModule;
    private final ExecutorModule executorModule;
    
    public ControlModuleWorkerBuilder(
            IoModule ioModule, 
            ApplicationComponentsHolderModule appComponentsHolderModule,
            DomainKeeperModule domainModule,
            ExecutorModule executorModule) {
        this.ioModule = ioModule;
        this.appComponentsHolderModule = appComponentsHolderModule;
        this.domainModule = domainModule;
        this.executorModule = executorModule;
    }

    @Override
    public ControlModule buildModule() {
        CommandLineProcessorBuilder cliBuilder = new CommandLineProcessorBuilder();
        CommandLineProcessor cli = cliBuilder.build(
                this.ioModule, 
                this.appComponentsHolderModule,
                this.executorModule,
                this.domainModule);
//        OuterIoEngine nativeConsole = new NativeConsoleBuilder().build(cli);
//        this.ioModule.registerOuterIoEngine(nativeConsole);
        ControlModule coreControlModule = new ControlModuleWorker(this.ioModule, cli);
        return coreControlModule;
    }
}
