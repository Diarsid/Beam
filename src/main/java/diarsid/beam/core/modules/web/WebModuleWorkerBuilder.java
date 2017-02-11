/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.WebModule;
import diarsid.beam.core.modules.web.core.container.AbstractDispatcherServlet;
import diarsid.beam.core.modules.web.core.container.NamedResources;
import diarsid.beam.core.modules.web.core.container.ResourceServletContainer;
import diarsid.beam.core.modules.web.core.container.Resources;
import diarsid.beam.core.modules.web.engines.JettyResourceServletContainer;
import diarsid.beam.core.modules.web.resources.BeamDispatcherServlet;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class WebModuleWorkerBuilder implements GemModuleBuilder<WebModule> {
    
    private final IoModule ioModule;
    private final ApplicationComponentsHolderModule applicationComponentsHolderModule;
    private final DomainKeeperModule domainKeeperModule;
    
    WebModuleWorkerBuilder(
            IoModule io, 
            ApplicationComponentsHolderModule applicationComponentsHolderModule,
            DomainKeeperModule domainKeeperModule) {        
        this.ioModule = io;
        this.applicationComponentsHolderModule = applicationComponentsHolderModule;
        this.domainKeeperModule = domainKeeperModule;
    }
    
    @Override
    public WebModule buildModule() {
        Resources resources;
        NamedResources namedResources;
        InnerIoEngine ioEngine;
        Configuration configuration;
        
        AbstractDispatcherServlet dispatcherServlet;
        ResourceServletContainer container;
        
        resources = new Resources();
        namedResources = new NamedResources(resources);
        
        ioEngine = this.ioModule.getInnerIoEngine();
        configuration = this.applicationComponentsHolderModule.getConfiguration();
        
        dispatcherServlet = new BeamDispatcherServlet(namedResources);
        container = new JettyResourceServletContainer(ioEngine, configuration);
        
        container.install(dispatcherServlet, resources);
        container.startServer();
        
        return new WebModuleWorker(container);
    }
}
