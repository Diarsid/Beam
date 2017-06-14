/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.console;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;

/**
 *
 * @author Diarsid
 */
public interface ConsolePlatform {
    
    ConsolePrinter printer();
    
    ConsoleReader reader();
    
    String name();
    
    void executeCommand(String commandLine);
    
    void reportException(IOException e);
    
    void stop();
    
    void acceptInitiator(Initiator initiator);
}
