/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control;

import diarsid.beam.core.base.control.io.base.console.Console;
import diarsid.beam.core.base.control.io.base.console.ConsoleCommandRealProcessor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.ControlModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.base.control.io.base.console.Console.buildConsoleUsing;
import static diarsid.beam.core.modules.control.cli.CliCommandDispatcher.buildCommandLineProcessor;

/**
 *
 * @author Diarsid
 */
public class ControlModuleWorkerBuilder implements GemModuleBuilder<ControlModule> {
    
    private final IoModule ioModule;
    private final DataModule dataModule;
    private final ApplicationComponentsHolderModule appComponentsHolderModule;
    private final DomainKeeperModule domainModule;
    private final ExecutorModule executorModule;
    
    public ControlModuleWorkerBuilder(
            IoModule ioModule, 
            DataModule dataModule,
            ApplicationComponentsHolderModule appComponentsHolderModule,
            DomainKeeperModule domainModule,
            ExecutorModule executorModule) {
        this.ioModule = ioModule;
        this.dataModule = dataModule;
        this.appComponentsHolderModule = appComponentsHolderModule;
        this.domainModule = domainModule;
        this.executorModule = executorModule;
    }

    @Override
    public ControlModule buildModule() {
        ConsoleCommandRealProcessor consoleProcessor = buildCommandLineProcessor(
                this.ioModule, 
                this.appComponentsHolderModule,
                this.executorModule,
                this.domainModule);
        
        if ( this.appComponentsHolderModule.configuration().asBoolean("ui.console.runOnStart") ) {
            this.startJavaFxConsoleUsing(consoleProcessor);
        }       
        
        ControlModule coreControlModule = new ControlModuleWorker(this.ioModule, consoleProcessor);
        return coreControlModule;
    }
    
    private void startJavaFxConsoleUsing(ConsoleCommandRealProcessor consoleProcessor) {
        ConsolePlatform javaFxConsolePlatform = this.appComponentsHolderModule
                .gui()
                .interactionGui()
                .guiConsolePlatformFor(this.dataModule, consoleProcessor);
        
        Console javaFxConsole = buildConsoleUsing(javaFxConsolePlatform);
        
        this.ioModule.registerOuterIoEngine(javaFxConsole);
        
        javaFxConsole.launch();
    }
}
