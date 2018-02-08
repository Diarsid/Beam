/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.String.format;

import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.MathUtil.onePointRatio;
import static diarsid.beam.core.base.util.MathUtil.ratio;

/**
 *
 * @author Diarsid
 */
class AnalyzeUtil {     
    
    private static final double WEIGHT_TRESHOLD = 30;
    
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
    
    /*
     * Returns importance of unsorted characters in a pattern.
     * Positive value means that unsorted characters makes pattern evaluation worse.
     * Negative value means that there are not unsorted characters, and pattern becames better 
     * depending on other conditions.
     */
    static double unsortedImportanceDependingOn(
            int patternInVariantLength, 
            int patternLength, 
            int unsorted, 
            int clustered, 
            double clustersImportance) {
        if ( clustered > 0 ) {
            if ( unsorted == 0 ) {
                return -( 
                        patternLength + 
                        (unsortedRatioDependingOn(clustersImportance) * 
                                onePointRatio(patternLength, patternInVariantLength)) );
            } else {
                double ratio = ratio(patternLength, patternInVariantLength);
                return unsorted * (unsorted - 0.8) * pow(onePointRatio(unsorted, patternLength), 2) *
                        unsortedRatioDependingOn(clustersImportance) * ( 2.45 + ratio );
            } 
        } else {
            if ( unsorted == 0 ) {
                return -pow(patternLength, onePointRatio(patternLength, patternInVariantLength));
            } else {
                double ratio = ratio(patternLength, patternInVariantLength);
                return unsorted * 
                        pow(unsortedRatioDependingOn(clustersImportance), 1.55 + ratio ) * 
                        (1.5 + ratio);
            }
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
    static double clustersImportanceDependingOn(
            int clustersQty, int clustered, int nonClustered) {
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
    
    public static int countUsorted(int[] data) {
        int unsorted = 0;
        for (int i = 0; i < data.length - 1; i++) {
            if ( data[i + 1] == -1 ) {
                continue;
            }
            if ( data[i] > data[i + 1] ) {
                unsorted++;
            }
        }
        return unsorted;
    }
    
    public static int sortAndCountSteps(int[] data) {
        int steps = 0;
        if ( data.length < 2 ) {
            return 0;
        } else if ( data.length == 2 ) {
            if ( data[0] > data[1] ) {
                int swap = data[0];
                data[0] = data[1];
                data[1] = swap;
                return 1;
            }
            return 0;
        }
        
        boolean dataIsUnsorted = true;
        int current;
        int next;
        int dataLength = data.length;
        
        while ( dataIsUnsorted ) {
            dataIsUnsorted = false;
            for (int i = 0; i < dataLength - 1; i++) {
                current = data[i];
                next = data[i + 1];
                if ( current > next ) {
                    if ( abs(current - next) > 1 ) {
                        System.out.println(format("bad current %s next %s", current, next));
                        steps++;
                    } 
                    data[i] = next;
                    data[i + 1] = current;
                    dataIsUnsorted = true;   
                } else {
                    steps--;
                }
            }
        }
        return steps;
    }
    
    public static void main(String[] args) {
        int[] a = new int[] {10,4,-1,-1,-1,2,9,8};
        System.out.println("unsorted: " + countUsorted(a));
    }
    
    static boolean isVariantOk(WeightedVariant variant) {
        return variant.weight() <= WEIGHT_TRESHOLD + lengthTolerance(variant.text().length());
    }

    static boolean isVariantTextLengthTooBad(double variantWeight, int variantLength) {
        return variantWeight > WEIGHT_TRESHOLD + lengthTolerance(variantLength);
    }
    
    static boolean missedTooMuch(int missed, int variantLength) {
        return ( ( (missed * 1.0) / (variantLength * 1.0) ) > 0.34 );
    }

    static double missedImportanceDependingOn(
            int missed, double clustersImportance, int patternLength, int variantLength) {
        if ( missed == 0 ) {
            return -9.6;
        }
        
        double baseMissedImportance = ( ( missed * 1.0) - 0.5 ) * missedRatio(clustersImportance);
        if ( patternLength > variantLength ) {
            return baseMissedImportance * ( absDiff(patternLength, variantLength) + 1.5 );
        } else if ( patternLength == variantLength ) {
            return baseMissedImportance * 1.5;
        } else {
            return baseMissedImportance;
        }
    }
}
