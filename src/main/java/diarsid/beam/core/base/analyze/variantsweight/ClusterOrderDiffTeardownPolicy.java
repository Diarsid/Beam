/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.function.BiFunction;

import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
enum ClusterOrderDiffTeardownPolicy {

    PATTERN_LENGTH_2((clusterLength, diff) -> {
        if ( diff > 0 ) {
            return 1;
        } else {
            return 0;
        }                
    }),
    
    PATTERN_LENGTH_3((clusterLength, diff) -> {
        if ( diff > 0 ) {
            return 1;
        } else {
            return 0;
        }  
    }),
    
    PATTERN_LENGTH_4((clusterLength, diff) -> {
        if ( diff == 0 ) {
            return 0;
        }
        
        switch ( clusterLength ) {
            case 2 : {
                if ( diff > 0 ) {
                    return 1;
                } else {
                    return 0;
                }
            }
            case 3 : {
                switch ( diff ) {
                    case 0: return 0;
                    case 1: return 1;
                    case 2:
                    case 3: return 2;
                    default: throw new IllegalArgumentException("Pattern length invalid.");
                }
            }
            case 4 : {
                switch ( diff ) {
                    case 0: return 0;
                    case 1: return 1;
                    case 2:
                    case 3: return 2;
                    case 4: return 3;
                    default: throw new IllegalArgumentException("Pattern length invalid.");
                }
            }
            default : {
                throw new IllegalArgumentException("Pattern length invalid.");
            }
        }
    }),
    
    PATTERN_LENGTH_5((clusterLength, diff) -> {
        return 0;
    }),
    
    PATTERN_LENGTH_6_AND_LONGER((clusterLength, diff) -> {
        return 0;
    });
    
    private final BiFunction<Integer, Integer, Integer> clusterTeardownCalculation;
    
    private ClusterOrderDiffTeardownPolicy() {
        this.clusterTeardownCalculation = null;
    }

    private ClusterOrderDiffTeardownPolicy(
            BiFunction<Integer, Integer, Integer> clusterTeardownCalculation) {
        this.clusterTeardownCalculation = clusterTeardownCalculation;
    }

    static ClusterOrderDiffTeardownPolicy teardownPolicyFor(int patternLength) {
        switch ( patternLength ) {
            case 0 :
            case 1 : throw new IllegalStateException("Pattern with length < 2 do not acceptable!");
            case 2 : return PATTERN_LENGTH_2;
            case 3 : return PATTERN_LENGTH_3;
            case 4 : return PATTERN_LENGTH_4;
            case 5 : return PATTERN_LENGTH_5;                
            default : return PATTERN_LENGTH_6_AND_LONGER;                
        }
    }

    boolean doesAllowTeardown() {
        return nonNull(this.clusterTeardownCalculation);
    }

    int clusterPartToBeTeardown(int clusterLength, int diff) {
        return this.clusterTeardownCalculation.apply(clusterLength, diff);
    }    
    
}
