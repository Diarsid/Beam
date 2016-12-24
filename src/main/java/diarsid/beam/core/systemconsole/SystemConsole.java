/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import diarsid.beam.core.modules.ConfigModule;
import diarsid.beam.core.modules.config.ConfigModuleWorkerBuilder;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

/**
 *
 * @author Diarsid
 */
public class SystemConsole {
    
    private static RemoteOuterIoEngine SYSTEM_CONSOLE;
    
    private SystemConsole() {
    }
    
    public static void main(String[] args) {
        ConfigModule configModule = new ConfigModuleWorkerBuilder().buildModule();
        
    }
    
    public static void saveRemoteOuterIoEngine(RemoteOuterIoEngine remoteOuterIoEngine) {
        SYSTEM_CONSOLE = remoteOuterIoEngine;
    }
}
