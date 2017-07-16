/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.web.core.container.ResourceDispatcherServlet;
import diarsid.beam.core.modules.web.core.container.ResourceServletContainer;
import diarsid.beam.core.modules.web.core.container.Resources;

import static diarsid.beam.core.base.util.Logs.logError;

import diarsid.beam.core.modules.web.core.jsonconversion.Objectivizer;

import static diarsid.beam.core.Beam.systemInitiator;

/**
 *
 * @author Diarsid
 */
class JettyResourceServletContainer extends ResourceServletContainer {
    
    private final InnerIoEngine ioEngine;
    private final Server jettyServer;
    private final ServletContextHandler jettyContext;
    private final String internetConnectorName;
    private final String localConnectorName;
    
    JettyResourceServletContainer(InnerIoEngine io, Configuration config, Objectivizer jsonizer) {     
        super(jsonizer);
        this.ioEngine = io;
        this.internetConnectorName = "internet_jetty_connector";
        this.localConnectorName = "localhost_jetty_connector";
        this.jettyServer = new Server();
        
        this.jettyContext = new ServletContextHandler(
                ServletContextHandler.NO_SESSIONS);   
        this.jettyContext.setContextPath(config.asString("web.local.path"));
        
        this.jettyServer.setHandler(this.jettyContext);
        this.configureServerAddresses(config);
        
        this.jettyServer.setStopAtShutdown(true);
    }
    
    private void configureServerAddresses(Configuration config) {
        try {
            String localHost = config.asString("web.local.host");
            int localPort = Integer.parseInt(config.asString("web.local.port"));
            this.addInetAddress(localHost, localPort, this.localConnectorName);
            if ( config.hasString("web.internet.host") && config.hasString("web.internet.port") ) {
                String internetHost = config.asString("web.internet.host");
                int internetPort = Integer.parseInt(config.asString("web.internet.port"));
                this.addInetAddress(internetHost, internetPort, this.internetConnectorName);
            }
        } catch (NumberFormatException e) {
            throw new ModuleInitializationException(
                    "Web ports in beam.config have wrong format.");
        }
    }
    
    private void addInetAddress(String host, int port, String connectorName) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        HttpConnectionFactory http1fConFactory = 
                new HttpConnectionFactory(httpConfig);
        HTTP2CServerConnectionFactory http2ConFactory = 
                new HTTP2CServerConnectionFactory(httpConfig);
        ServerConnector connector = new ServerConnector(
                this.jettyServer, http1fConFactory, http2ConFactory);
        connector.setHost(host);
        connector.setPort(port);
        connector.setName(connectorName);
        connector.setReuseAddress(true);
        connector.setIdleTimeout(1000*60*60);
        this.jettyServer.addConnector(connector);
    }    
    
    @Override 
    public void startServer() { 
        if ( this.jettyServer.getConnectors().length < 1 ) {
            this.ioEngine.reportAndExitLater(systemInitiator(), "Jetty ServerConnectors have not been set.");
            throw new ModuleInitializationException(
                    "Jetty ServerConnectors have not been set.");
        }
        try {
            this.jettyServer.start();
        } catch (Exception e) {
            logError(this.getClass(), e);
            this.ioEngine.reportAndExitLater(systemInitiator(), "It is impossible to start Jetty Server.");
            throw new ModuleInitializationException(
                    "It is impossible to start Jetty Server.");
        } 
    }
    
    @Override
    public void stopServer() {
        try {
            this.jettyServer.stop();
        } catch (Exception e) {
            logError(this.getClass(), e);            
            this.ioEngine.report(systemInitiator(), "Jetty Server fails to stop.");
        }        
    }
    
    @Override
    public void install(ResourceDispatcherServlet dispatcher, Resources resources) {
        this.jettyContext.addServlet(new ServletHolder("{dispatcher}", dispatcher), "/*");
        resources.doForEach(resource -> {
            this.jettyContext.addServlet(
                    new ServletHolder(resource.name(), resource), resource.url());
        });
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
