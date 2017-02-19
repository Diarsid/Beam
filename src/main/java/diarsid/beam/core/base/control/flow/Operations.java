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
    private static final ReturnOperation STOPPED_RETURN_OPERATION;
    
    static {
        OK_VOID_OPERATION = () -> OK;
        STOPPED_VOID_OPERATION = () -> STOP;
        STOPPED_RETURN_OPERATION = () -> STOP;
    }
    
    private Operations() {
    }
    
    public static VoidOperation ok() {
        return OK_VOID_OPERATION;
    }
    
    public static <T extends Object> ReturnOperation<T> okWith(Optional<T> optT) {
        if ( optT.isPresent() ) {
            return okWith(optT.get());
        } else {
            return successEmpty();
        }
    }
    
    public static <T extends Object> ReturnOperation<T> okWith(T t) {
        return new OkReturnOperation<T>() {
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
    
    public static <T extends Object> ReturnOperation<T> successEmpty() {
        return new OkReturnOperation<T>() {
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
    
    public static ReturnOperation returnOperationStopped() {
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
    
    public static FailedReturnOperation returnOperationFail(String failMessage) {
        return new FailedReturnOperation() {
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
    
    public static FailedReturnOperation asFail(ReturnOperation operationFlow) {
        return (FailedReturnOperation) operationFlow;
    }
    
    public static OkReturnOperation asOk(ReturnOperation operationFlow) {
        return (OkReturnOperation) operationFlow;
    }
}
