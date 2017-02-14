/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol;

import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.corecontrol.cli.CommandLineProcessorBuilder;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
public class CoreControlModuleWorkerBuilder implements GemModuleBuilder<CoreControlModule> {
    
    private final IoModule ioModule;
    private final ApplicationComponentsHolderModule appComponentsHolderModule;
    private final DomainKeeperModule domainModule;
    
    public CoreControlModuleWorkerBuilder(
            IoModule ioModule, 
            ApplicationComponentsHolderModule appComponentsHolderModule,
            DomainKeeperModule domainModule) {
        this.ioModule = ioModule;
        this.appComponentsHolderModule = appComponentsHolderModule;
        this.domainModule = domainModule;
    }

    @Override
    public CoreControlModule buildModule() {
        CommandLineProcessorBuilder cliBuilder = new CommandLineProcessorBuilder();
        CommandLineProcessor cli = cliBuilder.build(
                this.ioModule, 
                this.appComponentsHolderModule,
                this.domainModule);
//        OuterIoEngine nativeConsole = new NativeConsoleBuilder().build(cli);
//        this.ioModule.registerOuterIoEngine(nativeConsole);
        CoreControlModule coreControlModule = new CoreControlModuleWorker(this.ioModule, cli);
        return coreControlModule;
    }
}
