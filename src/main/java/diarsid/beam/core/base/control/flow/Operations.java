/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.flow;

import java.util.Optional;

import static diarsid.beam.core.base.control.flow.OperationResult.COMPLETE;
import static diarsid.beam.core.base.control.flow.OperationResult.FAIL;
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
        OK_VOID_OPERATION = new VoidOperation() {
            @Override
            public boolean hasMessage() {
                return false;
            }

            @Override
            public String message() {
                throw new IllegalStateException("this VoidOperation doesn't have message."); 
            }

            @Override
            public OperationResult result() {
                return COMPLETE;
            }
        };
        STOPPED_VOID_OPERATION = new VoidOperation() {
            @Override
            public boolean hasMessage() {
                return false;
            }

            @Override
            public String message() {
                throw new IllegalStateException("this VoidOperation doesn't have message."); 
            }

            @Override
            public OperationResult result() {
                return STOP;
            }
        };
        STOPPED_RETURN_OPERATION = new ValueOperation() {
            @Override
            public ValueOperationComplete asComplete() {
                throw new IllegalStateException("This is Stop operation");
            }

            @Override
            public ValueOperationFail asFail() {
                throw new IllegalStateException("This is Stop operation");
            }

            @Override
            public OperationResult result() {
                return STOP;
            }
        };
    }
    
    private Operations() {
    }
    
    public static VoidOperation voidCompleted() {
        return OK_VOID_OPERATION;
    }
    
    public static VoidOperation voidCompleted(String message) {
        return new VoidOperation() {
            @Override
            public boolean hasMessage() {
                return true;
            }

            @Override
            public String message() {
                return message;
            }

            @Override
            public OperationResult result() {
                return COMPLETE;
            }
        };
    }
    
    public static <T extends Object> ValueOperation<T> valueCompletedWith(Optional<T> optT) {
        if ( optT.isPresent() ) {
            return Operations.valueCompletedWith(optT.get());
        } else {
            return valueCompletedEmpty();
        }
    }
    
    public static <T extends Object> ValueOperation<T> valueCompletedWith(T t) {
        return new ValueOperationComplete<T>() {
            @Override
            public boolean hasValue() {
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
                return COMPLETE;
            }

            @Override
            public ValueOperationComplete<T> asComplete() {
                return this;
            }

            @Override
            public ValueOperationFail asFail() {                
                throw new IllegalStateException("This is Complete operation.");
            }
        };
    }
    
    public static <T extends Object> ValueOperation<T> valueCompletedEmpty() {
        return new ValueOperationComplete<T>() {
            @Override
            public boolean hasValue() {
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
                return COMPLETE;
            }

            @Override
            public ValueOperationComplete<T> asComplete() {
                return this;
            }

            @Override
            public ValueOperationFail asFail() {                
                throw new IllegalStateException("This is Complete operation.");
            }
        };
    }
    
    public static VoidOperation voidOperationStopped() {
        return STOPPED_VOID_OPERATION;
    }
    
    public static ValueOperation valueOperationStopped() {
        return STOPPED_RETURN_OPERATION;
    }
    
    public static VoidOperation voidOperationFail(String failMessage) {
        return new VoidOperation() {
            @Override
            public boolean hasMessage() {
                return true;
            }
            
            @Override
            public String message() {
                return failMessage;
            }

            @Override
            public OperationResult result() {
                return FAIL;
            }
        };
    }
    
    public static ValueOperationFail valueOperationFail(String failMessage) {
        return new ValueOperationFail() {
            @Override
            public String reason() {
                return failMessage;
            }

            @Override
            public OperationResult result() {
                return FAIL;
            }

            @Override
            public ValueOperationComplete asComplete() {                
                throw new IllegalStateException("This is Fail operation.");
            }

            @Override
            public ValueOperationFail asFail() {
                return this;
            }
        };
    }
}
