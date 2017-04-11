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

import static java.lang.Double.MAX_VALUE;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.removeWildcards;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;

/**
 *
 * @author Diarsid
 */
public class Analyze {
    
    private Analyze() {        
    }
    
    public static WeightedVariants analyze(String pattern, List<String> variants) {
        if ( hasWildcard(pattern) ) {
            WeightedVariants result = analyzeByPatternParts(splitByWildcard(pattern), variants);
            if ( result.hasAcceptableDiversity() ) {
                return result;
            } else {
                return analyzeCharByChar(removeWildcards(pattern), variants);
            }
        } else {
            return analyzeCharByChar(pattern, variants);
        }
    }
    
    public static void main(String[] args) {
        String pattern = "nebeprjo";
        
        List<String> variantsStrings = asList(
                "beam_project_home",
                "beam_project",
                "beam_project/src",
                "beam netpro",
                "abe_netpro",
                "babel_pro",
                "netbeans_projects", 
                "beam_server_project"
                );
        
        WeightedVariants variants = analyzeCharByChar(pattern, variantsStrings);
        while ( variants.hasNext() ) {            
            if ( variants.isCurrentMuchBetterThanNext() ) {
                System.out.println(variants.current().text() + " is much better than next: " + variants.current().weight());
                variants.toNext();
            } else {
                List<WeightedVariant> similar = variants.allNextSimilar();
                System.out.println("next candidates are similar: ");
                similar.stream().forEach(candidate -> System.out.println("  - " + candidate.text() + " : " + candidate.weight()));
            }
        }
    }
    
    private static WeightedVariants analyzeByPatternParts(
            List<String> pattern, List<String> variants) {
        
    }
    
    private static WeightedVariants analyzeCharByChar(String pattern, List<String> variantStrings) {
        pattern = lower(pattern);
        Map<Character, Integer> reusableVisitedChars = new HashMap<>();
        List<WeightedVariant> variants = new ArrayList<>();
        String variantText;
        
        int[] positions;
        double variantWeight;
        char[] patternChars;
        int currentCharPosition;
        int previousCharPosition;
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
        
        for (int variantIndex = 0; variantIndex < variantStrings.size(); variantIndex++) {
            variantText = lower(variantStrings.get(variantIndex));
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
                    //variantWeight = variantWeight - 8;
                    containsFirstChar = true;
                }
                if ( i < positions.length - 1 ) { 
                    nextPosition = positions[i + 1];
                    if ( currentPosition == nextPosition - 1 ) {                    
                        if ( clusterContinuation ) {                        
                            clustered++;
                            clustersWeight++;
                            currentClusterLength++;
                        } else {                        
                            clustered++;
                            clustersQty++;
                            clusterContinuation = true;
                            currentClusterLength = 1;
                            clustersWeight = clustersWeight + currentPosition;                        
                            if ( currentPosition > 0 ) {
                                if ( isSubwordBeginning(variantText.charAt(currentPosition - 1)) ) {
                                    variantWeight = variantWeight - 4;
                                }
                            }
                        }
                    } else {
                        if ( clusterContinuation ) {
                            clustered++;
                            clustersWeight++;
                            clusterContinuation = false;
                        } else {
                            nonClustered++;
                        }
                    }
                } else {
                    if ( isSubwordBeginning(variantText.charAt(currentPosition - 1)) ) {
                        variantWeight = variantWeight - 3;
                    }
                    if ( clusterContinuation ) {
                        clustered++;
                        clustersWeight++;
                    } else {
                        nonClustered++;
                    }
                }
            }
            nonClustered = nonClustered + missed;
            firstCharsMatchInVariantAndPattern = ( pattern.charAt(0) == variantText.charAt(0) );
            variantWeight = variantWeight + (
                    ( nonClustered * 5.3 ) 
                    - ( clustered * 2.6 ) 
                    - ( firstCharMatchRatio(containsFirstChar) )
                    + ( clustersWeight * clusterWeightRatioDependingOn(
                            containsFirstChar, 
                            firstCharsMatchInVariantAndPattern) ) 
                    - ( clustersQty * 5.4 ) 
                    + ( missed * 14.0 )
                    + (( variantText.length() - clustered ) * 0.8 ) 
                    + ( sortingSteps * 5.7 ) );
            String positionsS = stream(positions).mapToObj(position -> String.valueOf(position)).collect(joining(" "));
            System.out.println(variantText + ", positions: " + positionsS);
    //        System.out.println(String.format("   %-15s %s", "clusters", clustersQty));
    //        System.out.println(String.format("   %-15s %s", "clustered", clustered));
    //        System.out.println(String.format("   %-15s %s", "clusters weight", clustersWeight));
    //        System.out.println(String.format("   %-15s %s", "non clustered", nonClustered));
    //        System.out.println(String.format("   %-15s %s", "missed", missed));
    //        System.out.println(String.format("   %-15s %s", "sort steps", sortingSteps));
    //        System.out.println(String.format("   %-15s %s", "total weight", totalWeight));
            if ( variantWeight < minWeight ) {
                minWeight = variantWeight;
            }
            // weight calculation ends
            variants.add(new WeightedVariant(variantStrings.get(variantIndex), variantWeight, variantIndex));
        }
        
        if ( minWeight < 0 ) {
            double delta = abs(minWeight);
            variants.forEach(adbjustedCandidate -> adbjustedCandidate.adjustWeight(delta));
        }
        variants.stream().sorted().forEach(candidate -> System.out.println(format("%s : %s", candidate.weight(), candidate.text())));
        return new WeightedVariants(variants);
    }
    
    private static double clusterWeightRatioDependingOn(
            boolean containsFirstChar, 
            boolean firstCharsMatchInVariantAndPattern) {
        if ( containsFirstChar ) {
            if ( firstCharsMatchInVariantAndPattern ) {
                System.out.println("first chars matches!");
                return 1.1;
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
    
    public static boolean isSubwordBeginning(char c) {
        return 
                c == ' ' || 
                c == '_' || 
                c == '-' || 
                c == '/' || 
                c == '\\';
    }
}
