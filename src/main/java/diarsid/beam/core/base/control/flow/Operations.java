/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.flow;

import static diarsid.beam.core.base.control.flow.OperationResult.FAIL;
import static diarsid.beam.core.base.control.flow.OperationResult.STOP;
import static diarsid.beam.core.base.control.flow.OperationResult.SUCCESS;

/**
 *
 * @author Diarsid
 */
public class Operations {
    
    private final static OperationFlow SUCCEDED_OPERATION;
    private final static OperationFlow STOPPED_OPERATION;
    
    static {
        SUCCEDED_OPERATION = () -> SUCCESS;
        STOPPED_OPERATION = () -> STOP;
    }
    
    private Operations() {
    }
    
    public static OperationFlow success() {
        return SUCCEDED_OPERATION;
    }
    
    public static OperationFlow operationStopped() {
        return STOPPED_OPERATION;
    }
    
    public static FailedOperationFlow operationFailedWith(String failMessage) {
        return new FailedOperationFlow() {
            @Override
            public String getReason() {
                return failMessage;
            }

            @Override
            public OperationResult result() {
                return FAIL;
            }
        };
    }
    
    public static FailedOperationFlow asFail(OperationFlow operationFlow) {
        return (FailedOperationFlow) operationFlow;
    }
}
