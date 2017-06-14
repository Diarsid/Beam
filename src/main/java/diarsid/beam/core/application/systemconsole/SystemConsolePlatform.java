/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.systemconsole;

import java.io.IOException;
import java.rmi.RemoteException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.console.ConsolePlatform;
import diarsid.beam.core.base.control.io.console.ConsolePrinter;
import diarsid.beam.core.base.control.io.console.ConsoleReader;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;

import static diarsid.beam.core.application.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.application.systemconsole.SystemConsole.getPassport;


class SystemConsolePlatform implements ConsolePlatform {

    private static RemoteCoreAccessEndpoint remoteAccess;  
    
    private final ConsolePrinter printer;
    private final ConsoleReader reader;    
    private Initiator initiator;            

    SystemConsolePlatform(
            ConsolePrinter printer, 
            ConsoleReader reader, 
            RemoteCoreAccessEndpoint access) {
        remoteAccess = access;
        this.printer = printer;
        this.reader = reader;
    }

    @Override
    public ConsolePrinter printer() {
        return this.printer;
    }

    @Override
    public ConsoleReader reader() {
        return this.reader;
    }

    @Override
    public String name() {
        return getPassport().getName();
    }

    @Override
    public void executeCommand(String commandLine) {
        try {
            remoteAccess.executeCommand(initiator, commandLine);
        } catch (RemoteException e) {
            this.reportException(e);
        }
    }

    @Override
    public void reportException(IOException e) {
        this.printer.print(e);
    }

    @Override
    public void stop() { 
        exitSystemConsole();
    }

    @Override
    public void acceptInitiator(Initiator initiator) {
        this.initiator = initiator;
        getPassport().setInitiatorId(initiator.identity());   
    }
    
}
