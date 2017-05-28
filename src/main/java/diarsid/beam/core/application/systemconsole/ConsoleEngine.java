/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.systemconsole;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.commands.Command;

/**
 *
 * @author Diarsid
 */
public interface ConsoleEngine {
    
    String read();
    
    String readyAndWaitForLine();
    
    void print(String report);
    
    void print(Message report);
    
    void print(VariantsQuestion question);
    
    void printYesNoQuestion(String question);
    
    void printInvite(String inviteMessage);
    
    boolean isWorking();
    
    void interactionBegins();
    
    void interactionEnds();
    
    void execute(Command command);
    
    void acceptInitiator(Initiator initiator);
    
    void closeEngine();
}
