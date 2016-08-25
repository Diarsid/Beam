/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search.result;

import diarsid.beam.core.modules.executor.os.ResultOperationNotAllowedException;

import java.util.List;


public class FileSearchSuccessImpl implements FileSearchSuccess {
    
    private final List<String> multipleValues;
    
    private FileSearchSuccessImpl(List<String> multipleValues) {
        this.multipleValues = multipleValues;
    }
    
    public static FileSearchSuccess foundFiles(List<String> values) {
        if ( values == null || values.isEmpty() ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - values List is null or empty.");
        }
        return new FileSearchSuccessImpl(values);
    }

    @Override
    public boolean hasSingleFoundFile() {
        return ( this.multipleValues.size() == 1 );
    }

    @Override
    public String getFoundFile() {
        this.proceedOnlyIfSingleValue();
        return this.multipleValues.get(0);
    }

    @Override
    public List<String> getMultipleFoundFiles() {
        this.proceedOnlyIfMultipleValues();
        return this.multipleValues;
    }
    
    private void proceedOnlyIfSingleValue() {
        if ( this.multipleValues.size() != 1 ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - has multiple values, not single value.");
        }
    }
    
    private void proceedOnlyIfMultipleValues() {
        if ( this.multipleValues.size() < 2 ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - multiple values not found.");
        }
    }
}
