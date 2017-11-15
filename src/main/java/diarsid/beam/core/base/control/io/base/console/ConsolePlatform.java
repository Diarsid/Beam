/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType;

/**
 *
 * @author Diarsid
 */
public abstract class ConsolePlatform {
    
    protected final ConsolePrinter printer;
    protected final ConsoleReader reader;
    protected final ConsoleBlockingExecutor blockingExecutor;
    protected final OuterIoEngineType platformOuterIoEngineType;
    protected Initiator initiator;
    
    public ConsolePlatform(
            ConsolePrinter printer, 
            ConsoleReader reader, 
            ConsoleBlockingExecutor blockingExecutor,
            OuterIoEngineType platformOuterIoEngineType) {
        this.printer = printer;
        this.reader = reader;
        this.blockingExecutor = blockingExecutor;
        this.platformOuterIoEngineType = platformOuterIoEngineType;
    }
    
    public OuterIoEngineType type() {
        return this.platformOuterIoEngineType;
    }
    
    public final ConsolePrinter printer() {
        return this.printer;
    }
    
    public final ConsoleReader reader() {
        return this.reader;
    }
    
    public final void blockingExecuteCommand(String commandLine) throws Exception {
        this.blockingExecutor.blockingExecuteCommand(this.initiator, commandLine);
    }
    
    public final void acceptInitiator(Initiator initiator) {
        this.initiator = initiator;
        this.whenInitiatorAccepted();
    }
    
    public void whenInitiatorAccepted() {
        // intended to be overriden to act as a kind of optional 
        // callback when Initiator is accepted by this ConsolePlatform/
    }
    
    public final void reportException(Exception e) {
        this.printer.print(e);
    }
    
    public abstract String name();    
    
    public abstract void stop();
}
