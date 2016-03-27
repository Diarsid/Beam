/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import diarsid.beam.core.modules.HandlerManagerModule;
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
    private final HandlerManagerModule handlers;
    
    WebModuleWorkerBuilder(ConfigModule config, HandlerManagerModule handlers) {
        this.config = config;
        this.handlers = handlers;
    }
    
    @Override
    public WebModule buildModule() {
        ResourcesProvider resources = new ResourcesProviderWorker(this.handlers);
        ServletContainerProvider provider = 
                new JettyServletContainerProvider(this.config, resources);
       
        return new WebModuleWorker(provider.buildAndStartServer());
    }
}
