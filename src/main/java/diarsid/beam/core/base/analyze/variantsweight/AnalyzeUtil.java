/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.beam.core.base.objects.Cache.getCached;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.MathUtil.meanSmartIngoringZeros;
import static diarsid.beam.core.base.util.MathUtil.percentAsInt;
import static diarsid.beam.core.base.util.MathUtil.ratio;

/**
 *
 * @author Diarsid
 */
class AnalyzeUtil {     
    
    private static final double ADJUSTED_WEIGHT_TRESHOLD = 30;
    
    public static void main(String[] args) {
        List<List<Integer>> ints = asList(
                asList(2, -1, -1),
                asList(3, -2, 1, -3),
                asList(3, -2, 2, -3),
                asList(1, -1),
                asList(-1, -2),
                asList(-1, -1),
                asList(3, -1),
                asList(-1, -2),
                asList(-2, -1),
                asList(2, -1),
                asList(-2, -4),
                asList(1, -1, -5),
                asList(-1, -1, +2),
                asList(1, 1, 1, 1, -4),
                asList(1, -1),
                asList(-1, 1, 1),
                asList(-1, -1, -1, 3, -2),
                asList(-1, -1, -1, -1, -2),
                asList(3, 4, 2),
                asList(3, 1, -2, -2),
                asList(6, 4, 5, 5)
        );
        
        for (List<Integer> list : ints) {
            calculateCluster(list, list.size());
        }
    }
    
    static int inconsistencyOf(Cluster orderDiff, int clusterLength) {
        int percent = percentAsInt(orderDiff.ordersDiffCount(), clusterLength) / 10;
        return (percent + orderDiff.ordersDiffSum() + (orderDiff.ordersDiffSum()/2) ) * orderDiff.ordersDiffSum();
    }
    
    static Cluster calculateCluster(List<Integer> ints, int clusterLength) {
        int mean = meanSmartIngoringZeros(ints);
        if ( POSITIONS_CLUSTERS.isEnabled() ) {
            logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster order diffs         %s", 
                    ints.stream().map(i -> i.toString()).collect(joining(" ")));
            logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster order diffs mean    %s", mean);
        }
        
        int limit = ints.size() - 1;
        
        int current;
        int next;
        
        int repeat = 0;
        int repeatQty = 0;
        int shifts = 0;
        boolean haveCompensation = false;
        boolean previousIsRepeat = false;
        int repeatCommonDelta;
        int violatingOrder = 0;
        boolean isLastPair = false;
        boolean isFirstPair = true;
        
        int diffSum = absDiff(ints.get(0), mean);
        int diffCount = 0;
        if ( ints.get(0) != mean ) {
            diffCount++;
        }
        
        if ( diffSum > 0 ) {
            violatingOrder = diffSum;
        }
        
