/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.workflow;


public class OperationResultImpl implements OperationResult {
    
    private final boolean success;
    private final String argumentCausingOperationToFail;
    
    private OperationResultImpl(boolean success) {
        this.success = success;
        this.argumentCausingOperationToFail = "";
    }
    
    private OperationResultImpl(String invalidArgument) {
        this.success = false;
        this.argumentCausingOperationToFail = invalidArgument;
    }
    
    public static OperationResultImpl success() {
        return new OperationResultImpl(true);
    }
    
    public static OperationResultImpl failByInvalidArgument(String invalidArgument) {
        return new OperationResultImpl(invalidArgument);
    }
    
    public static OperationResultImpl failByInvalidLogic() {
        return new OperationResultImpl(false);
    }
    
    @Override
    public boolean ifSuccess() {
        return this.success;
    }
    
    @Override
    public boolean ifFail() {
        return ( ! this.success );
    }
    
    @Override
    public boolean ifFailCausedByInvalidArgument() {
        return ( ! this.argumentCausingOperationToFail.isEmpty() );
    }
    
    @Override
    public String getFailureArgument() {
        return this.argumentCausingOperationToFail;
    }
    
    // equals() and hashCode() methods have not been overriden
    // deliberately, as every operation that finishes successfuly 
    // returns absolutely equal OperationResultImpl objects. Some 
    // methods return Set<OperationResult> instead of single 
    // object, that's why all OperationResultImpl objects must be 
    // treated as completely different objects to place them 
    // in hash-based collection safely and they must not be 
    // equal even if they actually are.
}
