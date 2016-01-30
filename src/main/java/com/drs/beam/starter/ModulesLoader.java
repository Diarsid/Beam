/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.starter;

import java.io.IOException;
import java.util.List;

import com.drs.beam.shared.modules.ConfigModule;
import com.drs.beam.shared.modules.config.ConfigModuleWorkerBuilder;

/**
 *
 * @author Diarsid
 */
public class ModulesLoader {
    
    public static void main(String[] args) throws IOException {
        ConfigModuleWorkerBuilder confBuilder = new ConfigModuleWorkerBuilder();
        ConfigModule config = confBuilder.buildModule();        
        RemoteLocator locator = new RemoteLocator(config);
        List<String> modulesToStart = locator.defineModulesToStart();        
        if ( modulesToStart.size() > 0 ) {
            ScriptProvider scripts = new ScriptProvider(config);
            scripts.processScripts();
            Starter starter = new Starter(scripts);
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
                    starter.runConsole();
                }
            } else if (modulesToStart.contains("console")) {
                starter.runConsole();
            }
        }        
    }
}
