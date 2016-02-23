/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.web.engines;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.drs.beam.core.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.web.BeamServletContainer;
import com.drs.beam.shared.modules.ConfigModule;
import com.drs.beam.shared.modules.config.Config;

import static com.drs.beam.shared.modules.config.Config.WEB_LOCAL_HOST;
import static com.drs.beam.shared.modules.config.Config.WEB_LOCAL_PORT;
import static com.drs.beam.shared.modules.config.Config.WEB_INTERNET_HOST;
import static com.drs.beam.shared.modules.config.Config.WEB_INTERNET_PORT;

/**
 *
 * @author Diarsid
 */
public class JettyServletContainer implements BeamServletContainer {
    
    private Server jetty;
    
    public JettyServletContainer(ConfigModule config) {        
        this.jetty = new Server();
                
        try {
            if (    ! config.get(WEB_LOCAL_HOST).isEmpty() ||
                    ! config.get(WEB_LOCAL_PORT).isEmpty()) {
                this.addInetAddress(
                        config.get(WEB_LOCAL_HOST), 
                        Integer.parseInt(config.get(WEB_LOCAL_PORT)));
            }
            if (    ! config.get(WEB_INTERNET_HOST).isEmpty() ||
                    ! config.get(WEB_INTERNET_PORT).isEmpty()) {
                this.addInetAddress(
                        config.get(WEB_INTERNET_HOST), 
                        Integer.parseInt(config.get(WEB_INTERNET_PORT)));
            }
        } catch (NumberFormatException e) {
            throw new ModuleInitializationException(
                    "Web ports in config/config.xml have wrong format.");
        }
    }
    
    private void addInetAddress(String host, int port) {
        ServerConnector connector = new ServerConnector(this.jetty);
        connector.setHost(host);
        connector.setPort(port);
        connector.setIdleTimeout(5000);
        this.jetty.addConnector(connector);
    }
    
    @Override 
    public void startServer() { 
        if ( this.jetty.getConnectors().length < 1 ) {
            throw new ModuleInitializationException(
                    "Jetty ServerConnectors have not been set.");
        }
        try {
            this.jetty.start();
        } catch (Exception e) {
            throw new ModuleInitializationException(
                    "It is impossible to start Jetty Server.");
        } 
    }
}
