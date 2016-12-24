/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol;

import diarsid.beam.core.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.IoModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
public class CoreControlModuleWorkerBuilder implements GemModuleBuilder<CoreControlModule> {
    
    private final IoModule ioModule;
    
    public CoreControlModuleWorkerBuilder(IoModule ioModule) {
        this.ioModule = ioModule;
    }

    @Override
    public CoreControlModule buildModule() {
        Interpreter interpreter = new Interpreter();
        CoreCommandDispatcher commandDispatcher = new CoreCommandDispatcher(this.ioModule);
        CommandLineProcessor cli = new CommandLineProcessor(interpreter, commandDispatcher);
        
//        OuterIoEngine nativeConsole = new NativeConsoleBuilder().build(cli);
//        this.ioModule.registerOuterIoEngine(nativeConsole);
        CoreControlModule coreControlModule = new CoreControlModuleWorker(this.ioModule, cli);
        commandDispatcher.setCoreControl(coreControlModule);
        return coreControlModule;
    }
}
