/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.workflow;

/**
 *
 * @author Diarsid
 */
public class OperationResult {
    
    private final boolean success;
    private final String argumentCausingOperationToFail;
    
    private OperationResult(boolean success) {
        this.success = success;
        this.argumentCausingOperationToFail = "";
    }
    
    private OperationResult(String invalidArgument) {
        this.success = false;
        this.argumentCausingOperationToFail = invalidArgument;
    }
    
    public static OperationResult success() {
        return new OperationResult(true);
    }
    
    public static OperationResult failByInvalidArgument(String invalidArgument) {
        return new OperationResult(invalidArgument);
    }
    
    public static OperationResult failByInvalidLogic() {
        return new OperationResult(false);
    }
    
    public boolean ifSuccess() {
        return this.success;
    }
    
    public boolean ifFail() {
        return ( ! this.success );
    }
    
    public boolean ifFailCausedByInvalidArgument() {
        return ( ! this.argumentCausingOperationToFail.isEmpty() );
    }
    
    public String getFailureArgument() {
        return this.argumentCausingOperationToFail;
    }
    
    // equals() and hashCode() methods have not been overriden
    // deliberately, as every operation that finishes successfuly 
    // returns absolutely equal OperationResult objects. Some 
    // methods return Set<OperationResult> instead of single 
    // object, that's why all OperationResult objects must be 
    // treated as completely different objects to place them 
    // in hash-based collection safely and they must not be 
    // equal even if they actually are.
}
