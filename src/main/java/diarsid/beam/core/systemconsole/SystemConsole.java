/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.IOException;

import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.exceptions.WorkflowBrokenException;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static java.lang.Integer.parseInt;
import static java.lang.System.exit;
import static java.lang.Thread.sleep;

import static diarsid.beam.core.config.Config.SYS_CONSOLE_NAME;
import static diarsid.beam.core.config.Config.SYS_CONSOLE_PORT;
import static diarsid.beam.core.config.Configuration.getConfiguration;
import static diarsid.beam.core.systemconsole.SystemIO.provideReader;
import static diarsid.beam.core.systemconsole.SystemIO.provideWriter;
import static diarsid.beam.core.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class SystemConsole {
        
    private static RemoteOuterIoEngine SYSTEM_CONSOLE;
    private static final ConsolePassport PASSPORT;
    
    static {
        PASSPORT = new ConsolePassport();
    }
    
    private SystemConsole() {
    }
    
    public static void main(String[] args) throws IOException {
        try {
            Configuration configuration = getConfiguration();
            PASSPORT.setName(configuration.get(SYS_CONSOLE_NAME));
            PASSPORT.setPort(parseInt(configuration.get(SYS_CONSOLE_PORT)));
            ConsolePrinter printer = new ConsolePrinter(provideWriter());
            ConsoleReader reader = new ConsoleReader(provideReader());
            ConsoleController console = new ConsoleController(printer, reader);
            ConsoleRemoteManager remoteManager = new ConsoleRemoteManager(configuration);
            remoteManager.export(console);
        } catch (StartupFailedException|WorkflowBrokenException e) {
            logError(SystemConsole.class, e.getCause());
            logError(SystemConsole.class, e);
            delayedShutdown();
        }
    }
    
    public static void saveRemoteOuterIoEngine(RemoteOuterIoEngine remoteOuterIoEngine) {
        SYSTEM_CONSOLE = remoteOuterIoEngine;
    }
    
    public static ConsolePassport getPassport() {
        return PASSPORT;
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
