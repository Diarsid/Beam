/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.web;

import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.WebModule;
import com.drs.beam.shared.modules.ConfigModule;

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
