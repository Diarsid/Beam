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
public enum WeightEstimate {
    
    PERFECT (3),
    GOOD (2),
    MODERATE (1),
    BAD (0);
    
    private final int level;

    private WeightEstimate(int level) {
        this.level = level;
    }
    
    private static final double BAD_BOUND = -10;
    private static final double MODERATE_BOUND = -36;
    private static final double GOOD_BOUND = -75;
    
    public static WeightEstimate estimateWeightOf(Variant variant) {
        return estimate(variant.weight());
    }
    
    static WeightEstimate estimatePreliminarily(double weight) { 
        return estimate(weight - 5.0);
    }
    
    public static WeightEstimate estimate(double weight) {        
        if ( weight > BAD_BOUND ) {
            return BAD;
        } else if ( BAD_BOUND >= weight && weight > MODERATE_BOUND ) {
            return MODERATE;
        } else if ( MODERATE_BOUND >= weight && weight > GOOD_BOUND ) {
            return GOOD;
        } else {
            return PERFECT;
        }
    }
    
    public boolean isEqualOrBetterThan(WeightEstimate other) {
        return this.level >= other.level;
    }
    
    public boolean isBetterThan(WeightEstimate other) {
        return this.level > other.level;
    }
}
