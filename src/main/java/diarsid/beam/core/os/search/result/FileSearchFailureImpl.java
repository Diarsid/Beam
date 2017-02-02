/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.os.search.result;


public class FileSearchFailureImpl implements FileSearchFailure {
    
    private static final int LOCATION_NOT_FOUND;
    private static final int TARGET_NOT_FOUND;
    private static final int TARGET_NOT_ACCESSIBLE;
    
    private static final FileSearchFailure FAIL_BY_LOCATION;
    private static final FileSearchFailure FAIL_BY_TARGET_NOT_FOUND;
    
    static {
        LOCATION_NOT_FOUND = 1;
        TARGET_NOT_FOUND = 2;
        TARGET_NOT_ACCESSIBLE = 3;
        
        FAIL_BY_LOCATION = new FileSearchFailureImpl(LOCATION_NOT_FOUND);
        FAIL_BY_TARGET_NOT_FOUND = new FileSearchFailureImpl(TARGET_NOT_FOUND);
    }
    
    private final int failCause;
    private final String message;
    
    private FileSearchFailureImpl(int failCause) {
        this.failCause = failCause;
        this.message = null;
    }
    
    private FileSearchFailureImpl(int failCause, String message) {
        this.failCause = failCause;
        this.message = message;
    }
    
    public static FileSearchFailure invalidLocationFailure() {
        return FAIL_BY_LOCATION;
    }
    
    public static FileSearchFailure targetNotFoundFailure() {
        return FAIL_BY_TARGET_NOT_FOUND;
    }
    
    public static FileSearchFailure targetInvalidMessage(String failMessage) {
        if ( failMessage == null || failMessage.isEmpty() ) {
            throw new ResultOperationNotAllowedException(
                    "Failure message cannot be nor empty neither null.");
        }
        return new FileSearchFailureImpl(TARGET_NOT_ACCESSIBLE, failMessage);
    }

    @Override
    public boolean locationNotFound() {
        return ( this.failCause == LOCATION_NOT_FOUND );
    }

    @Override
    public boolean targetNotFound() {
        return ( this.failCause == TARGET_NOT_FOUND );
    }
    
    @Override
    public boolean targetNotAccessible() {
        return ( this.failCause == TARGET_NOT_ACCESSIBLE );
    }
    
    @Override
    public boolean hasTargetInvalidMessage() {
        return ( 
                this.failCause == TARGET_NOT_ACCESSIBLE && 
                this.message != null );
    }
    
    @Override
    public String getMessage() {
        this.proceedOnlyIfFailureIsDueToInvalidTarget();
        return this.message;
    }
    
    private void proceedOnlyIfFailureIsDueToInvalidTarget() {
        if ( this.failCause != TARGET_NOT_ACCESSIBLE ) {
            throw new ResultOperationNotAllowedException(
                    "This failure is not of invalid target type.");
        }
        if ( this.message == null ) {
            throw new ResultOperationNotAllowedException(
                    "This failure message is null.");
        }
    }
}
