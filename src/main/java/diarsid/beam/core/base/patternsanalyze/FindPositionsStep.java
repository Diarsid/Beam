/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.patternsanalyze;

/**
 *
 * @author Diarsid
 */
public enum FindPositionsStep {
    STEP_1 (2, false), 
    STEP_2 (1, true), 
    STEP_3 (0, false);
    
    private final int permissionTreshold;
    private final boolean typoSearchingAllowed;
    
    private FindPositionsStep(int permissionTreshold, boolean typoSearchingAllowed) {
        this.permissionTreshold = permissionTreshold;
        this.typoSearchingAllowed = typoSearchingAllowed;
    }
    
    boolean canAddToPositions(int charsInCluster) {
        return charsInCluster >= this.permissionTreshold;
    }
    
    boolean typoSearchingAllowed() {
        return this.typoSearchingAllowed;
    }
}
