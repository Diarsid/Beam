/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.modules.rmi;

import diarsid.beam.core.Beam;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.ExecutorModule;
import old.diarsid.beam.core.modules.IoInnerModule;
import old.diarsid.beam.core.modules.RmiModule;
import old.diarsid.beam.core.modules.TaskManagerModule;

import diarsid.beam.core.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import old.diarsid.beam.core.modules.OldIoModule;

/**
 *
 * @author Diarsid
 */
class RmiModuleWorkerBuilder implements GemModuleBuilder<RmiModule> {
    
    private final OldIoModule ioModule;
    private final IoInnerModule innerIoModule;
    private final ConfigModule configModule;
    private final DataModule dataModule;
    private final ExecutorModule executorModule;
    private final TaskManagerModule taskManagerModule;
    
    RmiModuleWorkerBuilder(
            OldIoModule ioModule,
            IoInnerModule innerIoModule, 
            ConfigModule configModule, 
            DataModule dataModule,
            ExecutorModule executorModule,
            TaskManagerModule taskManagerModule){
        
        this.ioModule = ioModule;
        this.innerIoModule = innerIoModule;
        this.configModule = configModule;
        this.dataModule = dataModule;
        this.executorModule = executorModule;
        this.taskManagerModule = taskManagerModule;
    }
    
    @Override
    public RmiModule buildModule() {
        RmiModule mod = new RmiModuleWorker(
                this.ioModule, 
                this.innerIoModule, 
                this.configModule, 
                this.dataModule, 
                this.executorModule, 
                this.taskManagerModule);
        Beam.saveRmiInterfacesInStaticContext(mod);
        mod.exportInterfaces();
        return mod;
    }
}
