/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.engines;

import diarsid.beam.core.modules.web.ResourcesProvider;
import diarsid.beam.core.modules.web.ServletContainer;
import diarsid.beam.core.modules.web.ServletContainerProvider;
import diarsid.beam.shared.modules.ConfigModule;

/**
 *
 * @author Diarsid
 */
public class JettyServletContainerProvider implements ServletContainerProvider {
    
    private final ConfigModule config;
    private final ResourcesProvider resources;
    
    public JettyServletContainerProvider(
            ConfigModule config, ResourcesProvider resources) {
        
        this.config = config;
        this.resources = resources;
    }
    
    @Override
    public ServletContainer buildAndStartServer() {
        JettyServletContainer jetty = new JettyServletContainer(this.config);        
        jetty.addServlets(this.resources.getServlets());
        jetty.startServer();
        return jetty;
    }
}
