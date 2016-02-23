/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.web;

import com.drs.beam.core.modules.WebModule;
import com.drs.beam.core.modules.web.engines.JettyServletContainer;
import com.drs.beam.shared.modules.ConfigModule;
import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class WebModuleWorkerBuilder implements GemModuleBuilder<WebModule> {
    
    private final ConfigModule config;
    
    WebModuleWorkerBuilder(ConfigModule config) {
        this.config = config;
    }
    
    @Override
    public WebModule buildModule() {
        BeamServletContainer server = new JettyServletContainer(config);
        server.startServer();
        WebModule webModule = new WebModuleWorker(server);
        return webModule;
    }
}
