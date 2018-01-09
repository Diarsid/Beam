/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType;

/**
 *
 * @author Diarsid
 */
public abstract class ConsolePlatform {
    
    private final ConsoleIO io;
    private final ConsoleBlockingExecutor blockingExecutor;
    private final OuterIoEngineType platformOuterIoEngineType;           
    private final AtomicBoolean isInteractionLasts;   
    private final AtomicBoolean isWorking;
    private Initiator initiator;
    
    public ConsolePlatform(
            ConsoleIO consoleIo, 
            ConsoleBlockingExecutor blockingExecutor,
            OuterIoEngineType platformOuterIoEngineType) {
        this.io = consoleIo;
        this.blockingExecutor = blockingExecutor;
        this.platformOuterIoEngineType = platformOuterIoEngineType;        
        this.isInteractionLasts = new AtomicBoolean(false);
        this.isWorking = new AtomicBoolean(true);
    }
    
    public abstract String name(); 
    
    public abstract boolean isActiveWhenClosed();
    
    public final OuterIoEngineType type() {
        return this.platformOuterIoEngineType;
    }
    
    public final ConsoleIO io() {
        return this.io;
    }
    
    protected final boolean isInteractionLasts() {
        return this.isInteractionLasts.get();
    }
    
    protected Initiator initiator() {
        return this.initiator;
    }
    
    final boolean isWorking() {
        return this.isWorking.get();
    }
    
    final void interactionBegins() {
        this.isInteractionLasts.set(true);
    }
    
    final void interactionEnds() {
        this.isInteractionLasts.set(false);
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
        // callback when Initiator is accepted by this ConsolePlatform
    }
    
    public final void reportException(Exception e) {
        this.io.print(e);
    }
    
    public final void stop() { 
        this.isWorking.set(this.isActiveWhenClosed());
        this.whenStopped();
    }
    
    public void whenStopped() {
        // intended to be overriden to act as a kind of optional 
        // callback when this ConsolePlatform is being stopped
    }
}
