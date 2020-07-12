/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import diarsid.beam.core.base.analyze.variantsweight.PositionsSearchStepOneCluster.PatternCluster;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.analyze.variantsweight.ClusterPreference.PREFERE_LEFT;
import static diarsid.beam.core.base.analyze.variantsweight.ClusterPreference.PREFERE_RIGHT;
import static diarsid.beam.core.base.analyze.variantsweight.PositionsSearchStepOneCluster.calculateAdditionalPossibleTypoMatches;

/**
 *
 * @author Diarsid
 */
public enum PositionsSearchStepOneClusterDuplicateComparison {
    
    A;
    
    static ClusterPreference compare(
            PositionsSearchStepOneCluster one, PositionsSearchStepOneCluster two) {
        if ( one.patternCluster().equals(two.patternCluster()) ) {
            PatternCluster commonPatternCluster = one.patternCluster();
            
            if ( commonPatternCluster.isAtPatternStart() ) {
                if ( one.isAtStart() ) {
                    return PREFERE_LEFT;
                } else if ( two.isAtStart() ) {
                    return PREFERE_RIGHT;
                } else {
                    return compareByPossibleTypoMatches(one, two);
                }
            } else if ( commonPatternCluster.isAtPatternEnd() ) {
                if ( one.isAtEnd() ) {
                    return PREFERE_LEFT;
                } else if ( two.isAtEnd() ) {
                    return PREFERE_RIGHT;
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

    private static ClusterPreference compareByPossibleTypoMatches(
            PositionsSearchStepOneCluster one, PositionsSearchStepOneCluster two) {
        if ( one.doesHaveMorePossibleTypoMatchesThan(two) ) {
            return PREFERE_LEFT;
        } else if ( two.doesHaveMorePossibleTypoMatchesThan(one) ) {
            return PREFERE_RIGHT;
        } else {
            ClusterPreference preference = calculateAdditionalPossibleTypoMatches(one, two);
            if ( nonNull(preference) ) {
                return preference;
            } else {
                if ( one.doesStartAfterSeparator() ) {
                    if ( two.doesStartAfterSeparator() ) {
                        if ( one.doesEndBeforeSeparator() ) {
                            return PREFERE_LEFT;
                        } else if ( two.doesEndBeforeSeparator() ) {
                            return PREFERE_RIGHT;
                        } else {
                            return PREFERE_RIGHT;
                        }
                    } else {
                        return PREFERE_LEFT;
                    }
                } else if ( two.doesStartAfterSeparator() ) {
                    return PREFERE_RIGHT;
                } else {
                    if ( one.doesEndBeforeSeparator() ) {
                        return PREFERE_LEFT;
                    } else if ( two.doesEndBeforeSeparator() ) {
                        return PREFERE_RIGHT;
                    } else {
                        return PREFERE_RIGHT;
                    }
                }
            }
        }
    }
    
}
