/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.patternsanalyze;

/**
 *
 * @author Diarsid
 */
class AnalyzeUtil { 
    
    static boolean isVariantOk(WeightedVariant variant) {
        return variant.weight() <= 80 + lengthTolerance(variant.text().length());
    }
    
    static int lengthTolerance(int variantLength) {
        if ( variantLength < 36 ) {
            return 0;
        } else {
            return ( ( variantLength - 20 ) / 15 ) * 5;
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
    
    static double sortingStepsImportanceDependingOn(
            int sortingSteps, double clustersImportance) {
        return sortingSteps * sortingStepsRatio(clustersImportance);
    }
    
    static double sortingStepsRatio(double clustersImportance) {
        if ( clustersImportance < 0 ) {
            return 14.3;
        } else if ( clustersImportance >= 0.0 && clustersImportance < 10.0 ) {
            return 8.9;
        } else if ( clustersImportance >= 10.0 && clustersImportance < 20.0 ) {
            return 7.3;
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
    
    static int CLUSTER_QTY_TRESHOLD = 4;
    static double clustersImportanceDependingOn(
            int clustersQty, int clustered, int nonClustered) {
        if ( clustersQty == 0 ) {
            return CLUSTER_QTY_TRESHOLD * nonClustered * -1.0 ;
        }
        if ( nonClustered == 0 ) {
            return clustered * clustered * 1.0;
        }
        if ( clustersQty > CLUSTER_QTY_TRESHOLD ) {
            return ( clustersQty - CLUSTER_QTY_TRESHOLD ) * -8.34;
        }
        
        return 1.32 * ( ( CLUSTER_QTY_TRESHOLD - clustersQty ) * 1.0 ) * 
                ( 1.0 + ( ( clustered * 1.0 ) / ( nonClustered * 1.0 ) ) ) * 
                ( ( ( clustered * 1.0 ) / ( clustersQty * 1.0 ) ) * 0.8 - 0.79 ) + ( ( clustered - 2 ) * 1.0 ) ;
    }
    
    static double clusterWeightRatioDependingOn(
            boolean containsFirstChar, 
            boolean firstCharsMatchInVariantAndPattern) {
        if ( containsFirstChar ) {
            if ( firstCharsMatchInVariantAndPattern ) {
                System.out.println("first chars matches!");
                return 0.9;
            } else {
                return 1.5;                
            }
        } else {
            return 2.2;
        }
    }
    
    static double firstCharMatchRatio(boolean isMatch) {
        if ( isMatch ) {
            return 8.0;
        } else {
            return 0.0;
        }
    }
    
    public static int countUsorted(int[] data) {
        int unsorted = 0;
        for (int i = 0; i < data.length - 1; i++) {
            if ( data[i] > data[i + 1] ) {
                unsorted++;
            }
        }
        if ( unsorted > 0 ) {
            unsorted++;
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
                    data[i] = next;
                    data[i + 1] = current;
                    steps++;
                    dataIsUnsorted = true;   
                }
            }
        }
        return steps;
    }
    
    static boolean isWordsSeparator(char c) {
        return 
                c == '.' ||
                c == ',' ||
                c == ' ' || 
                c == '_' || 
                c == '-' || 
                c == '/' || 
                c == '\\';
    }

    static boolean isVariantTextLengthTooBad(double variantWeight, int variantLength) {
        return variantWeight > 80 + lengthTolerance(variantLength);
    }
    
    static boolean missedTooMuch(int missed, int variantLength) {
        return ( ( (missed * 1.0) / (variantLength * 1.0) ) > 0.34 );
    }

    static double missedImportanceDependingOn(int missed, double clustersImportance) {
        if ( missed == 0 ) {
            return 0.0;
        }
        return ( ( missed * 1.0) - 0.8 ) * missedRatio(clustersImportance);
    }
}
