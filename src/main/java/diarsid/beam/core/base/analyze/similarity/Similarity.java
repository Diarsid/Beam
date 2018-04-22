/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;


import java.util.Collection;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.lang.String.format;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.util.ArraysUtil.array;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.MathUtil.absDiffOneIfZero;
import static diarsid.beam.core.base.util.PathUtils.removeSeparators;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Similarity {
    
    static final char CHAIN_NOT_FOUND_CHAR = '*';
    
    private static final int CHAIN_1_NOT_FOUND = -2;
    private static final int CHAIN_2_NOT_FOUND = -3;
    
    private static boolean logLevelBasicEnabled = 
            configuration().asBoolean("analyze.similarity.log.base");
    private static boolean logLevelAdvancedEnabled = 
            configuration().asBoolean("analyze.similarity.log.advanced");
    

    private Similarity() {        
    }
    
    public static void main(String[] args) {
        String target = "folder_1aAAaa.txt";
        String pattern = "yatx";
//        System.out.println(containsWeakChain("alforcr", 'f', 'o'));
        
        
//        String target = "engines";
//        String pattern = "eninges";

//        System.out.println(calculateSimilarityPercent("jshell", "shall"));
//        System.out.println(calculateSimilarityPercent("Programs", "proagm"));
        System.out.println(calculateSimilarityPercent(target, pattern));
//        System.out.println(calculateSimilarity("folder_1/file_1.txt", "foldaaa"));
//        System.out.println(calculateSimilarity("AAaaDir", "foldile"));
//        System.out.println(calculateSimilarity("folder_1/inner/nested/aaa.txt", "foldaaa"));
//        System.out.println(calculateSimilarity("folder_1/inner/nested/list_movie.txt", "foldile"));
        
//        System.out.println(measureInconsistency(a, a));
//        System.out.println(measureInconsistency("engines", "eninges"));
//        System.out.println(measureInconsistency("design", "engines"));
//        System.out.println(measureInconsistency(a, x));
    }    
    
    private static int compareAndFix(int chainIndex1, int chainIndex2, int fixer) {
        int diff = chainIndex2 - chainIndex1;
        if ( diff == fixer ) {
            return 0;
        } else {
            if ( diff != 0 ) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    private static int compare(int chainIndex1, int chainIndex2) {
        if ( chainIndex1 == chainIndex2 ) {
            return 0;
        } else {
            return 1;
        }
    }
    
    private static int measureInconsistency(String target, String pattern) {
        System.out.println("pattern: " + pattern);
        
        String chain;
        String reverseChain;
        String prevChain;
        String nextChain;
        int chainIndexTarget;
        int reverseChainIndexTarget;        
        int inconsistency = 0;
        int fixer = 0;
        
        for (int chainIndexPattern = 0; chainIndexPattern < pattern.length() - 1; chainIndexPattern++) {
            chain = pattern.substring(chainIndexPattern, chainIndexPattern + 2);
            chainIndexTarget = target.indexOf(chain);
            if ( chainIndexTarget > -1 ) {
                inconsistency = inconsistency + compareAndFix(chainIndexPattern, chainIndexTarget, fixer);
            } else {
                reverseChain = reverseChain(chain);
                reverseChainIndexTarget = target.indexOf(reverseChain);
                if ( reverseChainIndexTarget > -1 ) {
                    prevChain = getChainByIndexOrFirst(target, reverseChainIndexTarget - 1);
                    nextChain = getChainByIndexOrLast(target, reverseChainIndexTarget + 1);
                    if ( areConsistent(prevChain, reverseChain, nextChain) ) {
                        inconsistency--;
                        fixer--;
                    } else {               
                        inconsistency = inconsistency + compare(chainIndexPattern, reverseChainIndexTarget);
                    }
                } else {
                    inconsistency++;
                    fixer++;
                }
            }
        }
        return inconsistency;
    }
    
    private static int measureInconsistency(
            String target, String pattern, StrictSimilarityAnalyzeData data) {        
        for (int chainIndexPattern = 0; chainIndexPattern < pattern.length() - 1; chainIndexPattern++) {
            data.chain = pattern.substring(chainIndexPattern, chainIndexPattern + 2);
            data.chainIndexTarget = target.indexOf(data.chain);
            if ( data.chainIndexTarget > -1 ) {
                data.inconsistency = data.inconsistency + compareAndFix(chainIndexPattern, data.chainIndexTarget, data.fixer);
            } else {
                data.reverseChain = reverseChain(data.chain);
                data.reverseChainIndexTarget = target.indexOf(data.reverseChain);
                if ( data.reverseChainIndexTarget > -1 ) {
                    data.prevChain = getChainByIndexOrFirst(target, data.reverseChainIndexTarget - 1);
                    data.nextChain = getChainByIndexOrLast(target, data.reverseChainIndexTarget + 1);
                    if ( areConsistent(data.prevChain, data.reverseChain, data.nextChain) ) {
                        data.inconsistency--;                        
                        data.fixer--;
                    } else {               
                        data.inconsistency = data.inconsistency + compare(chainIndexPattern, data.reverseChainIndexTarget);
                    }
                } else {
                    data.inconsistency++;
                    data.fixer++;
                }
            }
        }
        return data.inconsistency;
    }
    
    private static boolean areConsistent(String prevChain, String reverseChain, String nextChain) {
        return prevChain.charAt(1) == reverseChain.charAt(0) && 
               reverseChain.charAt(1) == nextChain.charAt(0);
    }
    
    private static String getChainByIndexOrFirst(String target, int chainOrder) {
        if ( chainOrder < 1 ) {
            return target.substring(0, 2);
        } else {
            return target.substring(chainOrder, chainOrder + 2);
        }                   
    }
    
    private static String getChainByIndexOrLast(String target, int chainOrder) {
        if ( chainOrder >= target.length() - 2 ) {
            return target.substring(target.length() - 2);
        } else {
            return target.substring(chainOrder, chainOrder + 2);
        }
    }
    
    private static String reverseChain(String s) {
        return new String(array(s.charAt(1), s.charAt(0)));
    }
    
    private static int indexOfChain(String target, char chain1, char chain2) {
        return indexOfChainFrom(target, -1, chain1, chain2);
    }
    
    private static int indexOfChainFrom(
            String target, int fromExclusive, char chain1, char chain2) {
        return indexOfChainWithLoopFrom(target, fromExclusive, chain1, chain2);
    }
    
    private static int indexOfChainWithoutLoopFrom(
            String target, int fromExclusive, char chain1, char chain2) {
                int indexOfChain1 = target.indexOf(chain1, fromExclusive + 1);
        
        if ( indexOfChain1 < 0 ) {
            return CHAIN_1_NOT_FOUND;
        }
        while ( indexOfChain1 > -1 && indexOfChain1 < target.length() - 1 ) {            
            if ( target.charAt(indexOfChain1 + 1) == chain2 ) {
                return indexOfChain1;
            } else {
                return CHAIN_2_NOT_FOUND;
            }
        }       
        return CHAIN_2_NOT_FOUND;
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
            System.out.println();
        }
    }
    
    private static void similarityLog(String s, int indentLevel) {
        if ( indentLevel == 0 ) {
            similarityLog(s);
        } else {
            if ( logLevelBasicEnabled && logLevelAdvancedEnabled ) {
                System.out.println(format("[SIMILARITY] %s%s", indentOf(indentLevel), s));
            } 
        }               
    }
    
    private static void similarityLog(String s) {
        if ( logLevelBasicEnabled ) {
            System.out.println("[SIMILARITY] " + s);
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
        int chainIndexTarget;
        int reverseChainIndexTarget;  
        int chainIndexTargetPrev = -1;
        int similarityPercentSum = 0;
        int similarityPercent = 100 / (pattern.length() - 1);
        int patternCharPercent = 100 / pattern.length();
        int similarityResult;
        int patternCharsNotFoundPercentSum = 0;
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
            similarityPercentSum = similarityPercentSum - similarityPercent;
        }
        similarityLog(format("max inconsistency : %s", maxInconsistency), 1);        
        
        for (int chainIndexPattern = 0; chainIndexPattern < pattern.length() - 1; chainIndexPattern++) {
            chain1 = pattern.charAt(chainIndexPattern);
            chain2 = pattern.charAt(chainIndexPattern + 1);
            similarityLog(format("chain '%s%s'", chain1, chain2), 1);
            
            chainIndexTarget = indexOfChain(target, chain1, chain2);
            if ( chainIndexTarget > -1 && chainIndexTarget == chainIndexTargetPrev ) {
                similarityLog("found, equal to previous, ignore and continue", 2);
                chainIndexTarget = indexOfChainFrom(target, chainIndexTarget, chain1, chain2);
            }
            
            if ( chainIndexTarget == CHAIN_1_NOT_FOUND ) {
                patternCharsNotFoundPercentSum = patternCharsNotFoundPercentSum + patternCharPercent;
                similarityLog(format("char '%s' not found", chain1), 2);
                if ( patternCharsNotFoundPercentSum > 50 ) {
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
                        inconsistencySum++;
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
                                char prevFoundChain2 = target.charAt(chainIndexTargetPrev + 1);
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
            patternCharsNotFoundPercentSum = patternCharsNotFoundPercentSum + patternCharPercent;
            inconsistencySum++;
            similarityLog(format("char '%s' not found at end", pattern.charAt(pattern.length() - 1)), 2);
            if ( patternCharsNotFoundPercentSum > 50 ) {
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
        int notFoundCharsSimilarityPenaltyPercent = 0;
        if ( patternCharsNotFoundPercentSum > 0 ) {
            notFoundCharsSimilarityPenaltyPercent = (patternCharsNotFoundPercentSum * notFoundPenalty(patternCharsNotFoundPercentSum / patternCharPercent)) / 100;
        }
        similarityLog(format("consistency     : %s%%", consistencyPercent));
        similarityLog(format("chars not found : %s%% -> penalty : %s%%", patternCharsNotFoundPercentSum, notFoundCharsSimilarityPenaltyPercent));
        similarityLog(format("similarity      : %s%%", similarityPercentSum));

        if ( patternCharsNotFoundPercentSum > 0 ) {
            similarityPercentSum = similarityPercentSum * (100 - notFoundCharsSimilarityPenaltyPercent) / 100;
        }
        similarityResult = ( similarityPercentSum * consistencyPercent / 100 );
        return similarityResult;
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
//        switch ( patternLength ) {
//            case 0 : return 49;
//            case 1 : return 49;
//            case 2 : return 49;
//            case 3 : return 49;
//            case 4 : return 49;
//            case 5 : return 56;   
//            case 6 : return 53;   
//            case 7 : return 56;    
//            case 8 : return 58;
//            case 9 : return 60;
//            case 10 : return 62;
//            case 11 : return 63;    
//            case 12 : return 64; 
//            case 13 : return 65;
//            case 14 : return 66;    
//            case 15 : return 67;
//            default : return 68;
//        }
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
        target = removeSeparators(target);
        pattern = removeSeparators(pattern);
        int similarityPercent = calculateSimilarityPercent(target, pattern);
        int requiredPercent = requiredSimilarityPercentDependingOn(pattern.length());
        boolean similar = similarityPercent > requiredPercent;       
        similarityLog(format("result : %s %s%% (%s%% required)", 
                similar ? "OK" : "FAIL",
                similarityPercent, 
                requiredPercent));
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
    
    public static boolean isSimilarUsingSession(
            String target, String pattern, SimilarityCheckSession session) {
        boolean similar = 
//                calculateSimilarityPercentUsingSession(target, pattern, session) > 
                calculateSimilarityPercent(target, pattern) > 
                requiredSimilarityPercentDependingOn(pattern.length());
        return similar;
//        session.isCurrentsSimilar = similar;          
    }
    
    private static boolean isSimilar(
            String target, String pattern, SimilarityAnalyzeData data) {
        target = lower(target);
        pattern = lower(pattern);        
        
        if ( target.equals(pattern) ) {
            return true;
        }  
        
        data.clear();
        
        for (int patternCharIndex = 0; patternCharIndex < pattern.length(); patternCharIndex++) {
            data.realTargetCharIndex = target.indexOf(pattern.charAt(patternCharIndex));
            if ( data.realTargetCharIndex < 0 ) {
                data.realTargetPreviousCharIndex = MIN_VALUE;
                data.notFoundChars++;
            } else {
                if ( abs(data.realTargetCharIndex - data.realTargetPreviousCharIndex) == 1 || 
                        data.realTargetCharIndex == data.realTargetPreviousCharIndex ) {
                    if ( data.clustered == 0 ) {
                        data.clustered++;
                    } 
                    data.clustered++;
                }
                data.realTargetPreviousCharIndex = data.realTargetCharIndex;
            }
        }
        
        debug("[SIMILARITY] " + target + "::" + pattern + " = clustered " + data.clustered);
        
        boolean similar;
        if ( data.notFoundChars == 0 ) {
            similar = ( data.clustered >= ( pattern.length() / 2 ) );
        } else if ( data.notFoundChars == pattern.length() ) {
            similar = false;
        } else {
            similar = 
                    ( ( (data.notFoundChars * 1.0f) / (pattern.length() * 1.0f) ) <= 0.2f ) &&
                    ( data.clustered >= ( pattern.length() / 2 ) );
        }        
        
        data.clear();
        
        if ( similar ) {
            debug("[SIMILARITY] [FOUND] " + target + "::" + pattern);
        }
        return similar;
    }
    
    public static boolean hasSimilar(Collection<String> realTargets, String pattern) {
//        SimilarityAnalyzeData analyzeData = new SimilarityAnalyzeData();
        return realTargets
                .stream()
//                .anyMatch(target -> isSimilar(target, pattern, analyzeData));
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
//            int inconsitency = measureInconsistency(target, pattern);
//            int acceptableInconsistency = ( target.length() / 3 ) + 1;
//            boolean incosistencyLevelAcceptable = inconsitency <= acceptableInconsistency;
            
            int similarity = calculateSimilarityPercent(target, pattern);                    
            boolean similar = similarity > requiredStrictSimilarityPercentDependingOn(pattern.length());
            acceptable = similar;
        }
        
        return acceptable;
    }
    
    private static boolean isStrictSimilar(
            String target, String pattern, StrictSimilarityAnalyzeData data) {
        target = lower(target);
        pattern = lower(pattern);
        
        if ( target.equals(pattern) ) {
            return true;
        }
        
        data.clear();
        
        if ( target.length() == pattern.length() ) {
            for (int i = 0; i < target.length(); i++) {
                if ( ( pattern.indexOf(target.charAt(i)) > -1 ) &&
                     ( target.indexOf(pattern.charAt(i)) > -1 ) ) {
                    data.presentChars++;
                } 
            }
        } else {
            
            for (int i = 0; i < target.length(); i++) {
                if ( pattern.indexOf(target.charAt(i)) > -1 ) {
                    data.presentCharsInPattern++;
                } 
            }
            
            for (int i = 0; i < pattern.length(); i++) {
                if ( target.indexOf(pattern.charAt(i)) > -1 ) {
                    data.presentCharsInTarget++;
                } 
            }
            
            data.presentChars = target.length() - 
                    ( ( pattern.length() - data.presentCharsInPattern ) + 
                      ( target.length() - data.presentCharsInTarget ) );
        }
                        
        boolean acceptable = 
                isRatioAcceptable(target.length(), data.presentChars) && 
                isRatioAcceptable(target.length(), pattern.length());
        
        if ( acceptable && target.length() > 3 ) {
            acceptable = measureInconsistency(target, pattern, data) <= 
                    ( ( target.length() / 4 ) + 1 );
        }
        
        data.clear();
        
        return acceptable;
    }
    
    public static boolean hasStrictSimilar(Collection<String> realTargets, String pattern) {
//        StrictSimilarityAnalyzeData analyzeData = new StrictSimilarityAnalyzeData();
        return realTargets
                .stream()
//                .anyMatch(target -> isStrictSimilar(target, pattern, analyzeData));
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
