/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.flow;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.control.flow.FlowResult.COMPLETE;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.FlowResult.STOP;

/**
 *
 * @author Diarsid
 */
public class Flows {
    
    private static final VoidFlow VOID_FLOW_COMPLETED;
    private static final VoidFlow VOID_FLOW_STOPPED;
    private static final ValueFlow VALUE_FLOW_STOPPED;
    private static final ValueFlow<Object> VALUE_FLOW_EMPTY;
    
    static {
        VOID_FLOW_COMPLETED = new VoidFlow() {
            
            @Override
            public boolean hasMessage() {
                return false;
            }

            @Override
            public String message() {
                throw new IllegalStateException("this VoidFlow doesn't have message."); 
            }

            @Override
            public FlowResult result() {
                return COMPLETE;
            }
        };
        VOID_FLOW_STOPPED = new VoidFlow() {
            
            @Override
            public boolean hasMessage() {
                return false;
            }

            @Override
            public String message() {
                throw new IllegalStateException("this VoidFlow doesn't have message."); 
            }

            @Override
            public FlowResult result() {
                return STOP;
            }
        };
        VALUE_FLOW_STOPPED = new ValueFlow() {
            
            @Override
            public ValueFlowCompleted asComplete() {
                throw new IllegalStateException("This is Stop operation");
            }
            
            @Override
            public boolean isCompletedEmpty() {
                return false;
            }

            @Override
            public ValueFlowFail asFail() {
                throw new IllegalStateException("This is Stop operation");
            }

            @Override
            public FlowResult result() {
                return STOP;
            }
            
            @Override
            public VoidFlow toVoid() {
                return VOID_FLOW_STOPPED;
            }

            @Override
            public ValueFlow toFlowWith(Function mapFunction) {
                return this;
            }
        };
        VALUE_FLOW_EMPTY = new ValueFlowCompleted<Object>() {
            
            @Override
            public boolean hasValue() {
                return false;
            }
                        
            @Override
            public boolean isCompletedEmpty() {
                return true;
            }

            @Override
            public Object getOrThrow() {
                throw new IllegalStateException("This is empty flow.");
            }

            @Override
            public Object getOrDefault(Object defaultT) {
                return defaultT;
            }

            @Override
            public FlowResult result() {
                return COMPLETE;
            }

            @Override
            public ValueFlowCompleted<Object> asComplete() {
                return this;
            }

            @Override
            public ValueFlowFail asFail() {                
                throw new IllegalStateException("This is Completed flow.");
            }
            
            @Override
            public VoidFlow toVoid() {
                return VOID_FLOW_COMPLETED;
            }

            @Override
            public <R> ValueFlow<R> toFlowWith(Function<Object, R> mapFunction) {
                return (ValueFlow<R>) this;
            }
        };
    }
    
    private Flows() {
    }
    
    public static VoidFlow voidFlowCompleted() {
        return VOID_FLOW_COMPLETED;
    }
    
    public static VoidFlow voidFlowCompleted(String message) {
        return new VoidFlow() {
            
            @Override
            public boolean hasMessage() {
                return true;
            }

            @Override
            public String message() {
                return message;
            }

            @Override
            public FlowResult result() {
                return COMPLETE;
            }
        };
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowCompletedWith(
            Optional<T> optT) {
        if ( optT.isPresent() ) {
            return Flows.valueFlowCompletedWith(optT.get());
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowCompletedWith(T t) {
        return new ValueFlowCompleted<T>() {
            
            @Override
            public boolean hasValue() {
                return true;
            }
            
            @Override
            public boolean isCompletedEmpty() {
                return false;
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
            public FlowResult result() {
                return COMPLETE;
            }

            @Override
            public ValueFlowCompleted<T> asComplete() {
                return this;
            }

            @Override
            public ValueFlowFail asFail() {                
                throw new IllegalStateException("This is Complete operation.");
            }
            
            @Override
            public VoidFlow toVoid() {
                return VOID_FLOW_COMPLETED;
            }

            @Override
            public <R> ValueFlow<R> toFlowWith(Function<T, R> mapFunction) {
                return valueFlowCompletedWith(mapFunction.apply(t));
            }
        };
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowCompletedOrEmptyIfNull(
            ValueFlow<T> valueFlow) {
        if ( nonNull(valueFlow) ) {
            return valueFlow;
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowCompletedEmpty() {
        return (ValueFlow<T>) VALUE_FLOW_EMPTY;
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowCompletedEmpty(String message) {
        return new ValueFlowCompleted<T>() {            
    
            @Override
            public boolean hasMessage() {
                return true;
            }

            @Override
            public String message() {
                return message;
            }
    
            @Override
            public boolean hasValue() {
                return false;
            }

            @Override
            public T getOrThrow() {
                throw new IllegalStateException("This is empty flow.");
            }

            @Override
            public T getOrDefault(T defaultT) {
                return defaultT;
            }

            @Override
            public <R> ValueFlow<R> toFlowWith(Function<T, R> mapFunction) {
                return (ValueFlow<R>) this;
            }

            @Override
            public boolean isCompletedEmpty() {
                return true;
            }

            @Override
            public ValueFlowCompleted<T> asComplete() {
                return this;
            }

            @Override
            public ValueFlowFail asFail() {
                throw new IllegalStateException("This is Completed flow.");
            }

            @Override
            public VoidFlow toVoid() {
                return VOID_FLOW_COMPLETED;
            }

            @Override
            public FlowResult result() {
                return COMPLETE;
            }
        };
    }
    
    public static VoidFlow voidFlowStopped() {
        return VOID_FLOW_STOPPED;
    }
    
    public static ValueFlow valueFlowStopped() {
        return VALUE_FLOW_STOPPED;
    }
    
    public static VoidFlow voidFlowFail(String failMessage) {
        return new VoidFlow() {
            @Override
            public boolean hasMessage() {
                return true;
            }
            
            @Override
            public String message() {
                return failMessage;
            }

            @Override
            public FlowResult result() {
                return FAIL;
            }
        };
    }
    
    public static ValueFlowFail valueFlowFail(String failMessage) {
        return new ValueFlowFail() {
            @Override
            public String reason() {
                return failMessage;
            }
            
            @Override
            public boolean isCompletedEmpty() {
                return false;
            }

            @Override
            public FlowResult result() {
                return FAIL;
            }

            @Override
            public ValueFlowCompleted asComplete() {                
                throw new IllegalStateException("This is Fail operation.");
            }

            @Override
            public ValueFlowFail asFail() {
                return this;
            }
            
            @Override
            public VoidFlow toVoid() {
                return voidFlowFail(failMessage);
            }

            @Override
            public ValueFlow toFlowWith(Function mapFunction) {
                return this;
            }
        };
    }
}
