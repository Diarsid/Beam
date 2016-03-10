/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import diarsid.beam.core.modules.WebModule;


/**
 *
 * @author Diarsid
 */
class WebModuleWorker implements WebModule {
    
    private final ServletContainer server;
    
    WebModuleWorker(ServletContainer server) {
       this.server = server;
    }
}
