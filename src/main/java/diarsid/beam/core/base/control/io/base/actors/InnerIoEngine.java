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

/**
 *
 * @author Diarsid
 */
public interface InnerIoEngine {
    
    String askInput(Initiator initiator, String inputQuestion);
    
    Choice ask(Initiator initiator, String yesOrNoQuestion);
    
    Answer ask(Initiator initiator, Question question);
    
    void report(Initiator initiator, String string);
    
    void reportAndExitLater(Initiator initiator, String string);
    
    void reportMessage(Initiator initiator, Message message);
    
    void reportMessageAndExitLater(Initiator initiator, Message message);
}
