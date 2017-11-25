/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import diarsid.beam.core.base.control.io.base.console.Console;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;

import static java.lang.Integer.parseInt;
import static java.lang.System.exit;
import static java.lang.Thread.sleep;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
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
    private static RemoteCoreAccessEndpoint remoteAccess;  
    
    static {
        PASSPORT = new ConsolePassport();
    }
    
    private SystemConsole() {
    }
    
    public static void main(String[] args) throws IOException {
        try {
            createConsolePassport();            
            ConsoleRemoteManager remoteManager = createRemoteManager();
            remoteAccess = remoteManager.importRemoteAccess();
            Console console = createConsole();            
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

    private static Console createConsole() {
        SystemConsoleIO consoleIo = new SystemConsoleIO(writer(), reader());
        SystemConsolePlatform platform = new SystemConsolePlatform(consoleIo, blockingExecutor());
        Console console = buildConsoleUsing(platform);
        return console;
    }

    private static ConsoleRemoteManager createRemoteManager() {
        return new ConsoleRemoteManager(configuration());
    }
    
    private static ConsoleBlockingExecutor blockingExecutor() {
        return (initiator, commandLine) -> {
            remoteAccess.blockingExecuteCommand(initiator, commandLine);
        };
    }
    
    static BufferedWriter writer() {
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(System.console().writer());
        } catch (NullPointerException e) {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        return bufferedWriter;
    }

    static BufferedReader reader() {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(System.console().reader());
        } catch (NullPointerException e) {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        }
        return bufferedReader;
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
