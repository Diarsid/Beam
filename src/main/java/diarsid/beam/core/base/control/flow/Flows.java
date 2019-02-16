/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.flow;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.control.flow.FlowResult.DONE;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.FlowResult.STOP;

/**
 *
 * @author Diarsid
 */
public class Flows {
    
    private static final VoidFlow VOID_FLOW_DONE;
    private static final VoidFlow VOID_FLOW_STOPPED;
    private static final ValueFlow VALUE_FLOW_STOPPED;
    private static final ValueFlow<Object> VALUE_FLOW_EMPTY;
    
    static {
        VOID_FLOW_DONE = new VoidFlow() {
            
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
                return DONE;
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
            public ValueFlowDone asDone() {
                throw new IllegalStateException("This is Stop operation");
            }

            @Override
            public boolean isDoneWithValue() {
                return false;
            }

            @Override
            public boolean isNotDoneWithValue() {
                return true;
            }
            
            @Override
            public boolean isDoneEmpty() {
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
        VALUE_FLOW_EMPTY = new ValueFlowDone<Object>() {
            
            @Override
            public boolean hasValue() {
                return false;
            }
                        
            @Override
            public boolean isDoneEmpty() {
                return true;
            }

            @Override
            public boolean isDoneWithValue() {
                return false;
            }

            @Override
            public boolean isNotDoneWithValue() {
                return true;
            }

            @Override
            public Object orThrow() {
                throw new IllegalStateException("This is empty flow.");
            }

            @Override
            public Object orDefault(Object defaultT) {
                return defaultT;
            }

            @Override
            public FlowResult result() {
                return DONE;
            }

            @Override
            public ValueFlowDone<Object> asDone() {
                return this;
            }

            @Override
            public ValueFlowFail asFail() {                
                throw new IllegalStateException("This is Done flow.");
            }
            
            @Override
            public VoidFlow toVoid() {
                return VOID_FLOW_DONE;
            }

            @Override
            public <R> ValueFlow<R> toFlowWith(Function<Object, R> mapFunction) {
                return (ValueFlow<R>) this;
            }
        };
    }
    
    private Flows() {
    }
    
    public static VoidFlow voidFlowDone() {
        return VOID_FLOW_DONE;
    }
    
    public static VoidFlow voidFlowDone(String message) {
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
                return DONE;
            }
        };
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowDoneWith(
            Optional<T> optT) {
        if ( optT.isPresent() ) {
            return Flows.valueFlowDoneWith(optT.get());
        } else {
            return valueFlowDoneEmpty();
        }
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowDoneWith(T t) {
        return new ValueFlowDone<T>() {
            
            @Override
            public boolean hasValue() {
                return true;
            }
            
            @Override
            public boolean isDoneEmpty() {
                return false;
            }

            @Override
            public boolean isDoneWithValue() {
                return true;
            }

            @Override
            public boolean isNotDoneWithValue() {
                return false;
            }

            @Override
            public T orThrow() {
                return t;
            }

            @Override
            public T orDefault(T defaultT) {
                return t;
            }

            @Override
            public FlowResult result() {
                return DONE;
            }

            @Override
            public ValueFlowDone<T> asDone() {
                return this;
            }

            @Override
            public ValueFlowFail asFail() {                
                throw new IllegalStateException("This is Complete operation.");
            }
            
            @Override
            public VoidFlow toVoid() {
                return VOID_FLOW_DONE;
            }

            @Override
            public <R> ValueFlow<R> toFlowWith(Function<T, R> mapFunction) {
                return valueFlowDoneWith(mapFunction.apply(t));
            }
        };
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowDoneOrEmptyIfNull(
            ValueFlow<T> valueFlow) {
        if ( nonNull(valueFlow) ) {
            return valueFlow;
        } else {
            return valueFlowDoneEmpty();
        }
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowDoneEmpty() {
        return (ValueFlow<T>) VALUE_FLOW_EMPTY;
    }
    
    public static <T extends Object> ValueFlow<T> valueFlowDoneEmpty(String message) {
        return new ValueFlowDone<T>() {            
    
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
            public T orThrow() {
                throw new IllegalStateException("This is empty flow.");
            }

            @Override
            public T orDefault(T defaultT) {
                return defaultT;
            }

            @Override
            public <R> ValueFlow<R> toFlowWith(Function<T, R> mapFunction) {
                return (ValueFlow<R>) this;
            }

            @Override
            public boolean isDoneEmpty() {
                return true;
            }

            @Override
            public boolean isDoneWithValue() {
                return false;
            }

            @Override
            public boolean isNotDoneWithValue() {
                return true;
            }

            @Override
            public ValueFlowDone<T> asDone() {
                return this;
            }

            @Override
            public ValueFlowFail asFail() {
                throw new IllegalStateException("This is Done flow.");
            }

            @Override
            public VoidFlow toVoid() {
                return VOID_FLOW_DONE;
            }

            @Override
            public FlowResult result() {
                return DONE;
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
            public boolean isDoneEmpty() {
                return false;
            }

            @Override
            public boolean isDoneWithValue() {
                return false;
            }

            @Override
            public boolean isNotDoneWithValue() {
                return true;
            }

            @Override
            public FlowResult result() {
                return FAIL;
            }

            @Override
            public ValueFlowDone asDone() {                
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
