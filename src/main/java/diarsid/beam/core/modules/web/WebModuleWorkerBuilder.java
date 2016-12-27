/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.IoInnerModule;
import old.diarsid.beam.core.modules.WebModule;

import diarsid.beam.core.modules.web.engines.JettyServletContainerProvider;
import diarsid.beam.core.modules.web.resources.ResourcesProviderWorker;

import com.drs.gem.injector.module.GemModuleBuilder;

import diarsid.beam.core.modules.ConfigHolderModule;

/**
 *
 * @author Diarsid
 */
class WebModuleWorkerBuilder implements GemModuleBuilder<WebModule> {
    
    private final IoInnerModule ioEngine;
    private final ConfigHolderModule config;
    private final DataModule dataModule;
    
    WebModuleWorkerBuilder(
            IoInnerModule io, 
            ConfigHolderModule config, 
            DataModule dataModule) {
        
        this.ioEngine = io;
        this.config = config;
        this.dataModule = dataModule;
    }
    
    @Override
    public WebModule buildModule() {
        ResourcesProvider resources = new ResourcesProviderWorker(this.dataModule);
        ServletContainerProvider provider = new JettyServletContainerProvider(
                this.ioEngine, this.config, resources);
       
        return new WebModuleWorker(provider.buildAndStartServer());
    }
}
