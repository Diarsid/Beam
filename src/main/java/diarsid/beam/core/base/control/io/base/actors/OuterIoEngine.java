/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.actors;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;

/**
 *
 * @author Diarsid
 */
public interface OuterIoEngine {
    
    String name() throws IOException;
    
    String askForInput(String inputRequest) throws IOException;
    
    Choice resolve(String yesOrNoQuestion) throws IOException;
    
    Answer resolve(VariantsQuestion question) throws IOException;
    
    Answer resolve(WeightedVariants variants) throws IOException;
    
    void report(String string) throws IOException;
    
    void report(Message message) throws IOException;
    
    void close() throws IOException;
    
    void accept(Initiator initiator) throws IOException;
}
