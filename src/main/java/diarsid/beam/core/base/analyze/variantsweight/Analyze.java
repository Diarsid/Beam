/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.domain.entities.NamedEntity;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.BASE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isDiversitySufficient;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isVariantOkWhenAdjusted;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.objects.Cache.giveBackToCache;
import static diarsid.beam.core.base.objects.Cache.takeFromCache;
import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.StringUtils.containsWordsSeparator;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Analyze {
    
    private Analyze() {        
    }
    
    static void logAnalyze(AnalyzeLogType logType, String format, Object... args) {
        if ( logType.isEnabled() ) {
            System.out.println(format(format, args));
        }
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
    
    private static List<String> facebookCase2() {
        return asList(                
                "c:/books/library/common/author/book.fb2",
                "facebook");
    }
    
    private static List<String> commonBooksCase() {
        return asList(                
                "Books/Common/Tolkien_J.R.R",
                "Books/Common");
    }
    
    private static List<String> tmmCase() {
        return asList(                
                "Domain/ТММ/Functional_Design/Connote_entity_simplified_structure.txt"
        );        
    }
    
    private static List<String> diarsidProjectsCase() {
        return asList(
                "projects/diarsid",
                "projects/diarsid/netbeans"
        );
    }
    
    private static List<String> ukrPostApiCase() {
        return asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
    }
    
    private static List<String> ukrPostCase() {
        return asList(            
                "Projects/UkrPoshta");
    }
    
    private static List<String> priceApiCase() {
        return asList(  
                "images");
    }
    
    private static List<String> javaSpecCase() {
        return asList(                
//                "Projects/UkrPoshta/UkrPostAPI",
                "Tech/langs/Java/Specifications");
    }
    
    private static List<String> netBeansCase() {
        return asList(                
//                "Projects/Diarsid/NetBeans",
//                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans/Beam",
                "Projects/Diarsid/NetBeans/Research.Java");
    }
    
    private static List<String> visualCase() {
        return asList(                
                "JDK_public/lib/visualvm/platform/up",
                "JDK_public/bin/jvisualvm.exe");
    }
            
    private static List<String> dailyReportsCases() {
        return arrayListOf("current_job/process/daily_reports_for_standup.txt");
    }
    
    private static List<String> readListCase() {
        return asList("Books/list_to_read.txt", "Tech/CS/Algorithms");
    }

    public static void doAll() {
//        weightAnalyzeCase();

        analyzeImportance();
    }

    private static void analyzeImportance() {
        int clustersLimit = 3;
        int clusteredLimit = 6;
        int nonClusteredLimit = 4;
        
        for (int clustersQty = 1; clustersQty <= clustersLimit; clustersQty++) {
            System.out.println("clusters: " + clustersQty);
            for (int clustered = 2; clustered <= clusteredLimit; clustered++) {
                if ( clustered >= clustersQty * 2 ) {
                    System.out.println("  clustered: " + clustered);System.out.println("    cQ cL nC");
                    for (int nonClustered = 0; nonClustered <= nonClusteredLimit; nonClustered++) {                    
                        analyze(clustersQty, clustered, nonClustered);
                    }
                }                
            }
        }
        
//        analyze(1, 2, 1);
//        analyze(2, 6, 6);
//        analyze(3, 6, 0);
//        analyze(4, 6, 0);
//        analyze(1, 2, 5));
//        analyze(2, 7, 1));
//        analyze(3, 9, 2));
//        analyze(2, 9, 2));
//        analyze(1, 9, 2));
//        analyze(1, 8, 3));
    }
    
    static void analyze(int clustersQty, int clustered, int nClustered) {
        System.out.println(format("    %s  %s  %s  -  i: %s", 
                clustersQty, clustered, nClustered, clustersImportanceDependingOn(clustersQty, clustered, nClustered)));
    }
    
    private static void weightAnalyzeCase() {
        weightStrings("games", priceApiCase());
    }

    private static void weightAnalyzeCases() {
        List<String> variantsStrings = diarsidProjectsCase();
        
        String pattern = "diarsidprojecs";
//        variantsStrings.add(pattern);
        
        System.out.println("variants: " + variantsStrings.size());
        WeightedVariants variants = weightStrings(pattern, variantsStrings);
        AtomicInteger printed = new AtomicInteger(0);
        while ( variants.next() ) {            
            if ( variants.currentIsMuchBetterThanNext() ) {
                System.out.println(variants.current().text() + " is much better than next: " + variants.current().weight());
                printed.incrementAndGet();
            } else {
                System.out.println("next candidates are similar: ");                
                variants.nextSimilarVariants()
                        .stream()
                        .forEach(candidate -> {
                            System.out.println("  - " + candidate.text() + " : " + candidate.weight());
                            printed.incrementAndGet();
                        });
            }
        }
        System.out.println("printed: " + printed.get());
    }
    
    private static boolean canBeEvaluatedByStrictSimilarity(String pattern, String target) {
        if ( containsWordsSeparator(target) ) {
            return false;
        }
        if ( pattern.length() == target.length() ) {
            return pattern.length() < 10;
        } else {
            int min = min(pattern.length(), target.length());
            if ( min > 9 ) {
                return false;
            } else {
                int diff = absDiff(pattern.length(), target.length());
                if ( diff > (min / 3) ) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }
    
    public static boolean isNameSatisfiable(String pattern, String name) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, name) ) {
            return isSimilar(name, pattern);
        } else {
            return weightVariant(pattern, new Variant(name, 0)).isPresent();
        }        
    }
    
    public static boolean isVariantSatisfiable(String pattern, Variant variant) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, variant.text()) ) {
            return isSimilar(variant.text(), pattern);
        } else {
            return weightVariant(pattern, variant).isPresent();
        }        
    }
    
    public static boolean isEntitySatisfiable(String pattern, NamedEntity entity) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, entity.name()) ) {
            return isSimilar(entity.name(), pattern);
        } else {
            return weightVariant(pattern, entity.toSingleVariant()).isPresent();
        }        
    }
    
    public static Optional<WeightedVariant> weightVariant(String pattern, Variant variant) {
        AnalyzeData analyze = takeFromCache(AnalyzeData.class);
        analyze.setVariantText(variant);
        analyze.checkIfVariantTextContainsPatternDirectly(pattern);
        analyze.setPatternCharsAndPositions(pattern);
        analyze.analyzePatternCharsPositions();
        analyze.logUnsortedPositions();
        analyze.sortPositions();
        analyze.findPositionsClusters();
        if ( analyze.areTooMuchPositionsMissed() ) {
            giveBackToCache(analyze);
            return Optional.empty();
        }
        analyze.calculateClustersImportance();
        analyze.isFirstCharMatchInVariantAndPattern(pattern);
        analyze.calculateWeight();  
        analyze.logState();
        if ( analyze.isVariantTooBad() ) {
            System.out.println(analyze.variantText + " is too bad.");
            giveBackToCache(analyze);
            return Optional.empty();
        }
        analyze.setNewVariant(variant);
        Optional<WeightedVariant> weightedVariant = Optional.of(analyze.newVariant);
        giveBackToCache(analyze);
        return weightedVariant;
    }
    
    public static WeightedVariants weightVariants(String pattern, List<Variant> variants) {
        List<WeightedVariant> weightedVariants = weightVariantsList(pattern, variants);
        return new WeightedVariants(weightedVariants);
    }
    
    public static List<WeightedVariant> weightVariantsList(String pattern, List<Variant> variants) {
                pattern = lower(pattern);
        sort(variants);        
        Map<String, WeightedVariant> variantsByDisplay = new HashMap<>();
        Map<String, Variant> variantsByText = new HashMap<>();
        List<WeightedVariant> weightedVariants = new ArrayList<>();        
        AnalyzeData analyze = takeFromCache(AnalyzeData.class);
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
            System.out.println();
            logAnalyze(BASE, "===== ANALYZE : %s ( %s ) ===== ", variant.text(), pattern);
            variantsByText.put(lowerVariantText, variant);
            
            analyze.setVariantText(variant);
            analyze.checkIfVariantTextContainsPatternDirectly(pattern);
            analyze.setPatternCharsAndPositions(pattern);
            analyze.analyzePatternCharsPositions();
            analyze.logUnsortedPositions();
//            analyze.countUnsortedPositions();
            analyze.sortPositions();
            analyze.findPositionsClusters();
            if ( analyze.areTooMuchPositionsMissed() ) {
                analyze.clearForReuse();
                continue variantsWeighting;
            }
            analyze.calculateClustersImportance();
            analyze.isFirstCharMatchInVariantAndPattern(pattern);
//            analyze.strangeConditionOnUnsorted();
            analyze.calculateWeight();  
            analyze.logState();
            if ( analyze.isVariantTooBad() ) {
                logAnalyze(BASE, "  %s is too bad.", analyze.variantText);
                analyze.clearForReuse();
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
            analyze.clearForReuse();
        }
        giveBackToCache(analyze);
        
        double delta = minWeight;
//        weightedVariants = weightedVariants
//                .stream()
//                .peek(weightedVariant -> weightedVariant.adjustWeight(delta))
//                .filter(weightedVariant -> isVariantOkWhenAdjusted(weightedVariant))
//                .collect(toList());
        sort(weightedVariants);
        shrink(weightedVariants, 11);
        debug("[ANALYZE] weightedVariants qty: " + weightedVariants.size());        
        weightedVariants
                .stream()
                .forEach(candidate -> debug(format("%s : %s:%s", candidate.weight(), candidate.text(), candidate.displayText())));
        isDiversitySufficient(minWeight, maxWeight);
        return weightedVariants;
    }
    
    private static double minWeightFromVariants(List<WeightedVariant> weightedVariants) {
        if ( weightedVariants.isEmpty() ) {
            return 0.0;
        }
        
        double minWeight = MAX_VALUE;
        double varWeight;
        for (WeightedVariant variant : weightedVariants) {
            varWeight = variant.weight();
            if ( varWeight <= minWeight ) {
                minWeight = varWeight;
            }
        }
        
        return minWeight;
    }
    
    public static void adjustWeightAndSweepBad(List<WeightedVariant> weightedVariants) {
        if ( weightedVariants.isEmpty() ) {
            return;
        }
        
        double minWeight = minWeightFromVariants(weightedVariants);        
        WeightedVariant variant;
        for (int i = 0; i < weightedVariants.size(); i++) {
            variant = weightedVariants.get(i);
            variant.adjustWeight(minWeight);
            if ( ! isVariantOkWhenAdjusted(variant) ) {
                weightedVariants.remove(i);
            }
        }
    }
    
    public static void adjustWeightAndSweepBad(
            List<WeightedVariant> weightedVariants, double minWeight) {
        if ( weightedVariants.isEmpty() ) {
            return;
        }
        
        WeightedVariant variant;
        for (int i = 0; i < weightedVariants.size(); i++) {
            variant = weightedVariants.get(i);
            variant.adjustWeight(minWeight);
            if ( ! isVariantOkWhenAdjusted(variant) ) {
                weightedVariants.remove(i);
            }
        }
    }
}
