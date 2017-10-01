/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.flow;

import java.util.function.Function;

/**
 *
 * @author Diarsid
 */
public interface ValueFlow<T extends Object> extends Flow {
    
    <R> ValueFlow<R> map(Function<T, R> mapFunction);
    
    ValueFlowCompleted<T> asComplete();
    
    ValueFlowFail asFail();
    
    VoidFlow toVoid();
}
