/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.actors;

import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.Question;

import java.io.IOException;

/**
 *
 * @author Diarsid
 */
public interface OuterIoEngine {
    
    String getName() throws IOException;
    
    String askForInput(String inputRequest) throws IOException;
    
    Choice resolveYesOrNo(String yesOrNoQuestion) throws IOException;
    
    Answer resolveQuestion(Question question) throws IOException;
    
    void report(String string) throws IOException;
    
    void reportMessage(Message message) throws IOException;
    
    void close() throws IOException;
    
    void acceptInitiator(Initiator initiator) throws IOException;
}
