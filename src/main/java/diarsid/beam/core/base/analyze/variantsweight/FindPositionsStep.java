/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

/**
 *
 * @author Diarsid
 */
public enum FindPositionsStep {
    
    STEP_1 (/*order*/ 0, /*permission treshold*/ 2, /*typo searching allowed*/ false), 
    STEP_2 (/*order*/ 1, /*permission treshold*/ 1, /*typo searching allowed*/ true), 
    STEP_3 (/*order*/ 2, /*permission treshold*/ 0, /*typo searching allowed*/ false);
    
    private final int order;
    private final int permissionTreshold;
    private final boolean typoSearchingAllowed;
    
    private FindPositionsStep(int order, int permissionTreshold, boolean typoSearchingAllowed) {
        this.order = order;
        this.permissionTreshold = permissionTreshold;
        this.typoSearchingAllowed = typoSearchingAllowed;
    }
    
    boolean canAddToPositions(int charsInCluster) {
        return charsInCluster >= this.permissionTreshold;
    }
    
    boolean typoSearchingAllowed() {
        return this.typoSearchingAllowed;
    }
    
    boolean isLaterThan(FindPositionsStep other) {
        return this.order > other.order;
    }
}
