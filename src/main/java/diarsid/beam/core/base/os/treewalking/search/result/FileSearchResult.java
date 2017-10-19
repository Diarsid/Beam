/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search.result;

import java.util.List;

import static java.util.Objects.isNull;

import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchFailureImpl.FAIL_BY_LOCATION;
import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchFailureImpl.FAIL_BY_TARGET_NOT_FOUND;
import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchFailureImpl.TARGET_NOT_ACCESSIBLE;

/**
 *
 * @author Diarsid
 */
public interface FileSearchResult {
    
    public static FileSearchResult successWithFile(String value) {
        if ( isNull(value) || value.isEmpty() ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - value is null or empty.");
        }
        return new FileSearchSuccessImpl(value);
    }
    
    public static FileSearchResult successWithFiles(List<String> values) {
        if ( values == null || values.isEmpty() ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - values List is null or empty.");
        }
        return new FileSearchSuccessImpl(values);
    }
    
    public static FileSearchResult failWithInvalidLocationFailure() {
        return FAIL_BY_LOCATION;
    }
    
    public static FileSearchResult failWithTargetNotFoundFailure() {
        return FAIL_BY_TARGET_NOT_FOUND;
    }
    
    public static FileSearchResult failWithTargetInvalidMessage(String failMessage) {
        if ( failMessage == null || failMessage.isEmpty() ) {
            throw new ResultOperationNotAllowedException(
                    "Failure message cannot be nor empty neither null.");
        }
        return new FileSearchFailureImpl(TARGET_NOT_ACCESSIBLE, failMessage);
    }
    
    boolean isOk();
    
    FileSearchSuccess success();
    
    FileSearchFailure failure();
    
    List<String> foundFilesOrNothing();
}
