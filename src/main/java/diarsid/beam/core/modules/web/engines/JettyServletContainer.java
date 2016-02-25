/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.engines;

import java.util.EnumSet;
import java.util.HashSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import diarsid.beam.core.exceptions.ModuleInitializationException;

import diarsid.beam.core.modules.web.ServletContainer;

import diarsid.beam.shared.modules.ConfigModule;

import static diarsid.beam.shared.modules.config.Config.WEB_BEAM_CORE_CONTEXT_PATH;
import static diarsid.beam.shared.modules.config.Config.WEB_LOCAL_HOST;
import static diarsid.beam.shared.modules.config.Config.WEB_LOCAL_PORT;
import static diarsid.beam.shared.modules.config.Config.WEB_INTERNET_HOST;
import static diarsid.beam.shared.modules.config.Config.WEB_INTERNET_PORT;

/**
 *
 * @author Diarsid
 */
class JettyServletContainer implements ServletContainer {
    
    private final Server jettyServer;
    private final ServletContextHandler jettyContext;
    private final String internetConnector;
    private final String localConnector;
    
    JettyServletContainer(ConfigModule config) {     
        this.internetConnector = "internet_jetty_connector";
        this.localConnector = "localhost_jetty_connector";
        this.jettyServer = new Server();
        this.jettyContext = new ServletContextHandler(
                ServletContextHandler.NO_SESSIONS);
        
        this.configureServerAddresses(config);
        this.configureServerContext(config);
    }
    
    private void configureServerAddresses(ConfigModule config) {
        try {
            if (    ! config.get(WEB_LOCAL_HOST).isEmpty() ||
                    ! config.get(WEB_LOCAL_PORT).isEmpty()) {
                this.addInetAddress(
                        config.get(WEB_LOCAL_HOST), 
                        Integer.parseInt(config.get(WEB_LOCAL_PORT)),
                        this.localConnector);
            }
            if (    ! config.get(WEB_INTERNET_HOST).isEmpty() ||
                    ! config.get(WEB_INTERNET_PORT).isEmpty()) {
                this.addInetAddress(
                        config.get(WEB_INTERNET_HOST), 
                        Integer.parseInt(config.get(WEB_INTERNET_PORT)),
                        this.internetConnector);
            }
        } catch (NumberFormatException e) {
            throw new ModuleInitializationException(
                    "Web ports in config/config.xml have wrong format.");
        }
    }
    
    private void addInetAddress(String host, int port, String connectorName) {
        ServerConnector connector = new ServerConnector(this.jettyServer);
        connector.setHost(host);
        connector.setPort(port);
        connector.setName(connectorName);
        connector.setIdleTimeout(5000);
        this.jettyServer.addConnector(connector);
    }
    
    private void configureServerContext(ConfigModule config) {
        this.jettyContext.setContextPath(config.get(WEB_BEAM_CORE_CONTEXT_PATH));
        this.jettyServer.setHandler(this.jettyContext);
    }
    
    @Override 
    public void startServer() { 
        if ( this.jettyServer.getConnectors().length < 1 ) {
            throw new ModuleInitializationException(
                    "Jetty ServerConnectors have not been set.");
        }
        try {
            this.jettyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ModuleInitializationException(
                    "It is impossible to start Jetty Server.");
        } 
    }
    
    @Override
    public void addServlet(HttpServlet servlet, String servletUrlMapping) {
        this.jettyContext.addServlet(new ServletHolder(servlet), servletUrlMapping);
    }
    
    @Override
    public void addFilter(
            Filter filter, 
            String filterUrlMapping, 
            DispatcherType t1, 
            DispatcherType... types) {
        
        this.jettyContext.addFilter(
                new FilterHolder(filter), 
                filterUrlMapping, 
                EnumSet.of(t1, types));
    }
}
