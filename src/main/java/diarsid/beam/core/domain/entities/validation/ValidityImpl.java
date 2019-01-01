/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities.validation;

/**
 *
 * @author Diarsid
 */
class ValidityImpl implements Validity {
        
    private final String description;
    private final boolean isOk;
    
    ValidityImpl(boolean isOk, String description) {
        this.isOk = isOk;
        this.description = description;
    }
    
    @Override
    public boolean isOk() {
        return this.isOk;
    }
    
    @Override
    public String getFailureMessage() {
        return this.description;
    }    

    @Override
    public boolean isFail() {
        return ! this.isOk;
    }
}
