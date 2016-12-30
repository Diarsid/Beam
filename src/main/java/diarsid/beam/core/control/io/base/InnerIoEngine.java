/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.base;

/**
 *
 * @author Diarsid
 */
public interface InnerIoEngine {
    
    String askForInput(Initiator initiator, String inputQuestion);
    
    Choice resolveYesOrNo(Initiator initiator, String yesOrNoQuestion);
    
    VariantAnswer resolveVariants(Initiator initiator, VariantsQuestion question);
    
    void report(Initiator initiator, String string);
    
    void reportAndExitLater(Initiator initiator, String string);
    
    void reportMessage(Initiator initiator, Message message);
    
    void reportMessageAndExitLater(Initiator initiator, Message message);
}
