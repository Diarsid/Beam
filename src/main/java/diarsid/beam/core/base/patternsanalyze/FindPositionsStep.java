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
    STEP_1 (2), 
    STEP_2 (1), 
    STEP_3 (0);
    
    private final int permissionTreshold;
    
    private FindPositionsStep(int permissionTreshold) {
        this.permissionTreshold = permissionTreshold;
    }
    
    boolean canAddToPositions(int charsInCluster) {
        return charsInCluster >= this.permissionTreshold;
    }
}
