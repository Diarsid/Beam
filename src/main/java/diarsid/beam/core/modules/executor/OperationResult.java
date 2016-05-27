/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

/**
 *
 * @author Diarsid
 */
public class OperationResult {
    
    private final boolean status;
    private final String argumentCausingToFail;
    
    private OperationResult(boolean status) {
        this.status = status;
        this.argumentCausingToFail = "";
    }
    
    private OperationResult(String invalidArgument) {
        this.status = false;
        this.argumentCausingToFail = invalidArgument;
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
    
    boolean ifOperationWasSuccessful() {
        return this.status;
    }
    
    boolean ifFailCausedByInvalidArgument() {
        return ( ! this.argumentCausingToFail.isEmpty() );
    }
    
    String getFailureArgument() {
        return this.argumentCausingToFail;
    }
    
    // equals() and hashCode() methods have not been overriden
    // deliberately, as every operation finishing successfuly 
    // returns absolutely equal OperationResult objects. Some 
    // methods return Set<OperationResult> instead of single 
    // object, that's why all OperationResult objects must be 
    // treated as completely different objects to place them 
    // in hash-based collection safely and they must not be 
    // equal even if they actually are.
}
