/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.search.result;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;


public class FileSearchSuccessImpl implements FileSearchSuccess {
    
    private final List<String> multipleValues;
    
    private FileSearchSuccessImpl(List<String> multipleValues) {
        this.multipleValues = multipleValues;
    }
    
    private FileSearchSuccessImpl(String value) {
        this.multipleValues = new ArrayList<>();
        this.multipleValues.add(value);
    }
    
    public static FileSearchSuccess foundFile(String value) {
        if ( isNull(value) || value.isEmpty() ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - value is null or empty.");
        }
        return new FileSearchSuccessImpl(value);
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
