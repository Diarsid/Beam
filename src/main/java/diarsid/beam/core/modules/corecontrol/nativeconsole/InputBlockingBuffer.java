/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.nativeconsole;

/**
 *
 * @author Diarsid
 */
public class InputBlockingBuffer 
        implements 
                InputEmitter, 
                InputReciever, 
                InputDistributor {
    
    private boolean isWaitingForAnswer;
    private final Object waitingForAnswerLock;
    
    public InputBlockingBuffer() {
        this.isWaitingForAnswer = false;
        this.waitingForAnswerLock = new Object();
    }

    @Override
    public String waitForCommand() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void submit(String commandLine) {
        synchronized ( this.waitingForAnswerLock ) {
            if ( this.isWaitingForAnswer ) {
                
                throw new UnsupportedOperationException("Not supported yet.");
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
            }        
        }
    }

    @Override
    public void consoleIsWaitingForAnswer() {
        synchronized ( this.waitingForAnswerLock ) {
            this.isWaitingForAnswer = true;
        }        
    }
}
