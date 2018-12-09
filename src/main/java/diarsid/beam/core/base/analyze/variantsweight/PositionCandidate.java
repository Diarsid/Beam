/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import static java.lang.String.format;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_SEARCH;
import static diarsid.beam.core.base.util.MathUtil.absDiff;

/**
 *
 * @author Diarsid
 */
class PositionCandidate {
    
    private static final int UNINITIALIZED = -9;
    private static final boolean CURRENT_IS_BETTER = false;
    private static final boolean CURRENT_IS_WORSE = true;
    
    private int position;
    private int orderDiffInPattern;
    private int orderDiffInVariant;
    private int clusteredAround;
    private int mutationsCommitted;
    private int mutationsAttempts;

    public PositionCandidate() {
        this.position = UNINITIALIZED;
        this.orderDiffInPattern = UNINITIALIZED;
        this.clusteredAround = UNINITIALIZED;
        this.mutationsCommitted = 0;
        this.mutationsAttempts = 0;
    }
    
    void tryToMutate(int position, int orderDiffInVariant, int orderDiffInPattern, int clusteredAround, int charsRemained) {
        this.mutationsAttempts++;
        
        if (POSITIONS_SEARCH.isEnabled()) {
            logAnalyze(POSITIONS_SEARCH, "          [info] candidate %s in variant has:", position);
            logAnalyze(POSITIONS_SEARCH, "             pattern order diff  %s", orderDiffInPattern);
            logAnalyze(POSITIONS_SEARCH, "             variant order diff  %s", orderDiffInVariant == UNINITIALIZED ? "_" : orderDiffInPattern);
            logAnalyze(POSITIONS_SEARCH, "             clustered positions %s", clusteredAround);
            logAnalyze(POSITIONS_SEARCH, "             chars remained %s", charsRemained);
        }
        
        if ( this.isCurrentStateWorseThan(
                orderDiffInVariant, orderDiffInPattern, clusteredAround) ) {
            logAnalyze(POSITIONS_SEARCH, "          [info] accept %s", position);
            this.resetTo(position, orderDiffInVariant, orderDiffInPattern, clusteredAround);
        } else {
            logAnalyze(POSITIONS_SEARCH, "          [info] worse than %s, reject %s", this.position, position);
        }       
    }
    
    private boolean isCurrentStateWorseThan(
            int otherOrderDiffInVariant, int otherOrderDiffInPattern, int otherClusteredAround) {
        if ( this.position == UNINITIALIZED ) {
            return CURRENT_IS_WORSE;
        }
        
        if ( this.orderDiffInPattern == 1 && this.orderDiffInVariant == 1 ) {
            if ( otherOrderDiffInPattern == 1 && otherOrderDiffInVariant == 1 ) {
                if ( otherClusteredAround > this.clusteredAround ) {
                    return CURRENT_IS_WORSE;
                } else {
                    return CURRENT_IS_BETTER;
                }
            }
        }
        
        int thisSum = this.orderDiffInPattern + this.orderDiffInVariant - this.clusteredAround;
        int otherSum = otherOrderDiffInPattern + otherOrderDiffInVariant - otherClusteredAround;
        if ( thisSum > otherSum ) {
            return CURRENT_IS_WORSE;
        } else {
            return CURRENT_IS_BETTER;
        }
//        if ( otherOrderDiffInVariant < 0 ) {
//            return this.compareIgnoringVariantDiff(
//                    otherOrderDiffInPattern, otherClusteredAround);
//        } else {
//            return this.compareCountingVariantDiff(
//                    otherOrderDiffInVariant, otherOrderDiffInPattern, otherClusteredAround);
//        }
    }
    
    private boolean compareIgnoringVariantDiff(
            int otherOrderDiffInPattern, int otherClusteredAround) {
        if ( this.clusteredAround > otherClusteredAround ) {
            if ( this.orderDiffInPattern <= otherOrderDiffInPattern ) {
                return CURRENT_IS_BETTER;
            } else {
                int orderDiff = absDiff(this.orderDiffInPattern, otherOrderDiffInPattern);
                int clusteredDiff = absDiff(this.clusteredAround, otherClusteredAround);
                
                if ( orderDiff > (clusteredDiff * 2) ) {
                    return CURRENT_IS_WORSE;
                } else {
                    return CURRENT_IS_BETTER;
                }
            } 
        } else if ( this.clusteredAround == otherClusteredAround ) {
            if ( this.orderDiffInPattern <= otherOrderDiffInPattern ) {
                return CURRENT_IS_BETTER;
            } else {
                return CURRENT_IS_WORSE;
            }            
        } else {
            if ( this.orderDiffInPattern >= otherOrderDiffInPattern ) {
                return CURRENT_IS_WORSE;
            } else {
                int orderDiff = absDiff(this.orderDiffInPattern, otherOrderDiffInPattern);
                int clusteredDiff = absDiff(this.clusteredAround, otherClusteredAround);
                
                if ( orderDiff > (clusteredDiff * 2) ) {
                    return CURRENT_IS_BETTER;
                } else {
                    return CURRENT_IS_WORSE;
                }
            }
        }
    }
    
