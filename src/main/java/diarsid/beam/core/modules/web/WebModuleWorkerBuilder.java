/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;


import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.WebModule;
import diarsid.beam.core.modules.domainkeeper.WebDirectoriesKeeper;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;
import diarsid.beam.core.modules.web.core.container.ExceptionToJsonMapper;
import diarsid.beam.core.modules.web.core.container.ResourceDispatcherServlet;
import diarsid.beam.core.modules.web.core.container.ResourceServletContainer;
import diarsid.beam.core.modules.web.core.container.Resources;
import diarsid.beam.core.modules.web.service.resources.AllDirectoriesResource;
import diarsid.beam.core.modules.web.service.resources.AllPagesResource;
import diarsid.beam.core.modules.web.service.resources.SingleDirectoryPropertyResource;
import diarsid.beam.core.modules.web.service.resources.SingleDirectoryResource;
import diarsid.beam.core.modules.web.service.resources.SinglePagePropertyResource;
import diarsid.beam.core.modules.web.service.resources.SinglePageResource;
import diarsid.beam.core.modules.web.service.resources.WebObjectsValidationResource;

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
        InnerIoEngine ioEngine;
        Configuration configuration;           
        
        ExceptionToJsonMapper exceptionMapper;
        ResourceDispatcherServlet dispatcherServlet;
        ResourceServletContainer container;        
        
        WebDirectoriesKeeper webDirectoriesKeeper = this.domainKeeperModule.webDirectories();
        WebPagesKeeper webPagesKeeper = this.domainKeeperModule.webPages();
        resources = new Resources(
                new AllDirectoriesResource(webDirectoriesKeeper),
                new AllPagesResource(webPagesKeeper),
                new SingleDirectoryResource(webDirectoriesKeeper),
                new SingleDirectoryPropertyResource(webDirectoriesKeeper),
                new SinglePageResource(webPagesKeeper),
                new SinglePagePropertyResource(webPagesKeeper),
                new WebObjectsValidationResource());
        
        exceptionMapper = new ExceptionToJsonMapper();
        
        ioEngine = this.ioModule.getInnerIoEngine();
        configuration = this.applicationComponentsHolderModule.getConfiguration();
        
        dispatcherServlet = new ResourceDispatcherServlet(resources, exceptionMapper);
        container = new JettyResourceServletContainer(ioEngine, configuration);
        
        container.install(dispatcherServlet, resources);
        container.startServer();
        
        return new WebModuleWorker(container);
    }
}
