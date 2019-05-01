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
import java.util.function.BiFunction;

import diarsid.beam.core.base.analyze.cache.CacheUsage;
import diarsid.beam.core.base.analyze.cache.PersistentAnalyzeCache;
import diarsid.beam.core.base.analyze.similarity.Similarity;
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
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.analyze.cache.AnalyzeCache.PAIR_HASH_FUNCTION;
import static diarsid.beam.core.base.analyze.cache.CacheUsage.NOT_USE_CACHE;
import static diarsid.beam.core.base.analyze.cache.CacheUsage.USE_CACHE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.BASE;
import static diarsid.beam.core.base.events.BeamEventRuntime.requestPayloadThenAwaitForSupply;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.support.log.Logging.logFor;
import static diarsid.support.strings.StringUtils.containsWordsSeparator;
import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Analyze {
    
    private final int weightAlgorithmVersion = 15;
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
    
    static List<Variant> stringsToVariants(List<String> variantStrings) {
        AtomicInteger counter = new AtomicInteger(0);
        return variantStrings
                .stream()
                .map(string -> new Variant(string, counter.getAndIncrement()))
                .collect(toList());
    }
    
    public Variants weightStrings(String pattern, List<String> variants) {
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
    
    public Optional<Variant> weightVariant(String pattern, Variant variant) {
        return this.weightVariantInternally(pattern, variant, USE_CACHE);
    }
    
    private Optional<Variant> weightVariantInternally(
            String pattern, Variant variant, CacheUsage cacheUsage) {
        Float weight = this.weightStringInternally(pattern, variant.text(), cacheUsage);
        if ( this.isGood(weight) ) {
            variant.set(weight, variant.text().equalsIgnoreCase(pattern));
            return Optional.of(variant);
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
                if ( cacheUsage.equals(USE_CACHE) ) {
                    this.cache.addToCache(target, pattern, (float) analyze.weight);
                }
                return (float) analyze.weight;
            }
            analyze.checkIfVariantTextContainsPatternDirectly();
            analyze.findPathAndTextSeparators();
            analyze.setPatternCharsAndPositions();
            analyze.findPatternCharsPositions();
            analyze.logUnsortedPositions();
            analyze.sortPositions();
            analyze.findPositionsClusters();
            if ( analyze.ifClustersPresentButWeightTooBad() ) {
                logAnalyze(BASE, "  %s is too bad.", analyze.variant);
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
                logAnalyze(BASE, "%s is too bad.", analyze.variant);
                if ( cacheUsage.equals(USE_CACHE) ) {
                    this.cache.addToCache(target, pattern, this.tooBadWeight);
                }
                return this.tooBadWeight;
            }
            
            if ( cacheUsage.equals(USE_CACHE) ) {
                this.cache.addToCache(target, pattern, (float) analyze.weight);
            }
            
            return (float) analyze.weight;
        } finally {
            this.dataPool.takeBack(analyze);
        }
    }
    
    public Variants weightVariants(String pattern, List<Variant> variants) {
        List<Variant> weightedVariants = this.weightVariantsList(pattern, variants);
        return new Variants(weightedVariants);
    }
    
    public List<Variant> weightVariantsList(String pattern, List<Variant> variants) {
        return this.weightVariantsListInternally(pattern, variants, USE_CACHE);
    }
    
    public List<Variant> weightStringsList(String pattern, List<String> strings) {
        return this.weightVariantsListInternally(pattern, stringsToVariants(strings), USE_CACHE);
    }
    
//    private List<WeightedVariant> weightVariantsListInternally(
//            String pattern, List<Variant> variants, CacheUsage cacheUsage) {
//        pattern = lower(pattern);
//        sort(variants);        
//        Map<String, WeightedVariant> variantsByDisplay = new HashMap<>();
//        Map<String, Variant> variantsByText = new HashMap<>();
//        WeightedVariant duplicateByDisplayText;
//        WeightedVariant currentWeightedVariant;
//        Variant duplicateByText;
//        List<WeightedVariant> weightedVariants = new ArrayList<>();        
//        AnalyzeData analyze = this.dataPool.give();
//        String lowerVariantText;
//        double minWeight = MAX_VALUE;
//        double maxWeight = MIN_VALUE;
//        
//        try {
//            variantsWeighting: for (Variant variant : variants) {             
//                lowerVariantText = lower(variant.text());
//                
//                if ( variantsByText.containsKey(lowerVariantText) ) {
//                    duplicateByText = variantsByText.get(lowerVariantText);
//                    if ( duplicateByText.equalsByLowerName(variant) ||
//                         duplicateByText.equalsByLowerText(variant) ) {
//                        continue variantsWeighting;
//                    }
//                }        
//                
//                logAnalyze(BASE, "");
//                logAnalyze(BASE, "===== ANALYZE : %s ( %s ) ===== ", variant.text(), pattern);
//                variantsByText.put(lowerVariantText, variant);
//                
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    Float cachedWeight = this.cache.searchNullableCachedFor(lowerVariantText, pattern);
//                    if ( nonNull(cachedWeight) ) {
//                        
//                        logAnalyze(BASE, format("  FOUND CACHED weight: %s ", cachedWeight));
//                        
//                        if ( isTooBad(cachedWeight) ) {
//                            logAnalyze(BASE, "  too bad.");
//                            continue variantsWeighting;
//                        }
//                        
//                        if ( variant.doesHaveName() ) {
//                            String lowerVariantDisplayText = lower(variant.name());
//                            if ( variantsByDisplay.containsKey(lowerVariantDisplayText) ) {
//                                duplicateByDisplayText = variantsByDisplay.get(lowerVariantDisplayText);
//                                if ( cachedWeight < duplicateByDisplayText.weight() ) {
//                                    logFor(Analyze.class).info("[DUPLICATE] " + variant.text() + " is better than: " + duplicateByDisplayText.text());
//                                    WeightedVariant weightedVariant = new WeightedVariant(
//                                            variant, lowerVariantText.equals(pattern), cachedWeight);
//                                    variantsByDisplay.put(lowerVariantDisplayText, weightedVariant);
//                                    weightedVariants.add(weightedVariant);
//                                } 
//                            } else {
//                                WeightedVariant weightedVariant = new WeightedVariant(
//                                        variant, lowerVariantText.equals(pattern), cachedWeight);
//                                variantsByDisplay.put(lowerVariantDisplayText, weightedVariant);
//                                weightedVariants.add(weightedVariant);                  
//                            }
//                        } else {
//                            WeightedVariant weightedVariant = new WeightedVariant(
//                                    variant, lowerVariantText.equals(pattern), cachedWeight);
//                            weightedVariants.add(weightedVariant);                      
//                        }
//                        
//                        continue variantsWeighting;
//                    }
//                }        
//
//                analyze.set(pattern, variant.text());
//                if ( analyze.isVariantNotEqualsPattern() ) {
//                    analyze.checkIfVariantTextContainsPatternDirectly();
//                    analyze.findPathAndTextSeparators();
//                    analyze.setPatternCharsAndPositions();
//                    analyze.findPatternCharsPositions();
//                    analyze.logUnsortedPositions();
//                    analyze.sortPositions();
//                    analyze.findPositionsClusters();
//                    if ( analyze.ifClustersPresentButWeightTooBad() ) {
//                        logAnalyze(BASE, "  %s is too bad.", analyze.variant);
//                        
//                        if ( cacheUsage.equals(USE_CACHE) ) {
//                            this.cache.addToCache(variant.text(), pattern, this.tooBadWeight);
//                        }
//                        
//                        analyze.clearForReuse();
//                        continue variantsWeighting;
//                    }
//                    if ( analyze.areTooMuchPositionsMissed() ) {
//                        
//                        if ( cacheUsage.equals(USE_CACHE) ) {
//                            this.cache.addToCache(variant.text(), pattern, this.tooBadWeight);
//                        }
//                        
//                        analyze.clearForReuse();
//                        continue variantsWeighting;
//                    }
//                    analyze.calculateClustersImportance();
//                    analyze.isFirstCharMatchInVariantAndPattern(pattern);
//                    analyze.calculateWeight();  
//                    analyze.logState();
//                    if ( analyze.isVariantTooBad() ) {
//                        logAnalyze(BASE, "  %s is too bad.", analyze.variant);
//                        
//                        if ( cacheUsage.equals(USE_CACHE) ) {
//                            this.cache.addToCache(variant.text(), pattern, this.tooBadWeight);
//                        }
//                        
//                        analyze.clearForReuse();                        
//                        continue variantsWeighting;
//                    }
//
//                    if ( analyze.weight < minWeight ) {
//                        minWeight = analyze.weight;
//                    }
//                    if ( analyze.weight > maxWeight ) {
//                        maxWeight = analyze.weight;
//                    }                
//                }
//
//                currentWeightedVariant = new WeightedVariant(
//                    variant, analyze.variantEqualsToPattern, analyze.weight);
//                if ( currentWeightedVariant.doesHaveName() ) {
//                    logFor(Analyze.class).info(currentWeightedVariant.text() + ":" + currentWeightedVariant.name());
//                    if ( variantsByDisplay.containsKey(lower(variant.name())) ) {
//                        duplicateByDisplayText = variantsByDisplay.get(lower(currentWeightedVariant.name()));
//                        if ( currentWeightedVariant.isBetterThan(duplicateByDisplayText) ) {
//                            logFor(Analyze.class).info("[DUPLICATE] " + currentWeightedVariant.text() + " is better than: " + duplicateByDisplayText.text());
//                            variantsByDisplay.put(lower(currentWeightedVariant.name()), currentWeightedVariant);
//                            weightedVariants.add(currentWeightedVariant);
//                        } 
//                    } else {
//                        variantsByDisplay.put(lower(currentWeightedVariant.name()), currentWeightedVariant);
//                        weightedVariants.add(currentWeightedVariant);                  
//                    }
//                } else {
//                    weightedVariants.add(currentWeightedVariant);                
//                } 
//                
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    cache.addToCache(variant.text(), pattern, (float) currentWeightedVariant.weight());
//                }
//                
//                analyze.clearForReuse();
//            }
//        } finally {
//            this.dataPool.takeBack(analyze);
//        }
//        
//        sort(weightedVariants);
//        if ( this.isWeightedResultLimitPresent ) {
//            shrink(weightedVariants, this.weightedResultLimit);
//        }
//        logFor(Analyze.class).info("weightedVariants qty: " + weightedVariants.size());        
//        weightedVariants
//                .stream()
//                .forEach(candidate -> logFor(Analyze.class).info(format(US, "%.3f : %s:%s", candidate.weight(), candidate.text(), candidate.name())));
//
//        return weightedVariants;
//    }
    
    private List<Variant> weightVariantsListInternally(
            String pattern, List<Variant> xstrings, CacheUsage cacheUsage) {
        pattern = lower(pattern);
        sort(xstrings);        
        Map<String, Variant> xstringsByName = new HashMap<>();
        Map<String, Variant> xstringsByText = new HashMap<>();
        Variant duplicateByName;
        Variant duplicateByText;
        Variant currentWeightedXString;
        List<Variant> weightedXStrings = new ArrayList<>();        
        AnalyzeData analyze = this.dataPool.give();
        String lowerXStringText;
        double minWeight = MAX_VALUE;
        double maxWeight = MIN_VALUE;
        
        try {
            xstringsWeighting: for (Variant xstring : xstrings) {             
                lowerXStringText = lower(xstring.text());
                
                if ( xstringsByText.containsKey(lowerXStringText) ) {
                    duplicateByText = xstringsByText.get(lowerXStringText);
                    if ( duplicateByText.equalsByLowerName(xstring) ||
                         duplicateByText.equalsByLowerText(xstring) ) {
                        continue xstringsWeighting;
                    }
                }        
                
                logAnalyze(BASE, "");
                logAnalyze(BASE, "===== ANALYZE : %s ( %s ) ===== ", xstring.text(), pattern);
                xstringsByText.put(lowerXStringText, xstring);
                
                if ( cacheUsage.equals(USE_CACHE) ) {
                    Float cachedWeight = this.cache.searchNullableCachedFor(lowerXStringText, pattern);
                    if ( nonNull(cachedWeight) ) {
                        
                        logAnalyze(BASE, format("  FOUND CACHED weight: %s ", cachedWeight));
                        
                        if ( isTooBad(cachedWeight) ) {
                            logAnalyze(BASE, "  too bad.");
                            continue xstringsWeighting;
                        }
                        
                        if ( xstring.doesHaveName()) {
                            String lowerXStringName = lower(xstring.name());
                            if ( xstringsByName.containsKey(lowerXStringName) ) {
                                duplicateByName = xstringsByName.get(lowerXStringName);
                                if ( cachedWeight < duplicateByName.weight() ) {
                                    logFor(Analyze.class).info("[DUPLICATE] " + xstring.text() + " is better than: " + duplicateByName.text());
                                    xstring.set(cachedWeight, lowerXStringText.equals(pattern));
                                    xstringsByName.put(lowerXStringName, xstring);
                                    weightedXStrings.add(xstring);
                                } 
                            } else {
                                xstring.set(cachedWeight, lowerXStringText.equals(pattern));
                                xstringsByName.put(lowerXStringName, xstring);
                                weightedXStrings.add(xstring);                            
                            }
                        } else {
                            xstring.set(cachedWeight, lowerXStringText.equals(pattern));
                            weightedXStrings.add(xstring);                      
                        }
                        
                        continue xstringsWeighting;
                    }
                }        

                analyze.set(pattern, xstring.text());
                if ( analyze.isVariantNotEqualsPattern() ) {
                    analyze.checkIfVariantTextContainsPatternDirectly();
                    analyze.findPathAndTextSeparators();
                    analyze.setPatternCharsAndPositions();
                    analyze.findPatternCharsPositions();
                    analyze.logUnsortedPositions();
                    analyze.sortPositions();
                    analyze.findPositionsClusters();
                    if ( analyze.ifClustersPresentButWeightTooBad() ) {
                        logAnalyze(BASE, "  %s is too bad.", analyze.variant);
                        
                        if ( cacheUsage.equals(USE_CACHE) ) {
                            this.cache.addToCache(xstring.text(), pattern, this.tooBadWeight);
                        }
                        
                        analyze.clearForReuse();
                        continue xstringsWeighting;
                    }
                    if ( analyze.areTooMuchPositionsMissed() ) {
                        
                        if ( cacheUsage.equals(USE_CACHE) ) {
                            this.cache.addToCache(xstring.text(), pattern, this.tooBadWeight);
                        }
                        
                        analyze.clearForReuse();
                        continue xstringsWeighting;
                    }
                    analyze.calculateClustersImportance();
                    analyze.isFirstCharMatchInVariantAndPattern(pattern);
                    analyze.calculateWeight();  
                    analyze.logState();
                    if ( analyze.isVariantTooBad() ) {
                        logAnalyze(BASE, "  %s is too bad.", analyze.variant);
                        
                        if ( cacheUsage.equals(USE_CACHE) ) {
                            this.cache.addToCache(xstring.text(), pattern, this.tooBadWeight);
                        }
                        
                        analyze.clearForReuse();                        
                        continue xstringsWeighting;
                    }

                    if ( analyze.weight < minWeight ) {
                        minWeight = analyze.weight;
                    }
                    if ( analyze.weight > maxWeight ) {
                        maxWeight = analyze.weight;
                    }                
                }

                
                currentWeightedXString = xstring.set(analyze.weight, analyze.variantEqualsToPattern);
                if ( currentWeightedXString.doesHaveName() ) {
                    logFor(Analyze.class).info(currentWeightedXString.text() + ":" + currentWeightedXString.name());
                    if ( xstringsByName.containsKey(lower(xstring.name())) ) {
                        duplicateByName = xstringsByName.get(lower(currentWeightedXString.name()));
                        if ( currentWeightedXString.isBetterThan(duplicateByName) ) {
                            logFor(Analyze.class).info("[DUPLICATE] " + currentWeightedXString.text() + " is better than: " + duplicateByName.text());
                            xstringsByName.put(lower(currentWeightedXString.name()), currentWeightedXString);
                            weightedXStrings.add(currentWeightedXString);
                        } 
                    } else {
                        xstringsByName.put(lower(currentWeightedXString.name()), currentWeightedXString);
                        weightedXStrings.add(currentWeightedXString);                  
                    }
                } else {
                    weightedXStrings.add(currentWeightedXString);                
                } 
                
                if ( cacheUsage.equals(USE_CACHE) ) {
                    cache.addToCache(xstring.text(), pattern, (float) currentWeightedXString.weight());
                }
                
                analyze.clearForReuse();
            }
        } finally {
            this.dataPool.takeBack(analyze);
        }
        
        sort(weightedXStrings);
        if ( this.isWeightedResultLimitPresent ) {
            shrink(weightedXStrings, this.weightedResultLimit);
        }
        logFor(Analyze.class).info("weightedXStrings qty: " + weightedXStrings.size());        
        weightedXStrings
                .stream()
                .forEach(candidate -> logFor(Analyze.class).info(format(US, "%.3f : %s:%s", candidate.weight(), candidate.text(), candidate.name())));

        return weightedXStrings;
    }
    
//    private static double minWeightFromVariants(List<WeightedVariant> weightedVariants) {
//        if ( weightedVariants.isEmpty() ) {
//            return 0.0;
//        }
//        
//        double minWeight = MAX_VALUE;
//        double varWeight;
//        for (WeightedVariant variant : weightedVariants) {
//            varWeight = variant.weight();
//            if ( varWeight <= minWeight ) {
//                minWeight = varWeight;
//            }
//        }
//        
//        return minWeight;
//    }
    
//    static void adjustWeightAndSweepBad(List<WeightedVariant> weightedVariants) {
//        if ( weightedVariants.isEmpty() ) {
//            return;
//        }
//        
//        double minWeight = minWeightFromVariants(weightedVariants);        
//        WeightedVariant variant;
//        for (int i = 0; i < weightedVariants.size(); i++) {
//            variant = weightedVariants.get(i);
//            variant.adjustWeight(minWeight);
//            if ( ! isVariantOkWhenAdjusted(variant) ) {
//                weightedVariants.remove(i);
//            }
//        }
//    }
    
//    static void adjustWeightAndSweepBad(
//            List<WeightedVariant> weightedVariants, double minWeight) {
//        if ( weightedVariants.isEmpty() ) {
//            return;
//        }
//        
//        WeightedVariant variant;
//        for (int i = 0; i < weightedVariants.size(); i++) {
//            variant = weightedVariants.get(i);
//            variant.adjustWeight(minWeight);
//            if ( ! isVariantOkWhenAdjusted(variant) ) {
//                weightedVariants.remove(i);
//            }
//        }
//    }
}
