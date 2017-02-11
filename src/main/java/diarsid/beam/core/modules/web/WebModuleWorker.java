/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import diarsid.beam.core.modules.WebModule;
import diarsid.beam.core.modules.web.core.container.ResourceServletContainer;


/**
 *
 * @author Diarsid
 */
class WebModuleWorker implements WebModule {
    
    private final ResourceServletContainer server;
    
    WebModuleWorker(ResourceServletContainer server) {
       this.server = server;
    }
    
    @Override
    public void stopModule() {
        this.server.stopServer();
    }
}
