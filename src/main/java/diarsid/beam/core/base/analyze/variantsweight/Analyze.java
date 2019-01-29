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
import java.util.function.BiFunction;

import diarsid.beam.core.base.analyze.cache.CacheUsage;
import diarsid.beam.core.base.analyze.cache.PersistentAnalyzeCache;
import diarsid.beam.core.base.analyze.similarity.Similarity;
import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.ResponsiveDataModule;
import diarsid.support.configuration.Configuration;
import diarsid.support.objects.Pool;
import diarsid.support.objects.Pools;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Locale.US;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.analyze.cache.AnalyzeCache.PAIR_HASH_FUNCTION;
import static diarsid.beam.core.base.analyze.cache.CacheUsage.NOT_USE_CACHE;
import static diarsid.beam.core.base.analyze.cache.CacheUsage.USE_CACHE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.BASE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isVariantOkWhenAdjusted;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.events.BeamEventRuntime.requestPayloadThenAwaitForSupply;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.StringUtils.containsWordsSeparator;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class Analyze {
    
    private final int weightAlgorithmVersion = 11;
    private final PersistentAnalyzeCache<Float> cache;
    private final Float tooBadWeight;
    private final Pool<AnalyzeData> dataPool;
    private final Similarity similarity;
    
    private final int defaultWeightResultLimit;
    private boolean isWeightedResultLimitPresent;
    private int weightedResultLimit;
    
    public Analyze(Configuration configuration, Similarity similarity, Pools pools) {
        this.similarity = similarity;
        this.defaultWeightResultLimit = configuration.asInt("analyze.result.variants.limit");        
        this.tooBadWeight = 9000.0f;
        Pool<Cluster> clusterPool = pools.createPool(
                Cluster.class, 
                () -> new Cluster());        
        this.dataPool = pools.createPool(
                AnalyzeData.class, 
                () -> new AnalyzeData(clusterPool));
        
        BiFunction<String, String, Float> weightFunction = (target, pattern) -> {
            return this.weightStringInternally(pattern, target, NOT_USE_CACHE);
        }; 
        
        this.cache = new PersistentAnalyzeCache<>(
                systemInitiator(),
                weightFunction,
                PAIR_HASH_FUNCTION, 
                weightAlgorithmVersion);
        
        asyncDo(() -> {
            logFor(Analyze.class).info("requesting for data module...");
            requestPayloadThenAwaitForSupply(ResponsiveDataModule.class).ifPresent((dataModule) -> {
                logFor(Analyze.class).info("cache loading...");
                cache.initPersistenceWith(dataModule.cachedWeight());
                logFor(Analyze.class).info("cache loaded");            
            });
        });
        
        this.isWeightedResultLimitPresent = true;
        this.weightedResultLimit = defaultWeightResultLimit;        
    }
    
    public int resultsLimit() {
        return this.weightedResultLimit;
    }
    
    public boolean isResultsLimitPresent() {
        return this.isWeightedResultLimitPresent;
    }
    
    public void resultsLimitToDefault() {
        this.weightedResultLimit = this.defaultWeightResultLimit;
    }
    
    public void disableResultsLimit() {
        this.isWeightedResultLimitPresent = false;
        this.weightedResultLimit = this.defaultWeightResultLimit;
    }
    
    public void enableResultsLimit() {
        this.isWeightedResultLimitPresent = true;
        this.weightedResultLimit = this.defaultWeightResultLimit;
    }
    
    public void setResultsLimit(int newLimit) {
        this.weightedResultLimit = newLimit;
        this.isWeightedResultLimitPresent = true;
    }
    
    static void logAnalyze(AnalyzeLogType logType, String format, Object... args) {
        if ( logType.isEnabled() ) {
            if ( args.length == 0 ) {
                System.out.println(format);
            } else {
                System.out.println(format(format, args));
            }            
        }
    }
    
    public WeightedVariants weightStrings(String pattern, List<String> variants) {
        return this.weightVariants(pattern, stringsToVariants(variants));
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
    
    private boolean isGood(Float weight) {
        return weight < this.tooBadWeight;
    }
    
    private boolean isTooBad(Float weight) {
        return weight.equals(this.tooBadWeight) || weight > this.tooBadWeight;
    }
    
    public boolean isNameSatisfiable(String pattern, String name) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, name) ) {
            return this.similarity.isSimilar(name, pattern);
        } else {
            return this.isGood(weightStringInternally(pattern, name, USE_CACHE));
        }        
    }
    
    public boolean isVariantSatisfiable(String pattern, Variant variant) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, variant.text()) ) {
            return this.similarity.isSimilar(variant.text(), pattern);
        } else {
            return this.isGood(weightStringInternally(pattern, variant.text(), USE_CACHE));
        }        
    }
    
    public boolean isEntitySatisfiable(String pattern, NamedEntity entity) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, entity.name()) ) {
            return this.similarity.isSimilar(entity.name(), pattern);
        } else {
            return this.isGood(weightStringInternally(pattern, entity.name(), USE_CACHE));
        }        
    }
    
    public Optional<WeightedVariant> weightVariant(String pattern, Variant variant) {
        return this.weightVariantInternally(pattern, variant, USE_CACHE);
    }
    
    private Optional<WeightedVariant> weightVariantInternally(
            String pattern, Variant variant, CacheUsage cacheUsage) {
        Float weight = this.weightStringInternally(pattern, variant.text(), cacheUsage);
        if ( this.isGood(weight) ) {
            WeightedVariant weightedVariant = new WeightedVariant(
                    variant, 
                    variant.text().equalsIgnoreCase(pattern), 
                    weight);
            return Optional.of(weightedVariant);
        } else {
            return Optional.empty();
        }
    }
    
    private Float weightStringInternally(
            String pattern, String target, CacheUsage cacheUsage) {
        if ( cacheUsage.equals(USE_CACHE) ) {
            Float cachedWeight = this.cache.searchNullableCachedFor(target, pattern);
            if ( nonNull(cachedWeight) ) {
                logFor(Analyze.class).info(format(
                        "FOUND CACHED %s (target: %s, pattern: %s)", 
                        cachedWeight, target, pattern));
                return cachedWeight;
            }
        }
        
        AnalyzeData analyze = this.dataPool.give();
        try {
            analyze.set(pattern, target);
            if ( analyze.isVariantEqualsPattern() ) {
                analyze.complete();
                if ( cacheUsage.equals(USE_CACHE) ) {
                    this.cache.addToCache(target, pattern, (float) analyze.variantWeight);
                }
                return (float) analyze.variantWeight;
            }
            analyze.checkIfVariantTextContainsPatternDirectly();
            analyze.findPathAndTextSeparators();
            analyze.setPatternCharsAndPositions();
            analyze.findPatternCharsPositions();
            analyze.logUnsortedPositions();
            analyze.sortPositions();
            analyze.findPositionsClusters();
            if ( analyze.ifClustersPresentButWeightTooBad() ) {
                logAnalyze(BASE, "  %s is too bad.", analyze.variantText);
                if ( cacheUsage.equals(USE_CACHE) ) {
                    this.cache.addToCache(target, pattern, this.tooBadWeight);
                }
                return this.tooBadWeight;
            }
            if ( analyze.areTooMuchPositionsMissed() ) {
                if ( cacheUsage.equals(USE_CACHE) ) {
                    this.cache.addToCache(target, pattern, this.tooBadWeight);
                }
                return this.tooBadWeight;
            }
            analyze.calculateClustersImportance();
            analyze.isFirstCharMatchInVariantAndPattern(pattern);
            analyze.calculateWeight();  
            analyze.logState();
            if ( analyze.isVariantTooBad() ) {
                logAnalyze(BASE, "%s is too bad.", analyze.variantText);
                if ( cacheUsage.equals(USE_CACHE) ) {
                    this.cache.addToCache(target, pattern, this.tooBadWeight);
                }
                return this.tooBadWeight;
            }
            analyze.complete();
            
            if ( cacheUsage.equals(USE_CACHE) ) {
                this.cache.addToCache(target, pattern, (float) analyze.variantWeight);
            }
            
            return (float) analyze.variantWeight;
        } finally {
            this.dataPool.takeBack(analyze);
        }
    }
    
    public WeightedVariants weightVariants(String pattern, List<Variant> variants) {
        List<WeightedVariant> weightedVariants = this.weightVariantsList(pattern, variants);
        return new WeightedVariants(weightedVariants);
    }
    
    public List<WeightedVariant> weightVariantsList(String pattern, List<Variant> variants) {
        return this.weightVariantsListInternally(pattern, variants, USE_CACHE);
    }
    
    private List<WeightedVariant> weightVariantsListInternally(
            String pattern, List<Variant> variants, CacheUsage cacheUsage) {
        pattern = lower(pattern);
        sort(variants);        
        Map<String, WeightedVariant> variantsByDisplay = new HashMap<>();
        Map<String, Variant> variantsByText = new HashMap<>();
        WeightedVariant duplicateByDisplayText;
        Variant duplicateByText;
        List<WeightedVariant> weightedVariants = new ArrayList<>();        
        AnalyzeData analyze = this.dataPool.give();
        String lowerVariantText;
        double minWeight = MAX_VALUE;
        double maxWeight = MIN_VALUE;
        
        try {
            variantsWeighting: for (Variant variant : variants) {             
                lowerVariantText = lower(variant.text());
                
                if ( variantsByText.containsKey(lowerVariantText) ) {
                    duplicateByText = variantsByText.get(lowerVariantText);
                    if ( duplicateByText.equalsByLowerDisplayText(variant) ||
                         duplicateByText.equalsByLowerText(variant) ) {
                        continue variantsWeighting;
                    }
                }        
                
                logAnalyze(BASE, "");
                logAnalyze(BASE, "===== ANALYZE : %s ( %s ) ===== ", variant.text(), pattern);
                variantsByText.put(lowerVariantText, variant);
                
                if ( cacheUsage.equals(USE_CACHE) ) {
                    Float cachedWeight = this.cache.searchNullableCachedFor(lowerVariantText, pattern);
                    if ( nonNull(cachedWeight) ) {
                        
                        logAnalyze(BASE, format("  FOUND CACHED weight: %s ", cachedWeight));
                        
                        if ( isTooBad(cachedWeight) ) {
                            logAnalyze(BASE, "  too bad.");
                            continue variantsWeighting;
                        }
                        
                        if ( variant.hasDisplayText() ) {
                            String lowerVariantDisplayText = lower(variant.displayText());
                            if ( variantsByDisplay.containsKey(lowerVariantDisplayText) ) {
                                duplicateByDisplayText = variantsByDisplay.get(lowerVariantDisplayText);
                                if ( cachedWeight < duplicateByDisplayText.weight() ) {
                                    logFor(Analyze.class).info("[DUPLICATE] " + variant.text() + " is better than: " + duplicateByDisplayText.text());
                                    WeightedVariant weightedVariant = new WeightedVariant(
                                            variant, lowerVariantText.equals(pattern), cachedWeight);
                                    variantsByDisplay.put(lowerVariantDisplayText, weightedVariant);
                                    weightedVariants.add(weightedVariant);
                                } 
                            } else {
                                WeightedVariant weightedVariant = new WeightedVariant(
                                        variant, lowerVariantText.equals(pattern), cachedWeight);
                                variantsByDisplay.put(lowerVariantDisplayText, weightedVariant);
                                weightedVariants.add(weightedVariant);                  
                            }
                        } else {
                            WeightedVariant weightedVariant = new WeightedVariant(
                                    variant, lowerVariantText.equals(pattern), cachedWeight);
                            weightedVariants.add(weightedVariant);                      
                        }
                        
                        continue variantsWeighting;
                    }
                }        

                analyze.set(pattern, variant);
                if ( analyze.isVariantNotEqualsPattern() ) {
                    analyze.checkIfVariantTextContainsPatternDirectly();
                    analyze.findPathAndTextSeparators();
                    analyze.setPatternCharsAndPositions();
                    analyze.findPatternCharsPositions();
                    analyze.logUnsortedPositions();
                    analyze.sortPositions();
                    analyze.findPositionsClusters();
                    if ( analyze.ifClustersPresentButWeightTooBad() ) {
                        logAnalyze(BASE, "  %s is too bad.", analyze.variantText);
                        
                        if ( cacheUsage.equals(USE_CACHE) ) {
                            this.cache.addToCache(variant.text(), pattern, this.tooBadWeight);
                        }
                        
                        analyze.clearForReuse();
                        continue variantsWeighting;
                    }
                    if ( analyze.areTooMuchPositionsMissed() ) {
                        
                        if ( cacheUsage.equals(USE_CACHE) ) {
                            this.cache.addToCache(variant.text(), pattern, this.tooBadWeight);
                        }
                        
                        analyze.clearForReuse();
                        continue variantsWeighting;
                    }
                    analyze.calculateClustersImportance();
                    analyze.isFirstCharMatchInVariantAndPattern(pattern);
                    analyze.calculateWeight();  
                    analyze.logState();
                    if ( analyze.isVariantTooBad() ) {
                        logAnalyze(BASE, "  %s is too bad.", analyze.variantText);
                        
                        if ( cacheUsage.equals(USE_CACHE) ) {
                            this.cache.addToCache(variant.text(), pattern, this.tooBadWeight);
                        }
                        
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
                if ( analyze.weightedVariant.hasDisplayText() ) {
                    logFor(Analyze.class).info(analyze.weightedVariant.text() + ":" + analyze.weightedVariant.displayText());
                    if ( variantsByDisplay.containsKey(lower(variant.displayText())) ) {
                        duplicateByDisplayText = variantsByDisplay.get(lower(analyze.weightedVariant.displayText()));
                        if ( analyze.weightedVariant.betterThan(duplicateByDisplayText) ) {
                            logFor(Analyze.class).info("[DUPLICATE] " + analyze.weightedVariant.text() + " is better than: " + duplicateByDisplayText.text());
                            variantsByDisplay.put(lower(analyze.weightedVariant.displayText()), analyze.weightedVariant);
                            weightedVariants.add(analyze.weightedVariant);
                        } 
                    } else {
                        variantsByDisplay.put(lower(analyze.weightedVariant.displayText()), analyze.weightedVariant);
                        weightedVariants.add(analyze.weightedVariant);                  
                    }
                } else {
                    weightedVariants.add(analyze.weightedVariant);                
                } 
                
                if ( cacheUsage.equals(USE_CACHE) ) {
                    cache.addToCache(variant.text(), pattern, (float) analyze.weightedVariant.weight());
                }
                
                analyze.clearForReuse();
            }
        } finally {
            this.dataPool.takeBack(analyze);
        }
        
//        double delta = minWeight;
//        weightedVariants = weightedVariants
//                .stream()
//                .peek(weightedVariant -> weightedVariant.adjustWeight(delta))
//                .filter(weightedVariant -> isVariantOkWhenAdjusted(weightedVariant))
//                .collect(toList());
        sort(weightedVariants);
        if ( this.isWeightedResultLimitPresent ) {
            shrink(weightedVariants, this.weightedResultLimit);
        }
        logFor(Analyze.class).info("weightedVariants qty: " + weightedVariants.size());        
        weightedVariants
                .stream()
                .forEach(candidate -> logFor(Analyze.class).info(format(US, "%.3f : %s:%s", candidate.weight(), candidate.text(), candidate.displayText())));
//        isDiversitySufficient(minWeight, maxWeight);
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
    
    static void adjustWeightAndSweepBad(List<WeightedVariant> weightedVariants) {
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
    
    static void adjustWeightAndSweepBad(
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
