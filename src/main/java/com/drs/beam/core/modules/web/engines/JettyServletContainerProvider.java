/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.web.engines;

import java.util.Map;

import javax.servlet.http.HttpServlet;

import com.drs.beam.core.modules.web.ResourcesProvider;
import com.drs.beam.core.modules.web.ServletContainer;
import com.drs.beam.core.modules.web.ServletContainerProvider;
import com.drs.beam.shared.modules.ConfigModule;

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
        for (Map.Entry<String, HttpServlet> entry : this.resources.getServlets().entrySet()) {
            jetty.addServlet(entry.getValue(), entry.getKey());
        }
        jetty.startServer();
        return jetty;
    }
}
