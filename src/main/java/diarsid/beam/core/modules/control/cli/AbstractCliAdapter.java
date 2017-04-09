/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;


import java.util.function.Function;

import diarsid.beam.core.base.control.flow.OkValueOperation;
import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;

import static diarsid.beam.core.base.control.flow.OperationResult.FAIL;
import static diarsid.beam.core.base.control.flow.OperationResult.OK;
import static diarsid.beam.core.base.control.flow.OperationResult.STOP;
import static diarsid.beam.core.base.control.flow.Operations.asFail;
import static diarsid.beam.core.base.control.flow.Operations.asOk;

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

    protected final void reportVoidOperationFlow(
            Initiator initiator, VoidOperation flow, String onSuccess) {
        switch ( flow.result() ) {
            case OK : {
                this.ioEngine.report(initiator, onSuccess);
                break;
            }         
            case FAIL : {
                this.ioEngine.report(initiator, asFail(flow).reason());
                break;
            }         
            case STOP : {
                // do not report anything as STOP is the result of user choice.
                break;
            }         
            default : {
                this.ioEngine.report(initiator, "unkown operation result.");
            }
        }        
    }
    
    protected final void reportValueOperationFlow(
            Initiator initiator, 
            ValueOperation flow, 
            Function<OkValueOperation, Message> ifNonEmptyFunction, 
            String ifEmptyMessage) {
        switch ( flow.result() ) {
            case OK : {
                OkValueOperation success = asOk(flow);
                if ( success.hasReturn() ) {
                    this.ioEngine.reportMessage(initiator, ifNonEmptyFunction.apply(success));
                } else {
                    this.ioEngine.report(initiator, ifEmptyMessage);
                }                
                break;
            }         
            case FAIL : {
                this.ioEngine.report(initiator, asFail(flow).reason());
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
