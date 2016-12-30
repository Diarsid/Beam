/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.base;

import java.io.IOException;

/**
 *
 * @author Diarsid
 */
public interface OuterIoEngine {
    
    String getName() throws IOException;
    
    String askForInput(String inputRequest) throws IOException;
    
    Choice resolveYesOrNo(String yesOrNoQuestion) throws IOException;
    
    VariantAnswer resolveQuestion(VariantsQuestion question) throws IOException;
    
    void report(String string) throws IOException;
    
    void reportMessage(Message message) throws IOException;
    
    void close() throws IOException;
    
    void acceptInitiator(Initiator initiator) throws IOException;
}
