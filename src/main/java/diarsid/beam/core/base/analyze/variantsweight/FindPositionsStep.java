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
    
    STEP_1 (
            /*order*/ 0, 
            /*permission to cluster chars treshold*/ 2, 
            /*min pattern length to apply step*/ 0, 
            /*typo searching allowed*/ false), 
    STEP_2 (
            /*order*/ 1, 
            /*permission to cluster chars treshold*/ 1, 
            /*min pattern length to apply step*/ 3, 
            /*typo searching allowed*/ true), 
    STEP_3 (
            /*order*/ 2, 
            /*permission to cluster chars treshold*/ 0, 
            /*min pattern length to apply step*/ 5, 
            /*typo searching allowed*/ false);
    
    private final int order;
    private final int permissionToClusterCharTreshold;
    private final int minPatternLength;
    private final boolean typoSearchingAllowed;
    
    private FindPositionsStep(
            int order, 
            int permissionTreshold, 
            int minPatternLength, 
            boolean typoSearchingAllowed) {
        this.order = order;
        this.permissionToClusterCharTreshold = permissionTreshold;
        this.minPatternLength = minPatternLength;
        this.typoSearchingAllowed = typoSearchingAllowed;
    }
    
    boolean canProceedWith(int patternLength) {
        return patternLength >= this.minPatternLength;
    }
    
    boolean canAddToPositions(int charsInCluster) {
        return charsInCluster >= this.permissionToClusterCharTreshold;
    }
    
    boolean typoSearchingAllowed() {
        return this.typoSearchingAllowed;
    }
    
    boolean isAfter(FindPositionsStep other) {
        return this.order > other.order;
    }
}
