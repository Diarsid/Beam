/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.console.Console;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.lang.Integer.parseInt;
import static java.lang.System.exit;
import static java.lang.Thread.sleep;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.application.systemconsole.SystemIO.provideReader;
import static diarsid.beam.core.application.systemconsole.SystemIO.provideWriter;
import static diarsid.beam.core.base.control.io.base.console.Console.buildConsoleUsing;
import static diarsid.beam.core.base.rmi.RmiComponentNames.SYS_CONSOLE_NAME;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class SystemConsole {
    
    private static final ConsolePassport PASSPORT;
    
    static {
        PASSPORT = new ConsolePassport();
    }
    
    private SystemConsole() {
    }
    
    public static void main(String[] args) throws IOException {
        try {
            createConsolePassport();            
            ConsoleRemoteManager remoteManager = createRemoteManager();
            Console console = createConsole(remoteManager);            
            remoteManager.export(console);
            asyncDoIndependently(console);
        } catch (StartupFailedException|WorkflowBrokenException e) {
            logError(SystemConsole.class, e.getCause());
            delayedShutdown();
        }
    }

    private static void createConsolePassport() throws NumberFormatException {
        PASSPORT.setName(SYS_CONSOLE_NAME);
        PASSPORT.setPort(parseInt(configuration().asString("rmi.sysconsole.port")));
    }

    private static Console createConsole(ConsoleRemoteManager remoteManager) {
        SystemConsolePrinter printer = new SystemConsolePrinter(provideWriter());
        SystemConsoleReader reader = new SystemConsoleReader(provideReader());
        SystemConsoleBlockingExecutor blockingExecutor = 
                new SystemConsoleBlockingExecutor(remoteManager.importRemoteAccess());
        SystemConsolePlatform platform = new SystemConsolePlatform(
                printer, reader, blockingExecutor);
        Console console = buildConsoleUsing(platform);
        return console;
    }

    private static ConsoleRemoteManager createRemoteManager() {
        return new ConsoleRemoteManager(configuration());
    }
    
    static ConsolePassport getPassport() {
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
