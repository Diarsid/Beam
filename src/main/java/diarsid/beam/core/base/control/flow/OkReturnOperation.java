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
public interface OkReturnOperation<T extends Object> extends ReturnOperation<T> {
    
    boolean hasReturn();
    
    T getOrThrow();
    
    T getOrDefault(T defaultT);
}
