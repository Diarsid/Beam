/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;

import static diarsid.beam.core.base.control.flow.Operations.asFail;

/**
 *
 * @author Diarsid
 */
abstract class AbstractCliAdapter {
    
    private final InnerIoEngine ioEngine;
    
    AbstractCliAdapter(InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
    }
    
    protected final void report(Initiator initiator, String message) {
        this.ioEngine.report(initiator, message);
    }
    
    protected final  void report(Initiator initiator, Message message) {
        this.ioEngine.reportMessage(initiator, message);
    }

    protected final void reportOperationFlow(Initiator initiator, OperationFlow flow, String onSuccess) {
        switch ( flow.result() ) {
            case SUCCESS : {
                this.ioEngine.report(initiator, onSuccess);
                break;
            }         
            case FAIL : {
                this.ioEngine.report(initiator, asFail(flow).getReason());
                break;
            }         
            case STOP : {
                break;
            }         
            default : {
                this.ioEngine.report(initiator, "unkown operation result.");
            }
        }        
    }
}
