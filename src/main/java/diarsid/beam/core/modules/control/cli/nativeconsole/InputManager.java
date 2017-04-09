/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli.nativeconsole;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Diarsid
 */
public class InputManager 
        implements 
                InputSource, 
                InputReciever, 
                InputDistributor {
    
    private boolean isInteractionLasting;
    private final Object interactionLock;
    private final BlockingQueue<String> response;
    private final BlockingQueue<String> command;
    
    public InputManager() {
        this.isInteractionLasting = false;
        this.interactionLock = new Object();
        this.response = new ArrayBlockingQueue<>(1, true);
        this.command = new ArrayBlockingQueue<>(1, true);
    }

    @Override
    public String waitForCommand() throws InterruptedException {
        return this.command.take();
    }
    
    @Override
    public String waitForResponse() throws InterruptedException {
        return this.response.take();
    }

    @Override
    public void waitAndSubmit(String commandLine) throws InterruptedException {
        synchronized ( this.interactionLock ) {
            if ( this.isInteractionLasting ) {
                this.response.put(commandLine);
            } else {
                this.command.put(commandLine);
            }        
        }
    }

    @Override
    public void interactionBegins() {
        synchronized ( this.interactionLock ) {
            this.isInteractionLasting = true;
        }        
    }
    
    @Override
    public void interactionEnds() {
        synchronized ( this.interactionLock ) {
            this.isInteractionLasting = false;
        }        
    }
}
