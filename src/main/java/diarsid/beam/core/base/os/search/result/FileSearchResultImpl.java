/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.search.result;


public class FileSearchResultImpl implements FileSearchResult {
    
    private final boolean isSuccess;
    private final FileSearchSuccess success;
    private final FileSearchFailure failure;
    
    private FileSearchResultImpl(FileSearchSuccess success) {
        this.isSuccess = true;
        this.success = success;
        this.failure = null;
    }
    
    private FileSearchResultImpl(FileSearchFailure failure) {
        this.isSuccess = false;
        this.success = null;
        this.failure = failure;
    }
    
    public static FileSearchResult successWith(FileSearchSuccess success) {
        return new FileSearchResultImpl(success);
    }
    
    public static FileSearchResult failWith(FileSearchFailure failure) {
        return new FileSearchResultImpl(failure);
    }

    @Override
    public boolean isOk() {
        return this.isSuccess;
    }

    @Override
    public FileSearchSuccess success() {
        this.proceedOnlyIfResultIsOk();
        return this.success;
    }

    @Override
    public FileSearchFailure failure() {
        this.proceedOnlyIfResultIsFail();
        return this.failure;
    }
    
    private void proceedOnlyIfResultIsOk() {
        if ( ! this.isSuccess ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - this result value is fail.");
        }
    }
    
    private void proceedOnlyIfResultIsFail() {
        if ( this.isSuccess ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - this result value is success.");
        }
    }
}
