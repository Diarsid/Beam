/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.external.sysconsole.modules.workers;

import old.diarsid.beam.external.sysconsole.SysConsole;

import old.diarsid.beam.external.sysconsole.modules.BeamCoreAccessModule;
import old.diarsid.beam.external.sysconsole.modules.ConsoleDispatcherModule;
import old.diarsid.beam.external.sysconsole.modules.ConsolePrinterModule;
import old.diarsid.beam.external.sysconsole.modules.ConsoleReaderModule;
import old.diarsid.beam.external.sysconsole.modules.RmiConsoleManagerModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class ConsoleDispatcherBuilder implements GemModuleBuilder<ConsoleDispatcherModule> {
    
    private final BeamCoreAccessModule beam;
    private final ConsolePrinterModule printer;
    private final ConsoleReaderModule reader;
    private final RmiConsoleManagerModule rmi;

    
    ConsoleDispatcherBuilder(
            BeamCoreAccessModule be,
            ConsolePrinterModule pr,
            ConsoleReaderModule re,
            RmiConsoleManagerModule rmi) {
        
        this.beam = be;
        this.printer = pr;
        this.reader = re;
        this.rmi = rmi;
    }
    
    @Override
    public ConsoleDispatcherModule buildModule() {
        InputHandler inputHandler = new InputHandler(printer, reader);
        ConsoleDispatcherModule dispatcher = 
                new ConsoleDispatcher(beam, printer, reader, inputHandler);
        rmi.exportAndConnectToCore(dispatcher);
        SysConsole.saveExternalIOinStaticContext(dispatcher);
        return dispatcher;
    }
}
