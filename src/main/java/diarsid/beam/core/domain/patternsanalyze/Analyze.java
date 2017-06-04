/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.patternsanalyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Analyze {
    
    
    // TODO sorting steps should tolerate good clusters.
    private Analyze() {        
    }
    
    public static WeightedVariants weightStrings(String pattern, List<String> variants) {
        return weightVariants(pattern, stringsToVariants(variants));
    }
    
    private static List<String> javapathCase() {
        return asList(
                "Engines/java/path", 
                "Books/Tech/Java/JavaFX", 
                "Books/Tech/Java");
    }
    
    private static List<String> facebookCase() {
        return asList(                
                "fb",
                "fixed beam",
                "facebook",
                "epicfantasy crossbooking");
    }
    
    private static List<String> beamProjectCase() {
        return asList(
                "beam_project_home",
                "beam_project",
                "beam_home",
                "awesome java libs",
                "git>beam",
                "beam_project/src",
                "beam netpro",
                "abe_netpro",
                "babel_pro",
                "netbeans_projects", 
                "beam_server_project");
    }
    
    private static List<String> readListCase() {
        return asList("Books/list_to_read.txt", "Tech/CS/Algorithms");
    }

    public static void doAll() {
//        weightAnalyzeCases();

        analyzeImportance();
    }

    private static void analyzeImportance() {
        System.out.println(clustersImportanceDependingOn(1, 4, 3));
        System.out.println(clustersImportanceDependingOn(1, 2, 5));
    }

    private static void weightAnalyzeCases() {
        List<String> variantsStrings = javapathCase();
        
        String pattern = "pathjab";
        
        System.out.println("variants: " + variantsStrings.size());
        WeightedVariants variants = weightStrings(pattern, variantsStrings);
        AtomicInteger printed = new AtomicInteger(0);
        while ( variants.next() ) {            
            if ( variants.currentIsMuchBetterThanNext() ) {
                System.out.println(variants.current().text() + " is much better than next: " + variants.current().weight());
                printed.incrementAndGet();
            } else {
                List<WeightedVariant> similar = variants.nextSimilarVariants();
                System.out.println("next candidates are similar: ");                
                similar.stream().forEach(candidate -> {
                    System.out.println("  - " + candidate.text() + " : " + candidate.weight());
                    printed.incrementAndGet();
                });
            }
        }
        System.out.println("printed: " + printed.get());
    }
    
    private static boolean patternAdvancedSearch(String pattern, String analyzed) {
        int patternLength = pattern.length();
        List<Integer> positions = new ArrayList<>();
        char currentChar;
        int currentCharPosition;
        int maxClusterLength = Integer.MIN_VALUE;
        for (int currentCharIndex = 0; currentCharIndex < patternLength; currentCharIndex++) {
            currentChar = pattern.charAt(currentCharIndex);
            currentCharPosition = analyzed.indexOf(currentChar);
            positions.add(currentCharPosition);
            while ( currentCharPosition > 0 ) {
                currentCharPosition = analyzed.indexOf(currentChar, currentCharPosition + 1);
                if ( currentCharPosition > 0 ) {
                    positions.add(currentCharPosition);
                }
            }
        }
        sort(positions);
        int previousPosition = Integer.MIN_VALUE;
        int foundClusterLength = 0;
        for (Integer position : positions) {
            System.out.print(position + " ");
            if ( position == previousPosition + 1 ) {
                if ( foundClusterLength == 0 ) {
                    foundClusterLength = 2;
                } else {
                    foundClusterLength++;
                }                
            } else {
                if ( foundClusterLength > maxClusterLength ) {
                    maxClusterLength = foundClusterLength;
                    foundClusterLength = 0;
                }
            }
            previousPosition = position;
        }
        if ( foundClusterLength > maxClusterLength ) {
            maxClusterLength = foundClusterLength;
        }
        System.out.println("cluster length: " + maxClusterLength);
        return maxClusterLength >= patternLength;
    }
    
    private static int countWords(String variant) {
        char[] chars = variant.toCharArray();
        char current;
        int wordsCount = 1;
        boolean previousCharIsNotSeparator = true;
        for (int currentIndex = 0; currentIndex < chars.length; currentIndex++) {
            current = chars[currentIndex];
            if ( isWordsSeparator(current) ) {
                if ( currentIndex > 0 ) {
                    if ( previousCharIsNotSeparator ) {
                        wordsCount++;
                        previousCharIsNotSeparator = false;
                    }
                } else {
                    previousCharIsNotSeparator = false;
                }
            } else {
                previousCharIsNotSeparator = true;
            }
        }
        if ( isWordsSeparator(lastCharOf(variant)) ) {
            wordsCount--;
        }
        return wordsCount;
    }

    private static char lastCharOf(String variant) {
        return variant.charAt(variant.length() - 1);
    }
    
    // TODO improve pattern estimate. 
    // TODO throw variant away 
    public static WeightedVariants weightVariants(String pattern, List<Variant> variants) {
        pattern = lower(pattern);
        sort(variants);
        Map<Character, Integer> reusableVisitedChars = new HashMap<>();
        Map<String, WeightedVariant> variantsByDisplay = new HashMap<>();
        WeightedVariant newVariant;
        WeightedVariant prevVariant;
        List<WeightedVariant> weightedVariants = new ArrayList<>();
        String variantText;
        
        int[] positions;
        double variantWeight;
        char[] patternChars;
        int currentCharPosition;
        int possibleBetterCurrentCharPosition;
        char currentChar;
        
        int sortingSteps;
        int missed;
        int clustersQty;
        int clustered;
        int nonClustered;
        int clustersWeight;
        int currentClusterLength;
        int currentPosition;
        int nextPosition;
        boolean clusterContinuation;
        boolean containsFirstChar;
        boolean firstCharsMatchInVariantAndPattern;
        
        double minWeight = MAX_VALUE;
        double maxWeight = MIN_VALUE;
        
        for (Variant variant : variants) {
            variantText = lower(variant.text());
            // positions counting begins...
            patternChars = pattern.toCharArray();
            positions = new int[patternChars.length];

            for (int currentCharIndex = 0; currentCharIndex < patternChars.length; currentCharIndex++) {
                currentChar = patternChars[currentCharIndex];
                if ( reusableVisitedChars.containsKey(currentChar) ) {
                    currentCharPosition = 
                            variantText.indexOf(currentChar, reusableVisitedChars.get(currentChar) + 1);
                } else {
                    currentCharPosition = 
                            variantText.indexOf(currentChar);                
                }
                if ( currentCharPosition > -1 ) {
                    reusableVisitedChars.put(currentChar, currentCharPosition);
                }
                if ( ( currentCharIndex > 0 ) && ( currentCharPosition < positions[currentCharIndex - 1] ) ) {
                    possibleBetterCurrentCharPosition = 
                            variantText.indexOf(currentChar, currentCharPosition + 1);  
                    //System.out.println(format("better position of '%s' in '%s' is: %s instead of: %s", currentChar, variantText, possibleBetterCurrentCharPosition, currentCharPosition));
                    if ( possibleBetterCurrentCharPosition > -1 ) {
                        if ( possibleBetterCurrentCharPosition == positions[currentCharIndex - 1] + 1 ) {
                            System.out.println(format("assign position of '%s' in '%s' as %s instead of %s", currentChar, variantText, possibleBetterCurrentCharPosition, currentCharPosition));
                            currentCharPosition = possibleBetterCurrentCharPosition;
                        } else {
                            possibleBetterCurrentCharPosition = 
                                    variantText.indexOf(currentChar, positions[currentCharIndex - 1] + 1);  
                            if ( ( possibleBetterCurrentCharPosition > -1 ) && 
                                    (possibleBetterCurrentCharPosition == positions[currentCharIndex - 1] + 1) ) {
                                currentCharPosition = possibleBetterCurrentCharPosition;
                            }
                        }
                    } 
                }
                positions[currentCharIndex] = currentCharPosition;            
            }
            reusableVisitedChars.clear();
            // positions counting ends, weight calculation begins...
            sortingSteps = sortAndCountSteps(positions);
            missed = 0;
            variantWeight = 0;
            clustersQty = 0;
            clustered = 0;
            nonClustered = 0;
            clustersWeight = 0;
            currentClusterLength = 0;
            clusterContinuation = false;
            containsFirstChar = false;
            for (int i = 0; i < positions.length; i++) {
                currentPosition = positions[i];
                if ( currentPosition < 0 ) {
                    missed++;
                    continue;
                }
                if ( currentPosition == 0 ) {
                    containsFirstChar = true;
                }
                if ( i < positions.length - 1 ) { 
                    nextPosition = positions[i + 1];
                    if ( currentPosition == nextPosition - 1 ) {                    
                        if ( clusterContinuation ) {                        
                            clustered++;
                            //clustersWeight++;
                            currentClusterLength++;
                        } else {                        
                            clustered++;
                            clustersQty++;
                            clusterContinuation = true;
                            currentClusterLength = 1;
                            clustersWeight = clustersWeight + ( currentPosition / 2 );                        
                            if ( currentPosition > 0 ) {
                                if ( isWordsSeparator(variantText.charAt(currentPosition - 1)) ) {
                                    variantWeight = variantWeight - 4;
                                }
                            }
                        }
                    } else {
                        if ( clusterContinuation ) {
                            clustered++;
                            //clustersWeight++;
                            clusterContinuation = false;
                        } else {
                            nonClustered++;
                        }
                    }
                } else {
                    if ( currentPosition > 0 ) {
                        if ( isWordsSeparator(variantText.charAt(currentPosition - 1)) ) {
                            variantWeight = variantWeight - 3;
                        }
                    }                    
                    if ( clusterContinuation ) {
                        clustered++;
                        //clustersWeight++;
                    } else {
                        nonClustered++;
                    }
                }
            }
            nonClustered = nonClustered + missed;
            firstCharsMatchInVariantAndPattern = ( pattern.charAt(0) == variantText.charAt(0) );
            variantWeight = variantWeight + (
                    ( nonClustered * 5.3 ) 
                    - ( clustered * 4.6 ) 
                    - ( firstCharMatchRatio(containsFirstChar) )
                    - ( clustersImportanceDependingOn(clustersQty, clustered, nonClustered) )
                    + ( clustersWeight * clusterWeightRatioDependingOn(
                            containsFirstChar, 
                            firstCharsMatchInVariantAndPattern) ) 
                    - ( clustersQty * 5.4 ) 
                    + ( missedRatio(missed) )
                    + (( variantText.length() - clustered ) * 0.8 ) 
                    + ( sortingSteps * 5.7 ) );            
            if ( ( clustered < 2 ) && ( sortingSteps > 0 ) ) {
                variantWeight = variantWeight * 1.8;
            }
            String positionsS = stream(positions).mapToObj(position -> String.valueOf(position)).collect(joining(" "));
            System.out.println(variantText + ", positions: " + positionsS);
            System.out.println(String.format("   %-15s %s", "clusters", clustersQty));
            System.out.println(String.format("   %-15s %s", "clustered", clustered));
            System.out.println(String.format("   %-15s %s", "clusters weight", clustersWeight));
            System.out.println(String.format("   %-15s %s", "non clustered", nonClustered));
            System.out.println(String.format("   %-15s %s", "missed", missed));
            System.out.println(String.format("   %-15s %s", "sort steps", sortingSteps));
            System.out.println(String.format("   %-15s %s", "total weight", variantWeight));
            if ( variantWeight < minWeight ) {
                minWeight = variantWeight;
            }
            if ( variantWeight > maxWeight ) {
                maxWeight = variantWeight;
            }
            // weight calculation ends
            newVariant = new WeightedVariant(variant, variantWeight);
            if ( newVariant.hasDisplayText() ) {
                debug("[ANALYZE] " + newVariant.text() + ":" + newVariant.displayText());
                if ( variantsByDisplay.containsKey(lower(variant.displayText())) ) {
                    prevVariant = variantsByDisplay.get(lower(newVariant.displayText()));
                    if ( newVariant.betterThan(prevVariant) ) {
                        debug("[ANALYZE] [DUPLICATE] " + newVariant.text() + " is better than: " + prevVariant.text());
                        variantsByDisplay.put(lower(newVariant.displayText()), newVariant);
                        weightedVariants.add(newVariant);
                    } 
                } else {
                    variantsByDisplay.put(lower(newVariant.displayText()), newVariant);
                    weightedVariants.add(newVariant);
                }
            } else {
                weightedVariants.add(newVariant);
            }           
        }
        
        double delta = minWeight;
        weightedVariants.forEach(adjustedVariant -> adjustedVariant.adjustWeight(delta));
        sort(weightedVariants);
        shrink(weightedVariants, 11);
        debug("[ANALYZE] weightedVariants qty: " + weightedVariants.size());        
        weightedVariants
                .stream()
                .forEach(candidate -> debug(format("%s : %s:%s", candidate.weight(), candidate.text(), candidate.displayText())));
        return new WeightedVariants(weightedVariants, isDiversitySufficient(minWeight, maxWeight));
    }

    private static double missedRatio(int missed) {
        return ( ( missed * 1.0) - 0.8 ) * 14.0;
    }

    private static boolean isDiversitySufficient(double minWeight, double maxWeight) {
        return ((maxWeight - minWeight) > (minWeight * 0.25));
    }
    
    public static void main(String[] args) {        
        doAll();
    }
    
    private static int CLUSTER_QTY_TRESHOLD = 4;
    private static double clustersImportanceDependingOn(
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
    
    private static double clusterWeightRatioDependingOn(
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
    
    private static double firstCharMatchRatio(boolean isMatch) {
        if ( isMatch ) {
            return 8.0;
        } else {
            return 0.0;
        }
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
    
    private static boolean isWordsSeparator(char c) {
        return 
                c == '.' ||
                c == ',' ||
                c == ' ' || 
                c == '_' || 
                c == '-' || 
                c == '/' || 
                c == '\\';
    }
}
