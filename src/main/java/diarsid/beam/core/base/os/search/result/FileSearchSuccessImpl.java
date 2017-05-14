/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.search.result;

import java.util.ArrayList;
import java.util.List;


class FileSearchSuccessImpl implements FileSearchSuccess, FileSearchResult {
    
    private final List<String> multipleValues;
    
    FileSearchSuccessImpl(List<String> multipleValues) {
        this.multipleValues = multipleValues;
    }
    
    FileSearchSuccessImpl(String value) {
        this.multipleValues = new ArrayList<>();
        this.multipleValues.add(value);
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public FileSearchSuccess success() {
        return this;
    }

    @Override
    public FileSearchFailure failure() {
        throw new ResultOperationNotAllowedException(
                "Action not allowed - this result value is success.");
    }

    @Override
    public List<String> foundFilesOrNothing() {
        return this.multipleValues;
    }

    @Override
    public boolean hasSingleFoundFile() {
        return ( this.multipleValues.size() == 1 );
    }

    @Override
    public String foundFile() {
        this.proceedOnlyIfSingleValue();
        return this.multipleValues.get(0);
    }

    @Override
    public List<String> foundFiles() {
        return this.multipleValues;
    }
    
    private void proceedOnlyIfSingleValue() {
        if ( this.multipleValues.size() != 1 ) {
            throw new ResultOperationNotAllowedException(
                    "Action not allowed - has multiple values, not single value.");
        }
    }
}
