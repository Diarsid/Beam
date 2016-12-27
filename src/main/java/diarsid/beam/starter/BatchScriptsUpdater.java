/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.starter;


import diarsid.beam.core.modules.config.ConfigHolderModuleWorkerBuilder;
import diarsid.beam.core.modules.ConfigHolderModule;

/**
 *
 * @author Diarsid
 */
class BatchScriptsUpdater {
    
    private BatchScriptsUpdater() {
    }
    
    public static void main(String[] args) {
        System.out.println("Scripts updating...");
        ConfigHolderModuleWorkerBuilder confBuilder = new ConfigHolderModuleWorkerBuilder();
        ConfigHolderModule config = confBuilder.buildModule();
        System.out.println("Configuration reading...");
        BatchScriptsProvider scripts = new BatchScriptsProvider(config, true);
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
