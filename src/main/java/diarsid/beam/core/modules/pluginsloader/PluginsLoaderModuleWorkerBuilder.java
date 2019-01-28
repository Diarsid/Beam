/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.pluginsloader;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.PluginsLoaderModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import diarsid.beam.core.modules.BeamEnvironmentModule;

/**
 *
 * @author Diarsid
 */
class PluginsLoaderModuleWorkerBuilder implements GemModuleBuilder<PluginsLoaderModule> {
    
    private final IoModule ioModule;
    private final BeamEnvironmentModule componentsHolderModule;

    PluginsLoaderModuleWorkerBuilder(
            IoModule ioModule, BeamEnvironmentModule componentsHolderModule) {
        this.ioModule = ioModule;
        this.componentsHolderModule = componentsHolderModule;
    }
    
    @Override
    public PluginsLoaderModule buildModule() {
        InnerIoEngine ioEngine = this.ioModule.getInnerIoEngine();
        
        GooglePlugin googlePlugin = new GooglePlugin(ioEngine);
        
        PluginsLoaderModule pluginsLoaderModule = new PluginsLoaderModuleWorker(googlePlugin);
        this.componentsHolderModule.interpreter().install(pluginsLoaderModule.plugins());
        return pluginsLoaderModule;
    }
    
}
