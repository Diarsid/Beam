/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.executor;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.executor.os.OSProvider;
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
            IoInnerModule io, DataModule dataModule, ConfigModule configModule) {
        
        this.dataModule = dataModule;
        this.ioInnerModule = io;
        this.configModule = configModule;
    }
    
    @Override
    public ExecutorModule buildModule() {        
        OS os = OSProvider.getOS(this.ioInnerModule, this.configModule);
        return new ExecutorModuleWorker(this.ioInnerModule, this.dataModule, os);
    }
}
