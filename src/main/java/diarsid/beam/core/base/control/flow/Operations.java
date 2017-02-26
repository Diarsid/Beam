/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.flow;

import java.util.Optional;

import static diarsid.beam.core.base.control.flow.OperationResult.FAIL;
import static diarsid.beam.core.base.control.flow.OperationResult.OK;
import static diarsid.beam.core.base.control.flow.OperationResult.STOP;

/**
 *
 * @author Diarsid
 */
public class Operations {
    
    private static final VoidOperation OK_VOID_OPERATION;
    private static final VoidOperation STOPPED_VOID_OPERATION;
    private static final ValueOperation STOPPED_RETURN_OPERATION;
    
    static {
        OK_VOID_OPERATION = () -> OK;
        STOPPED_VOID_OPERATION = () -> STOP;
        STOPPED_RETURN_OPERATION = () -> STOP;
    }
    
    private Operations() {
    }
    
    public static VoidOperation voidCompleted() {
        return OK_VOID_OPERATION;
    }
    
    public static <T extends Object> ValueOperation<T> valueFound(Optional<T> optT) {
        if ( optT.isPresent() ) {
            return Operations.valueFound(optT.get());
        } else {
            return successEmpty();
        }
    }
    
    public static <T extends Object> ValueOperation<T> valueFound(T t) {
        return new OkValueOperation<T>() {
            @Override
            public boolean hasReturn() {
                return true;
            }

            @Override
            public T getOrThrow() {
                return t;
            }

            @Override
            public T getOrDefault(T defaultT) {
                return t;
            }

            @Override
            public OperationResult result() {
                return OK;
            }
        };
    }
    
    public static <T extends Object> ValueOperation<T> successEmpty() {
        return new OkValueOperation<T>() {
            @Override
            public boolean hasReturn() {
                return false;
            }

            @Override
            public T getOrThrow() {
                throw new IllegalStateException("This is empty return.");
            }

            @Override
            public T getOrDefault(T defaultT) {
                return defaultT;
            }

            @Override
            public OperationResult result() {
                return OK;
            }
        };
    }
    
    public static VoidOperation voidOperationStopped() {
        return STOPPED_VOID_OPERATION;
    }
    
    public static ValueOperation valueOperationStopped() {
        return STOPPED_RETURN_OPERATION;
    }
    
    public static FailedVoidOperation voidOperationFail(String failMessage) {
        return new FailedVoidOperation() {
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
    
    public static FailedValueOperation valueOperationFail(String failMessage) {
        return new FailedValueOperation() {
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
    
    public static FailedVoidOperation asFail(VoidOperation operationFlow) {
        return (FailedVoidOperation) operationFlow;
    }
    
    public static FailedValueOperation asFail(ValueOperation operationFlow) {
        return (FailedValueOperation) operationFlow;
    }
    
    public static OkValueOperation asOk(ValueOperation operationFlow) {
        return (OkValueOperation) operationFlow;
    }
}
