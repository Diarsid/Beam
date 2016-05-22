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
class ResolvedChoice {
        
    private final int variantNumber;
    private final String madeChoice;
    
    ResolvedChoice(int variantNumber, String madeChoice) {
        this.variantNumber = variantNumber;
        this.madeChoice = madeChoice;
    }
    
    boolean isResolved() {
        return ( variantNumber > 0 );
    }
    
    int getVariantNumber() {
        return this.variantNumber;
    }
    
    String getResolvedChoice() {
        return this.madeChoice;
    }
}
