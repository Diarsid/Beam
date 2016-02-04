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
    
    private ScriptsUpdater() {
    }
    
    public static void main(String[] args) {
        System.out.println("Scripts updating...");
        ConfigModuleWorkerBuilder confBuilder = new ConfigModuleWorkerBuilder();
        ConfigModule config = confBuilder.buildModule();
        System.out.println("Configuration reading...");
        ScriptProvider scripts = new ScriptProvider(config, true);
        scripts.processScripts();
        pause();
    }
    
    static void pause() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }
    }
}