    private boolean compareCountingVariantDiff(
            int otherOrderDiffInVariant, int otherOrderDiffInPattern, int otherClusteredAround) {
        if ( this.clusteredAround > otherClusteredAround ) {
            if ( this.orderDiffInPattern <= otherOrderDiffInPattern ) {
                if ( this.orderDiffInVariant <= otherOrderDiffInVariant ) {
                    return CURRENT_IS_BETTER;
                } else {
                    return CURRENT_IS_WORSE;
                }
            } else {
                int orderAbsDiff = absDiff(this.orderDiffInPattern, otherOrderDiffInPattern);
                int clusteredAroundAbsDiff = absDiff(this.clusteredAround, otherClusteredAround);
                
                if ( orderAbsDiff > (clusteredAroundAbsDiff * 2) ) {
                    return CURRENT_IS_WORSE;
                } else {
                    return CURRENT_IS_BETTER;
                }
            } 
        } else if ( this.clusteredAround == otherClusteredAround ) {
            boolean thisPatternDiffIsBetter = this.orderDiffInPattern <= otherOrderDiffInPattern;
            boolean thisVariantDiffIsBetter = this.orderDiffInVariant <= otherOrderDiffInVariant;
            int patternDiff = absDiff(this.orderDiffInPattern, otherOrderDiffInPattern);
            int variantDiff = absDiff(this.orderDiffInVariant, otherOrderDiffInVariant);
            
            if ( thisPatternDiffIsBetter && thisVariantDiffIsBetter ) {
                return CURRENT_IS_BETTER;
            } else if ( thisPatternDiffIsBetter /* and Variant diff is worse */ ) { 
                if ( patternDiff <= variantDiff ) {
                    return CURRENT_IS_BETTER;
                } else {
                    return CURRENT_IS_WORSE;
                }
            } else if ( thisVariantDiffIsBetter /* and Pattern diff is worse */ ) { 
                return CURRENT_IS_BETTER;
            } else {
                return CURRENT_IS_WORSE;
            }         
        } else {
            if ( this.orderDiffInPattern >= otherOrderDiffInPattern ) {
                return CURRENT_IS_WORSE;
            } else {
                int orderAbsDiff = absDiff(this.orderDiffInPattern, otherOrderDiffInPattern);
                int clusteredAroundAbsDiff = absDiff(this.clusteredAround, otherClusteredAround);
                
                if ( orderAbsDiff > (clusteredAroundAbsDiff * 2) ) {
                    return CURRENT_IS_BETTER;
                } else {
                    return CURRENT_IS_WORSE;
                }
            }
        }
    }

    private void resetTo(
            int position, int orderDiffInVariant, int orderDiffInPattern, int clusteredAround) {
        this.position = position;
        this.orderDiffInPattern = orderDiffInPattern;
        this.orderDiffInVariant = orderDiffInVariant;
        this.clusteredAround = clusteredAround;
        this.mutationsCommitted++;
    }
    
    boolean isPresent() {
        return this.position != UNINITIALIZED;
    }
    
    int position() {
        return this.position;
    }
    
    int committedMutations() {
        return this.mutationsCommitted;
    }
    
    int mutationAttempts() {
        return this.mutationsAttempts;
    }
    
    boolean hasAtLeastOneAcceptedCandidate() {
        return this.mutationsCommitted > 0;
    }
    
    boolean hasRejectedMutations() {
        return this.mutationsAttempts > this.mutationsCommitted;
    }
    
    void clear() {
        this.position = UNINITIALIZED;
        this.orderDiffInPattern = UNINITIALIZED;
        this.orderDiffInVariant = UNINITIALIZED;
        this.clusteredAround = UNINITIALIZED;
        this.mutationsAttempts = 0;
        this.mutationsCommitted = 0;
    }

    @Override
    public String toString() {
        if ( this.position == UNINITIALIZED ) {
            return "PositionCandidate[UNINITIALIZED]";
        }
        
        return format("PositionCandidate[pos:%s clusteredAround:%s mutations:[attemtps:%s committed:%s] ]", 
                      this.position, this.clusteredAround, this.mutationsAttempts, this.mutationsCommitted);
    }
    
}
