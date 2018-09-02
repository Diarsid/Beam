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

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.domain.entities.NamedEntity;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Locale.US;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.BASE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isDiversitySufficient;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isVariantOkWhenAdjusted;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.objects.Pools.giveBackToPool;
import static diarsid.beam.core.base.objects.Pools.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.StringUtils.containsWordsSeparator;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Analyze {
    
    private static final int DEFAULT_WEIGHTED_RESULT_LIMIT;
    private static boolean isWeightedResultLimitPresent;
    private static int weightedResultLimit;
    
    static {
        isWeightedResultLimitPresent = true;
        DEFAULT_WEIGHTED_RESULT_LIMIT = configuration().asInt("analyze.result.variants.limit");
        weightedResultLimit = DEFAULT_WEIGHTED_RESULT_LIMIT;
    }
    
    private Analyze() {        
    }
    
    public static int resultsLimit() {
        return weightedResultLimit;
    }
    
    public static boolean isResultsLimitPresent() {
        return isWeightedResultLimitPresent;
    }
    
    public static void resultsLimitToDefault() {
        weightedResultLimit = DEFAULT_WEIGHTED_RESULT_LIMIT;
    }
    
    public static void disableResultsLimit() {
        isWeightedResultLimitPresent = false;
        weightedResultLimit = DEFAULT_WEIGHTED_RESULT_LIMIT;
    }
    
    public static void enableResultsLimit() {
        isWeightedResultLimitPresent = true;
        weightedResultLimit = DEFAULT_WEIGHTED_RESULT_LIMIT;
    }
    
    public static void setResultsLimit(int newLimit) {
        weightedResultLimit = newLimit;
        isWeightedResultLimitPresent = true;
    }
    
    static void logAnalyze(AnalyzeLogType logType, String format, Object... args) {
        if ( logType.isEnabled() ) {
            System.out.println(format(format, args));
        }
    }
    
    public static WeightedVariants weightStrings(String pattern, List<String> variants) {
        return weightVariants(pattern, stringsToVariants(variants));
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
        AnalyzeData analyze = takeFromPool(AnalyzeData.class);
        try {
            analyze.set(pattern, variant);
            if ( analyze.isVariantEqualsPattern() ) {
                analyze.complete();
                Optional<WeightedVariant> weightedVariant = Optional.of(analyze.newVariant);
                return weightedVariant;
            }
            analyze.checkIfVariantTextContainsPatternDirectly();
            analyze.setPatternCharsAndPositions();
            analyze.analyzePatternCharsPositions();
            analyze.logUnsortedPositions();
            analyze.sortPositions();
            analyze.findPositionsClusters();
            if ( analyze.areTooMuchPositionsMissed() ) {
                return Optional.empty();
            }
            analyze.calculateClustersImportance();
            analyze.isFirstCharMatchInVariantAndPattern(pattern);
            analyze.calculateWeight();  
            analyze.logState();
            if ( analyze.isVariantTooBad() ) {
                logAnalyze(BASE, "%s is too bad.", analyze.variantText);
                return Optional.empty();
            }
            analyze.complete();
            Optional<WeightedVariant> weightedVariant = Optional.of(analyze.newVariant);
            return weightedVariant;
        } finally {
            giveBackToPool(analyze);
        }
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
        AnalyzeData analyze = takeFromPool(AnalyzeData.class);
        String lowerVariantText;
        double minWeight = MAX_VALUE;
        double maxWeight = MIN_VALUE;
        
        try {
            variantsWeighting: for (Variant variant : variants) {             
                lowerVariantText = lower(variant.text());
                if ( variantsByText.containsKey(lowerVariantText) ) {
                    if ( variantsByText.get(lowerVariantText).equalsByLowerDisplayText(variant) ) {
                        continue variantsWeighting;
                    }
                }
                logAnalyze(BASE, "");
                logAnalyze(BASE, "===== ANALYZE : %s ( %s ) ===== ", variant.text(), pattern);
                variantsByText.put(lowerVariantText, variant);

                analyze.set(pattern, variant);
                if ( analyze.isVariantNotEqualsPattern() ) {
                    analyze.checkIfVariantTextContainsPatternDirectly();
                    analyze.setPatternCharsAndPositions();
                    analyze.analyzePatternCharsPositions();
                    analyze.logUnsortedPositions();
                    analyze.sortPositions();
                    analyze.findPositionsClusters();
                    if ( analyze.areTooMuchPositionsMissed() ) {
                        analyze.clearForReuse();
                        continue variantsWeighting;
                    }
                    analyze.calculateClustersImportance();
                    analyze.isFirstCharMatchInVariantAndPattern(pattern);
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
                }

                analyze.complete();
                if ( analyze.newVariant.hasDisplayText() ) {
                    logFor(Analyze.class).info(analyze.newVariant.text() + ":" + analyze.newVariant.displayText());
                    if ( variantsByDisplay.containsKey(lower(variant.displayText())) ) {
                        analyze.setPreviousVariantWithSameDisplayText(variantsByDisplay);
                        if ( analyze.isNewVariantBetterThanPrevious() ) {
                            logFor(Analyze.class).info("[DUPLICATE] " + analyze.newVariant.text() + " is better than: " + analyze.prevVariant.text());
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
        } finally {
            giveBackToPool(analyze);
        }
        
//        double delta = minWeight;
//        weightedVariants = weightedVariants
//                .stream()
//                .peek(weightedVariant -> weightedVariant.adjustWeight(delta))
//                .filter(weightedVariant -> isVariantOkWhenAdjusted(weightedVariant))
//                .collect(toList());
        sort(weightedVariants);
        if ( isWeightedResultLimitPresent ) {
            shrink(weightedVariants, weightedResultLimit);
        }
        logFor(Analyze.class).info("weightedVariants qty: " + weightedVariants.size());        
        weightedVariants
                .stream()
                .forEach(candidate -> logFor(Analyze.class).info(format(US, "%.3f : %s:%s", candidate.weight(), candidate.text(), candidate.displayText())));
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
