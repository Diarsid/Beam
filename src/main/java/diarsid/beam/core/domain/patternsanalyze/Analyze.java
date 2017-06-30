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
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.isDiversitySufficient;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.isVariantOk;

/**
 *
 * @author Diarsid
 */
public class Analyze {
        
    private Analyze() {        
    }    
    
    public static void main(String[] args) {        
        doAll();
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
    
    private static List<String> dailyReportsCases() {
        return asList("current_job/process/daily_reports_for_standup.txt");
    }
    
    private static List<String> readListCase() {
        return asList("Books/list_to_read.txt", "Tech/CS/Algorithms");
    }

    public static void doAll() {
        weightAnalyzeCases();

//        analyzeImportance();
    }

    private static void analyzeImportance() {
        System.out.println(clustersImportanceDependingOn(1, 4, 3));
        System.out.println(clustersImportanceDependingOn(1, 2, 5));
        System.out.println(clustersImportanceDependingOn(2, 7, 1));
        System.out.println(clustersImportanceDependingOn(3, 9, 2));
        System.out.println(clustersImportanceDependingOn(2, 9, 2));
        System.out.println(clustersImportanceDependingOn(1, 9, 2));
        System.out.println(clustersImportanceDependingOn(1, 8, 3));
    }

    private static void weightAnalyzeCases() {
        List<String> variantsStrings = dailyReportsCases();
        
        String pattern = "dlayreport";
        
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
    
//    private static boolean patternAdvancedSearch(String pattern, String analyzed) {
//        int patternLength = pattern.length();
//        List<Integer> positions = new ArrayList<>();
//        char currentChar;
//        int currentCharPosition;
//        int maxClusterLength = Integer.MIN_VALUE;
//        for (int currentCharIndex = 0; currentCharIndex < patternLength; currentCharIndex++) {
//            currentChar = pattern.charAt(currentCharIndex);
//            currentCharPosition = analyzed.indexOf(currentChar);
//            positions.add(currentCharPosition);
//            while ( currentCharPosition > 0 ) {
//                currentCharPosition = analyzed.indexOf(currentChar, currentCharPosition + 1);
//                if ( currentCharPosition > 0 ) {
//                    positions.add(currentCharPosition);
//                }
//            }
//        }
//        sort(positions);
//        int previousPosition = Integer.MIN_VALUE;
//        int foundClusterLength = 0;
//        for (Integer position : positions) {
//            System.out.print(position + " ");
//            if ( position == previousPosition + 1 ) {
//                if ( foundClusterLength == 0 ) {
//                    foundClusterLength = 2;
//                } else {
//                    foundClusterLength++;
//                }                
//            } else {
//                if ( foundClusterLength > maxClusterLength ) {
//                    maxClusterLength = foundClusterLength;
//                    foundClusterLength = 0;
//                }
//            }
//            previousPosition = position;
//        }
//        if ( foundClusterLength > maxClusterLength ) {
//            maxClusterLength = foundClusterLength;
//        }
//        System.out.println("cluster length: " + maxClusterLength);
//        return maxClusterLength >= patternLength;
//    }
//    
//    private static int countWords(String variant) {
//        char[] chars = variant.toCharArray();
//        char current;
//        int wordsCount = 1;
//        boolean previousCharIsNotSeparator = true;
//        for (int currentIndex = 0; currentIndex < chars.length; currentIndex++) {
//            current = chars[currentIndex];
//            if ( isWordsSeparator(current) ) {
//                if ( currentIndex > 0 ) {
//                    if ( previousCharIsNotSeparator ) {
//                        wordsCount++;
//                        previousCharIsNotSeparator = false;
//                    }
//                } else {
//                    previousCharIsNotSeparator = false;
//                }
//            } else {
//                previousCharIsNotSeparator = true;
//            }
//        }
//        if ( isWordsSeparator(lastCharOf(variant)) ) {
//            wordsCount--;
//        }
//        return wordsCount;
//    }
//
//    private static char lastCharOf(String variant) {
//        return variant.charAt(variant.length() - 1);
//    }
    
    public static WeightedVariants weightVariants(String pattern, List<Variant> variants) {
        pattern = lower(pattern);
        sort(variants);        
        Map<String, WeightedVariant> variantsByDisplay = new HashMap<>();
        Map<String, Variant> variantsByText = new HashMap<>();
        List<WeightedVariant> weightedVariants = new ArrayList<>();        
        AnalyzeData data = new AnalyzeData();
        
        double minWeight = MAX_VALUE;
        double maxWeight = MIN_VALUE;
        
        variantsWeighting: for (Variant variant : variants) {
            data.variantText = lower(variant.text());
            if ( variantsByText.containsKey(data.variantText) ) {
                if ( variantsByText.get(data.variantText).equalsByLowerDisplayText(variant) ) {
                    continue variantsWeighting;
                }
            }
            variantsByText.put(data.variantText, variant);
            // positions counting begins...
            data.patternChars = pattern.toCharArray();
            data.positions = new int[data.patternChars.length];

            for (int currentCharIndex = 0; currentCharIndex < data.patternChars.length; currentCharIndex++) {
                data.currentCharIs(currentCharIndex);
                if ( data.isCurrentCharAlreadyVisited() ) {
                    data.setCurrentCharPositionNextFoundAfterLastVisitedOne();
                } else {
                    data.setCurrentCharPosition();                
                }
                if ( data.currentCharFound() ) {
                    data.addCurrentCharToVisited();
                }
                if ( data.currentCharIndexInRange(currentCharIndex) ) {
                    data.findBetterCurrentCharPosition();  
                    //System.out.println(format("better position of '%s' in '%s' is: %s instead of: %s", currentChar, variantText, possibleBetterCurrentCharPosition, currentCharPosition));
                    if ( data.isBetterCharPositionFound() ) {
                        if ( data.isCurrentCharBetterPostionInCluster(currentCharIndex) ) {                            
                            data.replaceCurrentPositionWithBetterPosition();
                        } else {
                            data.findBetterCurrentCharPositionFromPreviousCharPosition(currentCharIndex);  
                            if ( data.isBetterCharPositionFoundAndInCluster(currentCharIndex) ) {
                                data.replaceCurrentPositionWithBetterPosition();
                            }
                        }
                    } 
                }
                data.saveCurrentCharFinalPosition(currentCharIndex);            
            }
            data.reusableVisitedChars.clear();
            
            // positions counting ends, weight calculation begins...
            String positionsS = stream(data.positions).mapToObj(position -> String.valueOf(position)).collect(joining(" "));
            System.out.println("positions before sorting: " + positionsS);
            
            data.countUnsortedPositions();
            data.sortPositions();
            data.clearClustersInfo();
            data.findClusters();
            if ( data.areTooMuchPositionsMissed() ) {
                System.out.println(data.variantText + ", missed: " + data.missed + " to much, skip variant!");
                continue variantsWeighting;
            }
            data.isFirstCharMatchInVariantAndPattern(pattern);
            data.calculateWeight();            
            data.strangeConditionOnUnsorted();
            data.logState();            
            if ( data.isVariantTooBad() ) {
                continue variantsWeighting;
            }
            if ( data.variantWeight < minWeight ) {
                minWeight = data.variantWeight;
            }
            if ( data.variantWeight > maxWeight ) {
                maxWeight = data.variantWeight;
            }
            data.setNewVariant(variant);
            if ( data.newVariant.hasDisplayText() ) {
                debug("[ANALYZE] " + data.newVariant.text() + ":" + data.newVariant.displayText());
                if ( variantsByDisplay.containsKey(lower(variant.displayText())) ) {
                    data.setPreviousVariantWithSameDisplayText(variantsByDisplay);
                    if ( data.isNewVariantBetterThanPrevious() ) {
                        debug("[ANALYZE] [DUPLICATE] " + data.newVariant.text() + " is better than: " + data.prevVariant.text());
                        variantsByDisplay.put(lower(data.newVariant.displayText()), data.newVariant);
                        weightedVariants.add(data.newVariant);
                    } 
                } else {
                    variantsByDisplay.put(lower(data.newVariant.displayText()), data.newVariant);
                    weightedVariants.add(data.newVariant);
                }
            } else {
                weightedVariants.add(data.newVariant);
            }           
        }
        
        double delta = minWeight;
        weightedVariants = weightedVariants
                .stream()
                .peek(weightedVariant -> weightedVariant.adjustWeight(delta))
                .filter(weightedVariant -> isVariantOk(weightedVariant))
                .collect(toList());
        sort(weightedVariants);
        shrink(weightedVariants, 11);
        debug("[ANALYZE] weightedVariants qty: " + weightedVariants.size());        
        weightedVariants
                .stream()
                .forEach(candidate -> debug(format("%s : %s:%s", candidate.weight(), candidate.text(), candidate.displayText())));
        return new WeightedVariants(weightedVariants, isDiversitySufficient(minWeight, maxWeight));
    }
}
