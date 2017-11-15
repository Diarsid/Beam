/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import diarsid.beam.core.base.control.io.base.console.ConsoleReader;

/**
 *
 * @author Diarsid
 */
class ConsoleBlockingOutcome implements ConsoleReader {
    
    private final BlockingQueue<String> fromConsole;
    
    ConsoleBlockingOutcome() {
        this.fromConsole = new ArrayBlockingQueue<>(1, true);
    }
    
    void sendNewLine(String line) throws InterruptedException{
        this.fromConsole.put(line);   
    }

    @Override
    public String readLine() throws IOException {
        try {
            return this.fromConsole.take();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
    
}
