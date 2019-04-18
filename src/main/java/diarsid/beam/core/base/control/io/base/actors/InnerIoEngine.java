/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.actors;

import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.HelpKey;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

/**
 *
 * @author Diarsid
 */
public interface InnerIoEngine {
    
    String askInput(Initiator initiator, String inputQuestion, Help help);
    
    Choice ask(Initiator initiator, String yesOrNoQuestion, Help help);
    
    Answer ask(Initiator initiator, VariantsQuestion question, Help help);
    
    Answer ask(Initiator initiator, WeightedVariants variants, Help help);
    
    HelpKey addToHelpContext(String... help);
    
    HelpKey addToHelpContext(List<String> help);
    
    void report(Initiator initiator, String string);
    
    void reportAndExitLater(Initiator initiator, String string);
    
    void reportMessage(Initiator initiator, Message message);
    
    void reportMessageAndExitLater(Initiator initiator, Message message);
}
