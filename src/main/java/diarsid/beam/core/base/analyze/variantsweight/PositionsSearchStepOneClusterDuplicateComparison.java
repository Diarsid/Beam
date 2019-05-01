/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import diarsid.beam.core.base.analyze.variantsweight.PositionsSearchStepOneCluster.PatternCluster;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.analyze.variantsweight.ClusterComparison.LEFT_IS_BETTER;
import static diarsid.beam.core.base.analyze.variantsweight.ClusterComparison.LEFT_IS_WORSE;
import static diarsid.beam.core.base.analyze.variantsweight.PositionsSearchStepOneCluster.calculateAdditionalPossibleTypoMatches;

/**
 *
 * @author Diarsid
 */
public enum PositionsSearchStepOneClusterDuplicateComparison {
    
    A;
    
    static ClusterComparison compare(
            PositionsSearchStepOneCluster one, PositionsSearchStepOneCluster two) {
        if ( one.patternCluster().equals(two.patternCluster()) ) {
            PatternCluster commonPatternCluster = one.patternCluster();
            
            if ( commonPatternCluster.isAtPatternStart() ) {
                if ( one.isAtStart() ) {
                    return LEFT_IS_BETTER;
                } else if ( two.isAtStart() ) {
                    return LEFT_IS_WORSE;
                } else {
                    return compareByPossibleTypoMatches(one, two);
                }
            } else if ( commonPatternCluster.isAtPatternEnd() ) {
                if ( one.isAtEnd() ) {
                    return LEFT_IS_BETTER;
                } else if ( two.isAtEnd() ) {
                    return LEFT_IS_WORSE;
                } else {
                    return compareByPossibleTypoMatches(one, two);
                }
            } else {
                return compareByPossibleTypoMatches(one, two);
            }
        } else {            
            return compareByPossibleTypoMatches(one, two);
        }
    }

    private static ClusterComparison compareByPossibleTypoMatches(
            PositionsSearchStepOneCluster one, PositionsSearchStepOneCluster two) {
        if ( one.doesHaveMorePossibleTypoMatchesThan(two) ) {
            return LEFT_IS_BETTER;
        } else if ( two.doesHaveMorePossibleTypoMatchesThan(one) ) {
            return LEFT_IS_WORSE;
        } else {
            ClusterComparison comparison = calculateAdditionalPossibleTypoMatches(one, two);
            if ( nonNull(comparison) ) {
                return comparison;
            } else {
                if ( one.doesStartAfterSeparator() ) {
                    if ( two.doesStartAfterSeparator() ) {
                        if ( one.doesEndBeforeSeparator() ) {
                            return LEFT_IS_BETTER;
                        } else if ( two.doesEndBeforeSeparator() ) {
                            return LEFT_IS_WORSE;
                        } else {
                            return LEFT_IS_WORSE;
                        }
                    } else {
                        return LEFT_IS_BETTER;
                    }
                } else if ( two.doesStartAfterSeparator() ) {
                    return LEFT_IS_WORSE;
                } else {
                    if ( one.doesEndBeforeSeparator() ) {
                        return LEFT_IS_BETTER;
                    } else if ( two.doesEndBeforeSeparator() ) {
                        return LEFT_IS_WORSE;
                    } else {
                        return LEFT_IS_WORSE;
                    }
                }
            }
        }
    }
    
}
