/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;


import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.ConvertableToMessage;
import diarsid.beam.core.base.control.io.base.interaction.Message;

import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.FlowResult.STOP;
import static diarsid.beam.core.base.control.flow.FlowResult.DONE;

import diarsid.beam.core.base.control.flow.ValueFlowDone;

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
    
    protected final void report(Initiator initiator, Message message) {
        this.ioEngine.reportMessage(initiator, message);
    }
    
    protected final void reportVoidFlow(
            Initiator initiator, VoidFlow flow) {
        switch ( flow.result() ) {
            case DONE : {
                if ( flow.hasMessage() ) {
                    this.ioEngine.report(initiator, flow.message());
                }
                break;
            }         
            case FAIL : {
                this.ioEngine.report(initiator, flow.message());
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

    protected final void reportVoidFlow(
            Initiator initiator, VoidFlow flow, String onSuccess) {
        switch ( flow.result() ) {
            case DONE : {
                this.ioEngine.report(initiator, onSuccess);
                break;
            }         
            case FAIL : {
                this.ioEngine.report(initiator, flow.message());
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
    
    protected final <T> void reportValueFlow(
            Initiator initiator, 
            ValueFlow<T> flow, 
            Function<ValueFlowDone<T>, Message> ifNonEmptyFunction, 
            String ifEmptyMessage) {
        switch ( flow.result() ) {
            case DONE : {
                if ( flow.asDone().hasValue() ) {
                    this.ioEngine.reportMessage(
                            initiator, ifNonEmptyFunction.apply(flow.asDone()));
                } else {
                    this.ioEngine.report(initiator, ifEmptyMessage);
                }                
                break;
            }         
            case FAIL : {
                this.ioEngine.report(initiator, flow.asFail().reason());
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
    
    protected final <T extends ConvertableToMessage> void reportValueFlow(
            Initiator initiator, 
            ValueFlow<T> flow, 
            String ifEmptyMessage) {
        switch ( flow.result() ) {
            case DONE : {
                if ( flow.asDone().hasValue() ) {
                    this.ioEngine.reportMessage(
                            initiator, flow.asDone().orThrow().toMessage());
                } else {
                    this.ioEngine.report(initiator, ifEmptyMessage);
                }                
                break;
            }         
            case FAIL : {
                this.ioEngine.report(initiator, flow.asFail().reason());
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
