/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.flow;

/**
 *
 * @author Diarsid
 */
public interface OperationResult {

    String getFailureArgument();
    // equals() and hashCode() methods have not been overriden
    // deliberately, as every operation that finishes successfuly
    // returns absolutely equal OperationResultImpl objects. Some
    // methods return Set<OperationResult> instead of single
    // object, that's why all OperationResultImpl objects must be
    // treated as completely different objects to place them
    // in hash-based collection safely and they must not be
    // equal even if they actually are.

    boolean ifFail();

    boolean ifFailCausedByInvalidArgument();

    boolean ifSuccess();
    
}
