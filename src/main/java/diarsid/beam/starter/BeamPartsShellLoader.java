/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.starter;

import java.io.IOException;
import java.util.List;

import diarsid.beam.external.sysconsole.SysConsole;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.ConfigModuleWorkerBuilder;

/**
 *
 * @author Diarsid
 */
class BeamPartsShellLoader {
    
    private BeamPartsShellLoader() {
    }
    
    public static void main(String[] args) throws IOException {
        ConfigModuleWorkerBuilder confBuilder = new ConfigModuleWorkerBuilder();
        ConfigModule config = confBuilder.buildModule();        
        BeamPartsRemoteLocator locator = new BeamPartsRemoteLocator(config);
        List<String> modulesToStart = locator.defineModulesToStart();        
        if ( modulesToStart.size() > 0 ) {
            BatchScriptsProvider scripts = new BatchScriptsProvider(config, false);
            scripts.processScripts();
            BatchScriptsExecutor starter = new BatchScriptsExecutor(scripts);
            if (modulesToStart.contains("beam")) {
                starter.runBeam();
                if (modulesToStart.contains("console")) {
                    while (!locator.isBeamWorking()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ie) {
                            System.out.println(ie.getMessage());
                        }
                    }                    
                    SysConsole.main(args);
                }
            } else if (modulesToStart.contains("console")) {
                SysConsole.main(args);
            }
        }      
    }
}
