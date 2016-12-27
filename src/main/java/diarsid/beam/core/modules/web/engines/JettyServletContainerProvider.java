/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.engines;

import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.web.ResourcesProvider;
import diarsid.beam.core.modules.web.ServletContainer;
import diarsid.beam.core.modules.web.ServletContainerProvider;
import diarsid.beam.core.modules.ConfigHolderModule;

/**
 *
 * @author Diarsid
 */
public class JettyServletContainerProvider implements ServletContainerProvider {
    
    private final IoInnerModule ioEngine;
    private final ConfigHolderModule config;
    private final ResourcesProvider resources;
    
    public JettyServletContainerProvider(
            IoInnerModule io, ConfigHolderModule config, ResourcesProvider resources) {
        
        this.ioEngine = io;
        this.config = config;
        this.resources = resources;
    }
    
    @Override
    public ServletContainer buildAndStartServer() {
        JettyServletContainer jetty = 
                new JettyServletContainer(this.ioEngine, this.config);        
        jetty.addServlets(this.resources.getServlets());
        jetty.startServer();
        return jetty;
    }
}
