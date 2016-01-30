/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.starter;

import com.drs.beam.shared.modules.ConfigModule;
import com.drs.beam.shared.modules.config.ConfigModuleWorkerBuilder;

/**
 *
 * @author Diarsid
 */
class ScriptsUpdater {
    
    ScriptsUpdater() {
    }
    
    public static void main(String[] args) {
        System.out.println("Scripts updating...");
        ConfigModuleWorkerBuilder confBuilder = new ConfigModuleWorkerBuilder();
        ConfigModule config = confBuilder.buildModule();
        System.out.println("Configuration reading...");
        ScriptProvider scripts = new ScriptProvider(config);
        scripts.processScripts();
        System.out.println("Scripts updated.");
    }
}