        for (int i = 0; i < limit; i++) {            
            isLastPair = ( i == limit );
            current = ints.get(i);
            next = ints.get(i + 1);
            diffSum = diffSum + absDiff(next, mean);
            if ( next != mean ) {
                diffCount++;
            }
            
            if ( current == next ) { 
                previousIsRepeat = true;
                if ( isFirstPair ) {
                    violatingOrder = 0;
                }
                repeat = current;
                if ( repeatQty == 0 ) {
                    repeatQty = repeatQty + 2;
                } else {
                    repeatQty++;
                }
                if ( violatingOrder > 0 ) {
                    repeatCommonDelta = absDiff(repeat * repeatQty, mean * repeatQty); 
                    if ( violatingOrder == repeatCommonDelta ) {
                        logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] compensation for (%s * %s)_vs_%s", repeat, repeatQty, ints.get(0));
                        shifts = shifts + repeatQty;
                        diffSum = diffSum - (repeatCommonDelta * 2);
                        previousIsRepeat = false;
                        repeat = 0;
                        repeatQty = 0;
                        violatingOrder = 0;
                        haveCompensation = true;
                    }
                }                
            } else {
                if ( absDiff(current, next) == 2 && absDiff(current, mean) == 1 ) {
                    logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] mutual +1-1 compensation for %s_vs_%s", current, next);
                    haveCompensation = true;
                    diffSum = diffSum - 2;
                } else {
                    if ( violatingOrder == 0 ) {
                        violatingOrder = absDiff(next, mean);
                        if ( previousIsRepeat ) {
                            repeatCommonDelta = absDiff(repeat * repeatQty, mean * repeatQty);
                            if ( violatingOrder == repeatCommonDelta ) {
                                logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] compensation for (%s * %s)_vs_%s", repeat, repeatQty, next);
                                diffSum = diffSum - (repeatCommonDelta * 2);
                                violatingOrder = 0;
                                shifts = shifts + repeatQty;
                                haveCompensation = true;
                            }
                        }
                    }                    
                }                
                previousIsRepeat = false;
                repeat = 0;
                repeatQty = 0;
            }
            isFirstPair = false;
        }
        
        if ( POSITIONS_CLUSTERS.isEnabled() ) {
            if ( repeat != 0 ) {
                logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] repeating order     : %s", repeat);
                logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] repeating order qty : %s", repeatQty);
            } else {
                logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] no repeats");
            }    
            logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster order diff sum      %s", diffSum);
            logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster order diff count    %s", diffCount);
        }
        if ( diffSum == 0 && haveCompensation && clusterLength == 2 ) {            
            diffSum = 1;
            logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster order diff sum fix  %s", diffSum);
        }
        return getCached(Cluster.class)
                .set(clusterLength, mean, diffSum, diffCount, shifts, haveCompensation);
    }
    
    static int lengthTolerance(int variantLength) {
        if ( variantLength < 20 ) {
            return 0;
        } else {
            return ( ( variantLength - 11 ) / 10 ) * 5;
        }
    }
    
    static double missedRatio(double clustersImportance) {
        if ( clustersImportance < 0 ) {
            return 19.0;
        } else if ( clustersImportance >= 0.0 && clustersImportance < 10.0 ) {
            return 14.0;
        } else if ( clustersImportance >= 10.0 && clustersImportance < 20.0 ) {
            return 12.0;
        } else if ( clustersImportance >= 20.0 && clustersImportance < 30.0 ) {
            return 10.0;
        } else if ( clustersImportance >= 30.0 && clustersImportance < 40.0 ) {
            return 8.0;
        } else if ( clustersImportance >= 40.0 && clustersImportance < 60.0 ) {
            return 6.0;
        } else if ( clustersImportance >= 60.0 && clustersImportance < 80.0 ) {
            return 4.0;
        } else if ( clustersImportance >= 80.0 && clustersImportance < 100.0 ) {
            return 2.0;
        } else if ( clustersImportance >= 100.0 && clustersImportance < 130.0 ) {
            return 1.0;
        } else {
            return 0.5;
        }
    }
    
    static double unsortedRatioDependingOn(double clustersImportance) {
        if ( clustersImportance < 0 ) {
            return 26.8;
        } else if ( clustersImportance >= 0.0 && clustersImportance < 10.0 ) {
            return 14.3;
        } else if ( clustersImportance >= 10.0 && clustersImportance < 20.0 ) {
            return 8.9;
        } else if ( clustersImportance >= 20.0 && clustersImportance < 30.0 ) {
            return 5.1;
        } else if ( clustersImportance >= 30.0 && clustersImportance < 40.0 ) {
            return 3.7;
        } else if ( clustersImportance >= 40.0 && clustersImportance < 60.0 ) {
            return 2.3;
        } else if ( clustersImportance >= 60.0 && clustersImportance < 80.0 ) {
            return 1.8;
        } else if ( clustersImportance >= 80.0 && clustersImportance < 100.0 ) {
            return 1.1;
        } else if ( clustersImportance >= 100.0 && clustersImportance < 130.0 ) {
            return 0.8;
        } else {
            return 0.2;
        }
    }

    static boolean isDiversitySufficient(double minWeight, double maxWeight) {
        return ((maxWeight - minWeight) > (minWeight * 0.25));
    }
    
    static final int CLUSTER_QTY_TRESHOLD = 4;
    static double clustersImportanceDependingOn(int clustersQty, int clustered, int nonClustered) {
        return clustersImportance_v2(clustersQty, nonClustered, clustered);
    }
    
    private static double clustersImportance_v2(int clustersQty, int nonClustered, int clustered) {
        if ( clustersQty == 0 ) {
            return CLUSTER_QTY_TRESHOLD * nonClustered * -1.0 ;
        }
        if ( clustered < clustersQty * 2 ) {
            return 0;
        }
        
        double ci;
        int clusteredPercent = percentAsInt(clustered, clustered + nonClustered);
        if ( clustersQty < CLUSTER_QTY_TRESHOLD ) {
            ci = clusteredPercent * clustered * (CLUSTER_QTY_TRESHOLD - clustersQty) / 10;
        } else {
            ci = clusteredPercent * clustered * 0.8 / 10;
        } 
        
        if ( nonClustered > 1 ) {      
            double ratio = 1.0 - (nonClustered * 0.15);
            if ( ratio < 0.2 ) {
                ratio = 0.2;
            }
            ci = ci * ratio;
        }
        
        return ci;
    }

    private static double clustersImportance_v1(int clustersQty, int nonClustered, int clustered) {
        if ( clustersQty == 0 ) {
            return CLUSTER_QTY_TRESHOLD * nonClustered * -1.0 ;
        }
        
        if ( nonClustered == 0 ) {
            if ( clustersQty < CLUSTER_QTY_TRESHOLD ) {
                return clustered * clustered * (CLUSTER_QTY_TRESHOLD - clustersQty);
            } else {
                return clustered * clustered * 0.8;
            }            
        }
        
        if ( clustersQty > CLUSTER_QTY_TRESHOLD ) {
            return ( clustersQty - CLUSTER_QTY_TRESHOLD ) * -8.34;
        }
        
        double result = 1.32 * ( ( CLUSTER_QTY_TRESHOLD - clustersQty ) * 1.0 ) *
                ( 1.0 + ( ( clustered * 1.0 ) / ( nonClustered * 1.0 ) ) ) * 
                ( ( ( clustered * 1.0 ) / ( clustersQty * 1.0 ) ) * 0.8 - 0.79 ) + ( ( clustered - 2 ) * 1.0 ) ;
        return result;
    }
    
    static double lengthImportanceRatio(int length) {
        int lengthSteps = length / 5;
        double ratio = 0.5 + (lengthSteps * 0.07);
        if ( ratio > 1.0 ) {
            ratio = ratio + lengthSteps * 0.05;
        }
        return ratio;
    }
    
    static int nonClusteredImportanceDependingOn(int nonClustered, int missed, int patternLength) {
        int importance = (patternLength - 3 + 5);
        return importance * nonClustered;
    }
    
    static boolean isVariantOkWhenAdjusted(WeightedVariant variant) {
        return variant.weight() <= 
                ADJUSTED_WEIGHT_TRESHOLD + lengthTolerance(variant.text().length());
    }
    
    static boolean missedTooMuch(int missed, int patterLength) {
        return ( ratio(missed, patterLength) >= 0.32 );
    }

    static double missedImportanceDependingOn(
            int missed, double clustersImportance, int patternLength, int variantLength) {
        if ( missed == 0 ) {
            return 0;
        }
        
        double baseMissedImportance = ( missed - 0.25 ) * missedRatio(clustersImportance);
        return baseMissedImportance;
//        if ( patternLength > variantLength ) {
//            return baseMissedImportance * ( absDiff(patternLength, variantLength) + 1.5 );
//        } else if ( patternLength == variantLength ) {
//            return baseMissedImportance * 1.5;
//        } else {
//            return baseMissedImportance;
//        }
    }
}
