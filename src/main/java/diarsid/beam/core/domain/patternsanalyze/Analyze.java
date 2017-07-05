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
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;
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
                "beam_server_project"
        );
    }
    
    private static List<String> dailyReportsCases() {
        return arrayListOf("current_job/process/daily_reports_for_standup.txt");
    }
    
    private static List<String> readListCase() {
        return asList("Books/list_to_read.txt", "Tech/CS/Algorithms");
    }

    public static void doAll() {
        weightAnalyzeCases();

//        analyzeImportance();
    }

    private static void analyzeImportance() {
        System.out.println(clustersImportanceDependingOn(1, 9, 0));
        System.out.println(clustersImportanceDependingOn(1, 4, 3));
        System.out.println(clustersImportanceDependingOn(1, 2, 5));
        System.out.println(clustersImportanceDependingOn(2, 7, 1));
        System.out.println(clustersImportanceDependingOn(3, 9, 2));
        System.out.println(clustersImportanceDependingOn(2, 9, 2));
        System.out.println(clustersImportanceDependingOn(1, 9, 2));
        System.out.println(clustersImportanceDependingOn(1, 8, 3));
    }

    private static void weightAnalyzeCases() {
        List<String> variantsStrings = beamProjectCase();
        
        String pattern = "beaproj";
//        variantsStrings.add(pattern);
        
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
    
//    public static boolean entityIsSatisfiable(InvocationCommand command, NamedEntity entity) {
//        return weightVariant(command.originalArgument(), entity.toSingleVariant()).isPresent();
//    }
    
//    public static Optional<WeightedVariant> weightVariant(String pattern, Variant variant) {
//        AnalyzeData data = new AnalyzeData();
//        data.setVariantText(variant);
//        data.setPatternCharsAndPositions(pattern);
//
//        data.findPatternCharsPositions();
//
//        // positions counting ends, weight calculation begins...
//        String positionsS = stream(data.forwardPositions).mapToObj(position -> String.valueOf(position)).collect(joining(" "));
//        System.out.println("positions before sorting: " + positionsS);
//
//        data.countUnsortedPositions();
//        data.sortPositions();
//        data.clearClustersInfo();
//        data.findPositionsClusters();
//        if ( data.areTooMuchPositionsMissed() ) {
//            System.out.println(data.variantText + ", missed: " + data.missed + " to much, skip variant!");
//            return Optional.empty();
//        }
//        data.calculateClustersImportance();
//        data.isFirstCharMatchInVariantAndPattern(pattern);
//        data.calculateWeight();            
//        data.checkStrangeConditionOnUnsorted();
//        if ( data.isVariantTooBad() ) {
//            return Optional.empty();
//        }
//        data.logState(); 
//        data.setNewVariant(variant);
//        return Optional.of(data.newVariant);
//    }
    
    public static WeightedVariants weightVariants(String pattern, List<Variant> variants) {
        pattern = lower(pattern);
        sort(variants);        
        Map<String, WeightedVariant> variantsByDisplay = new HashMap<>();
        Map<String, Variant> variantsByText = new HashMap<>();
        List<WeightedVariant> weightedVariants = new ArrayList<>();        
        AnalyzeData analyze = getAnalyzeData();
        String lowerVariantText;
        double minWeight = MAX_VALUE;
        double maxWeight = MIN_VALUE;
        
        variantsWeighting: for (Variant variant : variants) { 
            
            lowerVariantText = lower(variant.text());
            if ( variantsByText.containsKey(lowerVariantText) ) {
                if ( variantsByText.get(lowerVariantText).equalsByLowerDisplayText(variant) ) {
                    continue variantsWeighting;
                }
            }
            variantsByText.put(lowerVariantText, variant);
            
            analyze.setVariantText(variant);
            analyze.checkIfVariantTextContainsPatternDirectly(pattern);
            analyze.setPatternCharsAndPositions(pattern);
            analyze.findPatternCharsPositions();
            analyze.logUnsortedPositions();
            analyze.countUnsortedPositions();
            analyze.sortPositions();
            analyze.findPositionsClusters();
            if ( analyze.areTooMuchPositionsMissed() ) {
                analyze.clearAnalyze();
                continue variantsWeighting;
            }
            analyze.calculateClustersImportance();
            analyze.isFirstCharMatchInVariantAndPattern(pattern);
            analyze.strangeConditionOnUnsorted();
            analyze.calculateWeight();   
            analyze.logState();
            if ( analyze.isVariantTooBad() ) {
                System.out.println(analyze.variantText + " is too bad.");
                analyze.clearAnalyze();
                continue variantsWeighting;
            }
            
            if ( analyze.variantWeight < minWeight ) {
                minWeight = analyze.variantWeight;
            }
            if ( analyze.variantWeight > maxWeight ) {
                maxWeight = analyze.variantWeight;
            }
            
            analyze.setNewVariant(variant);
            if ( analyze.newVariant.hasDisplayText() ) {
                debug("[ANALYZE] " + analyze.newVariant.text() + ":" + analyze.newVariant.displayText());
                if ( variantsByDisplay.containsKey(lower(variant.displayText())) ) {
                    analyze.setPreviousVariantWithSameDisplayText(variantsByDisplay);
                    if ( analyze.isNewVariantBetterThanPrevious() ) {
                        debug("[ANALYZE] [DUPLICATE] " + analyze.newVariant.text() + " is better than: " + analyze.prevVariant.text());
                        variantsByDisplay.put(lower(analyze.newVariant.displayText()), analyze.newVariant);
                        weightedVariants.add(analyze.newVariant);
                    } 
                } else {
                    variantsByDisplay.put(lower(analyze.newVariant.displayText()), analyze.newVariant);
                    weightedVariants.add(analyze.newVariant);
                }
            } else {
                weightedVariants.add(analyze.newVariant);
            } 
            analyze.clearAnalyze();
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

    private static AnalyzeData getAnalyzeData() {
        return new AnalyzeData();
    }
}
