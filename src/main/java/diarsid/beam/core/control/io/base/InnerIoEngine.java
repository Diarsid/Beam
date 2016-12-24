/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.base;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.IoChoice;
import diarsid.beam.core.control.io.base.IoMessage;
import diarsid.beam.core.control.io.base.IoQuestion;

/**
 *
 * @author Diarsid
 */
public interface InnerIoEngine {
    
    boolean resolveYesOrNo(Initiator initiator, String yesOrNoQuestion);
    
    IoChoice resolveVariants(Initiator initiator, IoQuestion question);
    
    void report(Initiator initiator, String string);
    
    void reportAndExitLater(Initiator initiator, String string);
    
    void reportMessage(Initiator initiator, IoMessage message);
    
    void reportMessageAndExitLater(Initiator initiator, IoMessage message);
}
