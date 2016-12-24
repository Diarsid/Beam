/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.IOException;

import diarsid.beam.core.modules.ConfigModule;
import diarsid.beam.core.modules.config.ConfigModuleWorkerBuilder;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

/**
 *
 * @author Diarsid
 */
public class SystemConsole {
        
    private static RemoteOuterIoEngine SYSTEM_CONSOLE;
    
    private SystemConsole() {
    }
    
    public static void main(String[] args) throws IOException {
        try {
            ConfigModule config = new ConfigModuleWorkerBuilder().buildModule();
            ConsoleController console = new ConsoleController();
            ConsoleRemoteManager remoteManager = new ConsoleRemoteManager(config);
            remoteManager.export(console);
        } catch (StartupFailedException e) {
            e.printStackTrace();
            delayedShutdown();
        }
    }
    
    public static void saveRemoteOuterIoEngine(RemoteOuterIoEngine remoteOuterIoEngine) {
        System.out.println("Saving console remote in static");
        SYSTEM_CONSOLE = remoteOuterIoEngine;
    }
    
    private static void delayedShutdown() {
        try {
            sleep(10000);
            exit(-1);
        } catch (InterruptedException ex) {
            // nothing
        }
    }
    
    public static void exitSystemConsole() {
        Thread t = new Thread(() -> exit(0));
        t.setDaemon(true);
        t.start();
    }
}
