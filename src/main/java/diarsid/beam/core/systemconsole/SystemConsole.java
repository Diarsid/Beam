/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.File;
import java.io.IOException;

import diarsid.beam.core.config.ConfigFileReader;
import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.exceptions.WorkflowBrokenException;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

import static diarsid.beam.core.Beam.CONFIG_FILE;
import static diarsid.beam.core.systemconsole.SystemIO.provideReader;
import static diarsid.beam.core.systemconsole.SystemIO.provideWriter;

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
            File configXML = new File(CONFIG_FILE);
            ConfigFileReader configReader = new ConfigFileReader();
            Configuration configuration = configReader.readConfigurationFile(configXML);
            ConsolePrinter printer = new ConsolePrinter(provideWriter());
            ConsoleReader reader = new ConsoleReader(provideReader());
            ConsoleController console = new ConsoleController(printer, reader);
            ConsoleRemoteManager remoteManager = new ConsoleRemoteManager(configuration);
            remoteManager.export(console);
        } catch (StartupFailedException|WorkflowBrokenException e) {
            e.getCause().printStackTrace();
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
