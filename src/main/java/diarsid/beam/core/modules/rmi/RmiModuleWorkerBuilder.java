/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.rmi;

import diarsid.beam.core.Beam;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.HandlerManagerModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RmiModule;
import diarsid.beam.core.modules.TaskManagerModule;
import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class RmiModuleWorkerBuilder implements GemModuleBuilder<RmiModule> {
    
    private final IoModule ioModule;
    private final IoInnerModule innerIoModule;
    private final ConfigModule configModule;
    private final HandlerManagerModule handlers;
    private final ExecutorModule executorModule;
    private final TaskManagerModule taskManagerModule;
    
    RmiModuleWorkerBuilder(
            IoModule ioModule,
            IoInnerModule innerIoModule, 
            ConfigModule configModule, 
            HandlerManagerModule handlers,
            ExecutorModule executorModule,
            TaskManagerModule taskManagerModule){
        
        this.ioModule = ioModule;
        this.innerIoModule = innerIoModule;
        this.configModule = configModule;
        this.handlers = handlers;
        this.executorModule = executorModule;
        this.taskManagerModule = taskManagerModule;
    }
    
    @Override
    public RmiModule buildModule() {
        RmiModule mod = new RmiModuleWorker(
                this.ioModule, 
                this.innerIoModule, 
                this.configModule, 
                this.handlers, 
                this.executorModule, 
                this.taskManagerModule);
        Beam.saveRmiInterfacesInStaticContext(mod);
        mod.exportInterfaces();
        return mod;
    }
}
