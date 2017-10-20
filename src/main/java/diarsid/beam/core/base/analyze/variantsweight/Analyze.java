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
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isDiversitySufficient;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isVariantOk;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.lower;

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
    
    private static List<String> toolsCase() {
        return asList(
                "LostFilm", 
                "Dev/3__Tools");
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
    
    private static List<String> beamProjectCase() {
        return asList(
//                "beam_project_home",
//                "beam_project",
//                "beam_home",
//                "awesome java libs",
//                "git>beam",
//                "beam_project/src",
//                "beam netpro",
//                "abe_netpro",
//                "babel_pro",
//                "netbeans_projects", 
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
        weightAnalyzeCase();

//        analyzeImportance();
    }

    private static void analyzeImportance() {
        System.out.println(clustersImportanceDependingOn(1, 6, 0));
        System.out.println(clustersImportanceDependingOn(2, 6, 0));
        System.out.println(clustersImportanceDependingOn(3, 6, 0));
        System.out.println(clustersImportanceDependingOn(4, 6, 0));
//        System.out.println(clustersImportanceDependingOn(1, 2, 5));
//        System.out.println(clustersImportanceDependingOn(2, 7, 1));
//        System.out.println(clustersImportanceDependingOn(3, 9, 2));
//        System.out.println(clustersImportanceDependingOn(2, 9, 2));
//        System.out.println(clustersImportanceDependingOn(1, 9, 2));
//        System.out.println(clustersImportanceDependingOn(1, 8, 3));
    }
    
    private static void weightAnalyzeCase() {
        Variant variant = new Variant("Programs", 0);
        String pattern = "diarsid";
        
        Optional<WeightedVariant> result = weightVariant(pattern, variant);
        if ( result.isPresent() ) {
            System.out.println("OK");
        } else {
            System.out.println("FAIL");
        }
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
    
    public static boolean nameIsSatisfiable(String pattern, String name) {
        return weightVariant(pattern, new Variant(name, 0)).isPresent();
    }
    
    public static boolean entityIsSatisfiable(String pattern, NamedEntity entity) {
        return weightVariant(pattern, entity.toSingleVariant()).isPresent();
    }
    
    public static Optional<WeightedVariant> weightVariant(String pattern, Variant variant) {
        AnalyzeData analyze = new AnalyzeData();
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
            return Optional.empty();
        }
        analyze.calculateClustersImportance();
        analyze.isFirstCharMatchInVariantAndPattern(pattern);
        analyze.strangeConditionOnUnsorted();
        analyze.calculateWeight();   
        analyze.logState();
        if ( analyze.isVariantTooBad() ) {
            System.out.println(analyze.variantText + " is too bad.");
            analyze.clearAnalyze();
            return Optional.empty();
        }
        analyze.setNewVariant(variant);
        Optional<WeightedVariant> weightedVariant = Optional.of(analyze.newVariant);
        analyze.clearAnalyze();
        return weightedVariant;
    }
    
    public static WeightedVariants weightVariants(String pattern, List<Variant> variants) {
        pattern = lower(pattern);
        sort(variants);        
        Map<String, WeightedVariant> variantsByDisplay = new HashMap<>();
        Map<String, Variant> variantsByText = new HashMap<>();
        List<WeightedVariant> weightedVariants = new ArrayList<>();        
        AnalyzeData analyze = new AnalyzeData();
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
            System.out.println(" ==== ANALYZE : " + variant.text() + " ==== ");
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
}
