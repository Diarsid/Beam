/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.flow;

/**
 *
 * @author Diarsid
 */
public interface ValueFlowCompleted<T extends Object> extends ValueFlow<T> {
    
    boolean hasValue();
    
    default boolean isEmpty() {
        return ! this.hasValue();
    }
    
    default boolean hasMessage() {
        return false;
    }

    default String message() {
        throw new IllegalStateException("This flow does not have message.");
    }
    
    T orThrow();
    
    T orDefault(T defaultT);
}
