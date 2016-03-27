/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import diarsid.beam.core.entities.Location;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.HandlerManagerModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.os.OSProvider;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorkerBuilder implements GemModuleBuilder<ExecutorModule> {
    
    private final DataModule dataModule;
    private final HandlerManagerModule handlers;
    private final IoInnerModule ioInnerModule;
    private final ConfigModule configModule;
    
    ExecutorModuleWorkerBuilder(
            IoInnerModule io, 
            DataModule dataModule, 
            HandlerManagerModule handlers,
            ConfigModule configModule) {
        
        this.dataModule = dataModule;
        this.handlers = handlers;
        this.ioInnerModule = io;
        this.configModule = configModule;
    }
    
    @Override
    public ExecutorModule buildModule() {        
        IntelligentResolver intell = 
                new IntelligentResolver(this.dataModule, this.ioInnerModule);
        OS os = OSProvider.getOS(this.ioInnerModule, this.configModule); 
        Location notes = new Location(
                "notes", 
                this.configModule.get(Config.NOTES_LOCATION));
        
        return new ExecutorModuleWorker(
                this.ioInnerModule, 
                this.dataModule.getCommandsDao(), 
                this.handlers, 
                intell, 
                os, 
                notes);
    }
}
