/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;


import java.util.Collection;
import java.util.function.BiFunction;

import diarsid.beam.core.base.analyze.cache.CacheUsage;
import diarsid.beam.core.base.analyze.cache.PersistentAnalyzeCache;
import diarsid.beam.core.modules.ResponsiveDataModule;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.analyze.cache.AnalyzeCache.PAIR_HASH_FUNCTION;
import static diarsid.beam.core.base.analyze.cache.CacheUsage.NOT_USE_CACHE;
import static diarsid.beam.core.base.analyze.cache.CacheUsage.USE_CACHE;
import static diarsid.beam.core.base.events.BeamEventRuntime.requestPayloadThenAwaitForSupply;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.MathUtil.absDiffOneIfZero;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.removeAllSeparators;
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class Similarity {
    
    private static final int SIMILARITY_ALGORITHM_VERSION = 4;
    private static final PersistentAnalyzeCache<Boolean> CACHE;
    
    static {
        BiFunction<String, String, Boolean> similarityFunction = (target, pattern) -> {
            return isSimilarInternally(target, pattern, NOT_USE_CACHE);
        };
        
        CACHE = new PersistentAnalyzeCache<>(
                systemInitiator(),
                similarityFunction,
                PAIR_HASH_FUNCTION, 
                SIMILARITY_ALGORITHM_VERSION);
        
        asyncDo(() -> {
            logFor(Similarity.class).info("requesting for data module...");
            requestPayloadThenAwaitForSupply(ResponsiveDataModule.class).ifPresent((dataModule) -> {
                logFor(Similarity.class).info("cache loading...");
                CACHE.initPersistenceWith(dataModule.cachedSimilarity());
                logFor(Similarity.class).info("cache loaded");            
            });
        });
    }
    
    static final char CHAIN_NOT_FOUND_CHAR = '*';
    
    private static final int CHAIN_1_NOT_FOUND = -2;
    private static final int CHAIN_2_NOT_FOUND = -3;
    
    private static boolean logLevelBasicEnabled = 
            configuration().asBoolean("analyze.similarity.log.base");
    private static boolean logLevelAdvancedEnabled = 
            configuration().asBoolean("analyze.similarity.log.advanced");
    

    private Similarity() {        
    }
    
    private static int indexOfChain(String target, char chain1, char chain2) {
        return indexOfChainFrom(target, -1, chain1, chain2);
    }
    
    private static int indexOfChainFrom(
            String target, int fromExclusive, char chain1, char chain2) {
        return indexOfChainWithLoopFrom(target, fromExclusive, chain1, chain2);
    }
    
    private static int indexOfChainWithLoopFrom(
            String target, int fromExclusive, char chain1, char chain2) {
        int indexOfChain1 = target.indexOf(chain1, fromExclusive + 1);
        
        if ( indexOfChain1 < 0 ) {
            return CHAIN_1_NOT_FOUND;
        }
        
        while ( indexOfChain1 > -1 && indexOfChain1 < target.length() - 1 ) {
            if ( target.charAt(indexOfChain1 + 1) == chain2 ) {
                return indexOfChain1;
            }    
            indexOfChain1 = target.indexOf(chain1, indexOfChain1 + 1);
        }       
        return CHAIN_2_NOT_FOUND;
    }
    
    private static boolean containsWeakChain(String target, char chain1, char chain2) {
        int indexOfChain1 = target.indexOf(chain1);
        while ( indexOfChain1 > -1 && indexOfChain1 < target.length() - 2 ) {            
            if ( target.charAt(indexOfChain1 + 2) == chain2 ) {
                return true;
            } else {
                indexOfChain1 = target.indexOf(chain1, indexOfChain1 + 1);
            }
        }
        return false;
    }
    
    private static void similarityLogBreak() {
        if ( logLevelBasicEnabled ) {
            logFor(Similarity.class).info("");
        }
    }
    
    private static void similarityLog(String s, int indentLevel) {
        if ( indentLevel == 0 ) {
            similarityLog(s);
        } else {
            if ( logLevelBasicEnabled && logLevelAdvancedEnabled ) {
                logFor(Similarity.class).info(indentOf(indentLevel) + s);
            } 
        }               
    }
    
    private static void similarityLog(String s) {
        if ( logLevelBasicEnabled ) {
            logFor(Similarity.class).info(s);
        }        
    }
    
    private static String indentOf(int level) {
        switch ( level ) {
            case 0  : return "";
            case 1  : return "  ";
            case 2  : return "    ";
            case 3  : return "      ";
            default : return "        ";
        }
    }
    
    private static int calculateSimilarityPercent(String target, String pattern) {        
        if ( target.isEmpty() 
                || pattern.isEmpty() 
                || pattern.length() == 1 
                || target.length() == 1) {
            return 0;
        }
        if ( (pattern.length() - target.length()) > (pattern.length() / 3) ) {
            return 0;
        }
        
        similarityLogBreak();        
        similarityLog("pattern : " + pattern);
        similarityLog("target  : " + target);
        if ( target.equalsIgnoreCase(pattern) ) {
            similarityLog("equal", 1);
            return 100;
        }
        
        target = lower(target);
        pattern = lower(pattern);
        
        char chain1;
        char chain2;
        char reverseChain1;
        char reverseChain2;
        boolean chainAreDuplicate;
        int chainIndexTarget;
        int reverseChainIndexTarget;  
        int chainIndexTargetPrev = -1;
        int similarityPercentSum = 0;
        int similarityPercent = 100 / (pattern.length() - 1);
        int patternCharPercent = 100 / pattern.length();
        int similarityResult;
        int missingPatternCharsPercentSum = 0;
        int previousChainsNotFoundQty = 0;
        boolean previousChainFoundAsReverse = false;
        boolean previousChainFoundAsWeak = false;
        char weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
        
        int consistencyPercent;
        int inconsistencySum = 0;
        
        int maxInconsistency = pattern.length() - 1;
        if ( pattern.length() == 3 ) {
            maxInconsistency = 3;
        }
        
        if ( pattern.length() > target.length() ) {
            similarityLog("pattern is longer than target!", 1);
        }
        similarityLog(format("max inconsistency : %s", maxInconsistency), 1);        
        
        for (int chainIndexPattern = 0; chainIndexPattern < pattern.length() - 1; chainIndexPattern++) {
            chain1 = pattern.charAt(chainIndexPattern);
            chain2 = pattern.charAt(chainIndexPattern + 1);
            chainAreDuplicate = chain1 == chain2;
            similarityLog(format("chain '%s%s'", chain1, chain2), 1);
            if ( chainAreDuplicate ) {
                similarityLog("duplicated chars", 2);
            }
            
            chainIndexTarget = indexOfChain(target, chain1, chain2);
            if ( chainIndexTarget > -1 && chainIndexTarget == chainIndexTargetPrev ) {
                similarityLog("found, equal to previous, ignore and continue", 2);
                chainIndexTarget = indexOfChainFrom(target, chainIndexTarget, chain1, chain2);
            }
            
            if ( chainIndexTarget == CHAIN_1_NOT_FOUND ) {
                missingPatternCharsPercentSum = missingPatternCharsPercentSum + patternCharPercent;
                similarityLog(format("char '%s' not found", chain1), 2);
                if ( missingPatternCharsPercentSum > 50 ) {
                    similarityLog("too much chars missed!");
                    return 0;
                }
            }
            
            if ( chainIndexTarget > -1 ) {
                similarityLog(format("found +%s%%", similarityPercent), 2);
                similarityPercentSum = similarityPercentSum + similarityPercent;                
                if ( previousChainFoundAsWeak ) {
                    if ( weakChainLastChar == chain1 ) {
                        similarityLog(format("previous weak chain is consistent +%s%%", similarityPercent/2), 2);
                        similarityPercentSum = similarityPercentSum + similarityPercent/2;
                    }
                } else {
                    if ( previousChainsNotFoundQty == 1 && similarityPercentSum > 0 ) {
                        similarityLog(format("previous not found chain found partially +%s%%", similarityPercent/2), 2);
                        similarityPercentSum = similarityPercentSum + similarityPercent/2;
                    }
                }
                if ( chainIndexTargetPrev > -1 ) {
                    if ( chainIndexTarget < chainIndexTargetPrev ) {
                        inconsistencySum++;
                    }
                }
                chainIndexTargetPrev = chainIndexTarget;
                previousChainFoundAsReverse = false;
                previousChainFoundAsWeak = false;
                previousChainsNotFoundQty = 0;
                weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
            } else {                
                similarityLog("not found directly", 2);
                if ( containsWeakChain(target, chain1, chain2) ) {
                    if ( previousChainFoundAsReverse ) {
                        similarityLog(format("found as weak chain after reverse +%s%%", similarityPercent/2), 2);
                        similarityPercentSum = similarityPercentSum + similarityPercent/2;
                    } else {
                        similarityLog(format("found as weak chain, not after reverse +%s%%", 2*similarityPercent/3), 2);
                        similarityPercentSum = similarityPercentSum + 2*similarityPercent/3;
                        if ( chainIndexPattern == (pattern.length() - 2) && 
                                chain2 == target.charAt(target.length() - 1)) {
                            similarityLog(format("weak chain is last +%s%%", similarityPercent/2), 2);
                            similarityPercentSum = similarityPercentSum + similarityPercent/2;
                        }
                    }
                    if ( previousChainsNotFoundQty > 0 ) {
                        similarityLog(format("previous chain not found but current chain is found +%s%%", similarityPercent/5), 2);
                        similarityPercentSum = similarityPercentSum + similarityPercent/5;
                    }
                    previousChainFoundAsReverse = false;
                    previousChainFoundAsWeak = true;
                    weakChainLastChar = chain2;
                    previousChainsNotFoundQty = 0;
                } else {
                    if ( chainAreDuplicate ) {
                        similarityLog("no sense for reverse check of duplicated chain", 2);
                        continue;
                    }
                    reverseChain1 = chain2;
                    reverseChain2 = chain1;
                    reverseChainIndexTarget = indexOfChain(target, reverseChain1, reverseChain2);
                    if ( reverseChainIndexTarget > -1 && chainIndexTargetPrev == reverseChainIndexTarget ) {
                        similarityLog("found reverse, equal to previous, ignore and continue", 2);
                        reverseChainIndexTarget = indexOfChainFrom(target, chainIndexTargetPrev, reverseChain1, reverseChain2);
                    }
                    if ( reverseChainIndexTarget > -1 ) {
                        similarityLog(format("found reverse +%s%%", similarityPercent), 2);
                        similarityPercentSum = similarityPercentSum + similarityPercent;   
                        if ( maxInconsistency < 5 ) {
                            inconsistencySum++;
                        } else if ( ! previousChainFoundAsReverse ) {
                            inconsistencySum++;
                        }
                        if ( previousChainFoundAsWeak ) {
                            similarityLog(format("previous chain found as weak +%s%%", similarityPercent/2), 2);
                            similarityPercentSum = similarityPercentSum + similarityPercent/2;
                        }
                        chainIndexTargetPrev = reverseChainIndexTarget;
                        previousChainFoundAsReverse = true;
                        previousChainFoundAsWeak = false;
                        weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
                        previousChainsNotFoundQty = 0;
                    } else {
                        if ( containsWeakChain(target, reverseChain1, reverseChain2) && similarityPercentSum > similarityPercent ) {
                            similarityPercentSum = similarityPercentSum + similarityPercent/3;
                            similarityLog(format("chain found as weak and reverse +%s%%", similarityPercent/3), 2);
                        } else {
                            previousChainsNotFoundQty++;
                            if ( chainIndexTargetPrev > -1 ) {
                                char prevFoundChain2 = '3';
                                try {
                                    prevFoundChain2 = target.charAt(chainIndexTargetPrev + 1);
                                } catch (Exception e) {
                                    similarityLog(format("target:%s pattern%s", target, pattern));
                                    throw e;
                                }
                                if ( prevFoundChain2 == chain1 ) {
                                    int chain2IndexTarget = target.indexOf(chain2);
                                    if ( chain2IndexTarget > -1 && chain2IndexTarget > chainIndexTarget ) {
                                        similarityLog(format("found as long-weak chain +%s%%", (2 * similarityPercent / 3)), 2);
                                        similarityPercentSum = similarityPercentSum + (2 * similarityPercent / 3);
                                        previousChainsNotFoundQty--;
                                    } else {
                                        if ( pattern.length() > 3 ) {
                                            similarityLog(format("found partially +%s%%", similarityPercent/2), 2);
                                            similarityPercentSum = similarityPercentSum + similarityPercent/2;                                            
                                        } else {
                                            similarityLog(format("found partially +%s%%", similarityPercent/3), 2);
                                            similarityPercentSum = similarityPercentSum + similarityPercent/3;      
                                        }    
                                        previousChainsNotFoundQty--;
                                    }                                    
                                } else {
                                    if ( ! previousChainFoundAsReverse ) {
                                        if ( indexOfChainFrom(target, chainIndexTargetPrev, prevFoundChain2, chain2) > -1 ) {
                                            similarityLog(format("found as combined chain '%s%s' +%s%%", prevFoundChain2, chain2, similarityPercent/2), 2);                                            
                                            similarityPercentSum = similarityPercentSum + similarityPercent/2;
                                        } else if ( containsWeakChain(target, prevFoundChain2, chain2) ) {
                                            similarityLog(format("found as combined weak chain with previous found chain +%s%%", similarityPercent/2), 2);
                                            similarityPercentSum = similarityPercentSum + similarityPercent/2;
                                        } else if ( ! previousChainFoundAsWeak ) {
                                            if ( chainIndexPattern > 0 ) {
                                                char prevNotFoundChain2 = pattern.charAt(chainIndexPattern - 1);
                                                if ( indexOfChainFrom(target, chainIndexPattern, prevNotFoundChain2, chain2) > -1 ) {
                                                    similarityLog(format("found as combined weak chain with previous not found chain +%s%%", similarityPercent/2), 2);
                                                    similarityPercentSum = similarityPercentSum + similarityPercent/2;
                                                } 
                                            }                                                                                       
                                        }                                        
                                    }
                                }
                            } else {
                                int chain1IndexTarget = target.indexOf(chain1);
                                int chain2IndexTarget = target.indexOf(chain2);
                                if ( chain1IndexTarget > -1 ) {
                                    if ( chain2IndexTarget > -1 ) {
                                        int flexiblePercent = (3 * similarityPercent / 5) / 
                                                absDiffOneIfZero(chain1IndexTarget, chain2IndexTarget) ;
                                        similarityLog(format("found 2 chars separately +%s%%", flexiblePercent), 2);
                                        similarityPercentSum = similarityPercentSum + flexiblePercent;
                                        previousChainsNotFoundQty--;
                                        if ( chain1IndexTarget > chain2IndexTarget ) {
                                            inconsistencySum++;
                                            chainIndexTargetPrev = chain2IndexTarget;
                                        } else {
                                            chainIndexTargetPrev = chain1IndexTarget;
                                        }
                                    } else {
                                        similarityLog(format("found 1 char separately +%s%%", (1 * similarityPercent / 5)), 2);
                                        similarityPercentSum = similarityPercentSum + (1 * similarityPercent / 5);
                                        previousChainsNotFoundQty--;
                                    }
                                } else {                                    
                                    if ( chain2IndexTarget > -1 ) {
                                        
                                    }
                                }
                            }
                            if ( chainIndexPattern == (pattern.length() - 2) && 
                                        chain2 == target.charAt(target.length() - 1)) {
                                similarityLog(format("last chars match +%s%%", similarityPercent/3), 2);
                                similarityPercentSum = similarityPercentSum + (similarityPercent / 3);
                                previousChainsNotFoundQty--;
                            }
                            previousChainFoundAsReverse = false;
                            previousChainFoundAsWeak = false;
                            weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
                        }                        
                    }
                }
            }
        }
        if ( target.indexOf(pattern.charAt(pattern.length() - 1)) < 0 ) {
            missingPatternCharsPercentSum = missingPatternCharsPercentSum + patternCharPercent;
            similarityLog(format("char '%s' not found at end", pattern.charAt(pattern.length() - 1)), 2);
            if ( missingPatternCharsPercentSum > 50 ) {
                similarityLog("too much chars missed!");
                return 0;
            }
        }
        if ( similarityPercentSum < 50 ) {
            similarityLog("similarity is too low!");
            return 0;
        }
        
        if ( similarityPercentSum > 0 ) {
            consistencyPercent = 100 - ( inconsistencySum * 100 / maxInconsistency );
        } else {
            consistencyPercent = 0;
        }
        int missingCharsPenaltyPercent = 0;
        if ( missingPatternCharsPercentSum > 0 ) {
            missingCharsPenaltyPercent = calculateMissingCharsPenalty(patternCharPercent, missingPatternCharsPercentSum);
        }
        similarityLog(format("consistency     : %s%%", consistencyPercent));
        similarityLog(format("chars not found : %s%% -> penalty : %s%%", missingPatternCharsPercentSum, missingCharsPenaltyPercent));
        similarityLog(format("similarity      : %s%%", similarityPercentSum));

        if ( missingPatternCharsPercentSum > 0 ) {
            similarityPercentSum = similarityPercentSum * (100 - missingCharsPenaltyPercent) / 100;
        }
        similarityResult = ( similarityPercentSum * consistencyPercent / 100 );
        return similarityResult;
    }
    
    private static int calculateMissingCharsPenalty(int patternCharPercent, int missingPatternCharsPercentSum) {
        return (missingPatternCharsPercentSum * notFoundPenalty(missingPatternCharsPercentSum / patternCharPercent)) / 100;
    }
    
    private static int notFoundPenalty(int notFoundCharsQty) {
        switch ( notFoundCharsQty ) {
            case 0 :
                return 0;
            case 1 :
                return 70;
            case 2 :
                return 100;
            case 3 : 
                return 125;
            case 4 :
                return 145;
            case 5 :
                return 160;
            case 6 : 
                return 170;
            case 7 :
                return 175;
            default : 
                return 200;
        }
    }
    
    private static int requiredSimilarityPercentDependingOn(int patternLength) {
        return 51 + patternLength + (patternLength / 5);
    }
    
    private static int requiredStrictSimilarityPercentDependingOn(int patternLength) {
        switch ( patternLength ) {
            case 0 : return 50;
            case 1 : return 50;
            case 2 : return 50;
            case 3 : return 52;
            case 4 : return 53;
            case 5 : return 55;   
            case 6 : return 58;   
            case 7 : return 60;    
            case 8 : return 62;
            case 9 : return 64;
            case 10 : return 65;
            case 11 : return 66;    
            case 12 : return 67; 
            case 13 : return 68;
            case 14 : return 69;    
            case 15 : return 70;
            default : return 71;
        }
    }
    
    public static boolean isSimilar(String target, String pattern) {
        return isSimilarInternally(target, pattern, USE_CACHE);
    }
    
    static boolean isSimilarInternally(
            String target, String pattern, CacheUsage cacheUsage) {
        if ( cacheUsage.equals(USE_CACHE) ) {
            Boolean similarity = CACHE.searchNullableCachedFor(target, pattern);
            if ( nonNull(similarity) ) {
                similarityLog(format(
                        "FOUND CACHED %s (target: %s, pattern: %s)", 
                        similarity, target, pattern));
                return similarity;
            }
        }        
        
        String pureTarget = removeAllSeparators(target);
        String purePattern = removeAllSeparators(pattern);
        int similarityPercent = calculateSimilarityPercent(pureTarget, purePattern);
        int requiredPercent = requiredSimilarityPercentDependingOn(purePattern.length());
        boolean similar = similarityPercent > requiredPercent;       
        similarityLog(format("result : %s %s%% (%s%% required)", 
                similar ? "OK" : "FAIL",
                similarityPercent, 
                requiredPercent));
        
        if ( cacheUsage.equals(USE_CACHE) ) {
            CACHE.addToCache(target, pattern, similar);
        }
        
        return similar;          
    }
    
    public static boolean isSimilarPathToPath(String targetPath, String patternPath) {
        similarityLog("[PATH] pattern : " + patternPath);
        similarityLog("[PATH] target  : " + targetPath);
        String[] patterns = splitPathFragmentsFrom(patternPath);
        String[] targets = splitPathFragmentsFrom(targetPath);
        
        int matchesRequired = patterns.length;
        int matchesCounter = 0;
        
        outer: for (String pattern : patterns) {
            inner: for (String target : targets) {
                if ( isSimilar(target, pattern) ) {
                    matchesCounter++;
                    if ( matchesCounter == matchesRequired ) {
                        break outer;
                    }
                }
            }
        }
        
        boolean matches = matchesRequired == matchesCounter;
        similarityLog("");
        similarityLog(format("[PATH] result : %s %s mathes (%s required)", 
                matches ? "OK" : "FAIL",
                matchesCounter,
                matchesRequired));
        return matches;
    }
    
    public static boolean hasSimilar(Collection<String> realTargets, String pattern) {
        return realTargets
                .stream()
                .anyMatch(target -> isSimilar(target, pattern));
    }
    
    public static boolean isStrictSimilar(String target, String pattern) {
        target = lower(target);
        pattern = lower(pattern);
        
        if ( target.equals(pattern) ) {
            return true;
        }
        
        int presentChars = 0;
        
        if ( target.length() == pattern.length() ) {
            for (int i = 0; i < target.length(); i++) {
                if ( ( pattern.indexOf(target.charAt(i)) > -1 ) &&
                     ( target.indexOf(pattern.charAt(i)) > -1 ) ) {
                    presentChars++;
                } 
            }
        } else {
            int presentCharsInPattern = 0;
            int presentCharsInTarget = 0;
            
            for (int i = 0; i < target.length(); i++) {
                if ( pattern.indexOf(target.charAt(i)) > -1 ) {
                    presentCharsInPattern++;
                } 
            }
            
            for (int i = 0; i < pattern.length(); i++) {
                if ( target.indexOf(pattern.charAt(i)) > -1 ) {
                    presentCharsInTarget++;
                } 
            }
            
            presentChars = target.length() - 
                    ( ( pattern.length() - presentCharsInPattern ) + 
                      ( target.length() - presentCharsInTarget ) );
        }
                        
        boolean acceptable = 
                isRatioAcceptable(target.length(), presentChars) && 
                isRatioAcceptable(target.length(), pattern.length());
        
        if ( acceptable && target.length() > 3 ) {            
            int similarity = calculateSimilarityPercent(target, pattern);                    
            boolean similar = similarity > requiredStrictSimilarityPercentDependingOn(pattern.length());
            acceptable = similar;
        }
        
        return acceptable;
    }
    
    public static boolean hasStrictSimilar(Collection<String> realTargets, String pattern) {
        return realTargets
                .stream()
                .anyMatch(target -> isStrictSimilar(target, pattern));
    }
    
    private static boolean isRatioAcceptable(int targetLength, int patternLength) {
        if ( targetLength < 4 ) {
            return targetLength == patternLength;
        } else if ( targetLength < 6 ) {
            return 
                    targetLength == patternLength ||
                    targetLength == ( patternLength - 1 ) || 
                    targetLength == ( patternLength + 1 );
        } else {
            return targetLength == patternLength || 
                    ( 
                    (double) min(targetLength, patternLength) / 
                    (double) max(targetLength, patternLength) ) 
                    > 0.8;
        }
    }
}
