/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.WebModule;
import diarsid.beam.core.modules.web.engines.JettyServletContainerProvider;
import diarsid.beam.core.modules.web.resources.ResourcesProviderWorker;
import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class WebModuleWorkerBuilder implements GemModuleBuilder<WebModule> {
    
    private final ConfigModule config;
    private final DataModule dataModule;
    
    WebModuleWorkerBuilder(ConfigModule config, DataModule dataModule) {
        this.config = config;
        this.dataModule = dataModule;
    }
    
    @Override
    public WebModule buildModule() {
        ResourcesProvider resources = new ResourcesProviderWorker(this.dataModule);
        ServletContainerProvider provider = 
                new JettyServletContainerProvider(this.config, resources);
       
        return new WebModuleWorker(provider.buildAndStartServer());
    }
}
