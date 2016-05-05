/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.os.OSProvider;
import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorkerBuilder implements GemModuleBuilder<ExecutorModule> {
    
    private final DataModule dataModule;
    private final IoInnerModule ioInnerModule;
    private final ConfigModule configModule;
    
    ExecutorModuleWorkerBuilder(
            IoInnerModule io, 
            DataModule dataModule, 
            ConfigModule configModule) {
        
        this.dataModule = dataModule;
        this.ioInnerModule = io;
        this.configModule = configModule;
    }
    
    @Override
    public ExecutorModule buildModule() {        
        IntelligentResolver intell = 
                new IntelligentResolver(this.dataModule, this.ioInnerModule);
        OS os = OSProvider.getOS(this.ioInnerModule, this.configModule); 
        ProcessorsBuilder processorsBuilder = new ProcessorsBuilder(
                this.ioInnerModule, 
                this.dataModule, 
                this.configModule, 
                intell, 
                os);        
        return new ExecutorModuleWorker(
                this.ioInnerModule, intell, processorsBuilder);
    }
}
