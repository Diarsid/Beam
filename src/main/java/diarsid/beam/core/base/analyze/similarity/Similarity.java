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
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Similarity {
    
    static final char CHAIN_NOT_FOUND_CHAR = '*';
    
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
        int indexOfChain1 = target.indexOf(chain1, fromExclusive + 1);
        if ( indexOfChain1 < 0 ) {
            return -2;
        }
        while ( indexOfChain1 > -1 && indexOfChain1 < target.length() - 1 ) {            
            if ( target.charAt(indexOfChain1 + 1) == chain2 ) {
                return indexOfChain1;
            } else {
                indexOfChain1 = target.indexOf(chain1, indexOfChain1 + 1);
            }
        }
        return -1;
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
    
//    private static int calculateSimilarityPercentUsingSession(
//            String target, String pattern, SimilarityCheckSession session) {  
//        if ( target.isEmpty() 
//                || pattern.isEmpty() 
//                || pattern.length() == 1 
//                || target.length() == 1) {
//            return 0;
//        }
//        if ( (pattern.length() - target.length()) > (pattern.length() / 3) ) {
//            return 0;
//        }
//        
//        similarityLogBreak();        
//        similarityLog("pattern : " + pattern);
//        similarityLog("target  : " + target);
//        if ( target.equalsIgnoreCase(pattern) ) {
//            similarityLog("equal", 1);
//            return 100;
//        }
//        
//        target = lower(target);
//        pattern = lower(pattern);
//        
//        session.similarityPercent = 100 / (pattern.length() - 1);
//        session.maxInconsistency = abs(target.length() - pattern.length());
//        if ( session.maxInconsistency < pattern.length() ) {
//            session.maxInconsistency = pattern.length();
//        }
//        if ( session.maxInconsistency > pattern.length() * 3 ) {
//            session.maxInconsistency = pattern.length() * 3;
//        }
//        if ( pattern.length() > target.length() ) {
//            similarityLog("pattern is longer than target!", 1);
//            session.similarityPercentSum = session.similarityPercentSum - session.similarityPercent;
//        }
//        similarityLog(format("max inconsistency : %s", session.maxInconsistency), 1);        
//        
//        for (int chainIndexPattern = 0; chainIndexPattern < pattern.length() - 1; chainIndexPattern++) {
//            session.chain1 = pattern.charAt(chainIndexPattern);
//            session.chain2 = pattern.charAt(chainIndexPattern + 1);
//            similarityLog(format("chain '%s%s'", session.chain1, session.chain2), 1);
//            
//            session.chainIndexTarget = indexOfChain(target, session.chain1, session.chain2);
//            if ( session.chainIndexTarget > -1 ) {
//                similarityLog("found", 2);
//                session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent;
//                session.similarityFound++;
//                if ( session.chainIndexTargetPrev > -1 ) {
//                    session.indexDiff = abs(session.chainIndexTarget - session.chainIndexTargetPrev) - 1;
//                    session.inconsistencySum = session.inconsistencySum + session.indexDiff;
//                    similarityLog(format("chain inconsistency : %s", session.indexDiff), 2);
//                }     
//                if ( session.previousChainFoundAsWeak ) {
//                    if ( session.weakChainLastChar == session.chain1 ) {
//                        similarityLog("previous weak chain is consistent <- join!", 2);
//                        session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent;
//                        session.similarityFound++;
//                        session.inconsistencySum++;
//                    }
//                }
//                session.chainIndexTargetPrev = session.chainIndexTarget;
//                session.previousChainFoundAsReverse = false;
//                session.previousChainFoundAsWeak = false;
//                session.weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
//            } else {
//                session.reverseChain1 = session.chain2;
//                session.reverseChain2 = session.chain1;
//                session.reverseChainIndexTarget = indexOfChain(target, session.reverseChain1, session.reverseChain2);
//                if ( session.reverseChainIndexTarget > -1 ) {
//                    similarityLog("found reverse", 2);
//                    session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent;
//                    session.similarityFound++;
//                    session.inconsistencySum++;
//                    if ( session.chainIndexTargetPrev > -1 ) {
//                        session.indexDiff = abs(session.reverseChainIndexTarget - session.chainIndexTargetPrev) - 1;
//                        session.inconsistencySum = session.inconsistencySum + session.indexDiff;
//                        similarityLog(format("chain inconsistency : %s", session.indexDiff), 2);                        
//                    }
//                    if ( session.previousChainFoundAsWeak ) {
//                        similarityLog("previous chain found as weak <- join!", 2);
//                        session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent;
//                        session.similarityFound++;
//                        session.inconsistencySum++;
//                    }
//                    session.chainIndexTargetPrev = session.reverseChainIndexTarget;
//                    session.previousChainFoundAsReverse = true;
//                    session.previousChainFoundAsWeak = false;
//                    session.weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
//                } else {
//                    similarityLog("not found directly", 2);
//                    if ( containsWeakChain(target, session.chain1, session.chain2) ) {
//                        if ( session.previousChainFoundAsReverse ) {
//                            similarityLog("found as weak chain after reverse <- join!", 2);
//                            session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent;
//                            session.similarityFound++;
//                            session.inconsistencySum++;
//                        } else {
//                            similarityLog("found as weak chain, not after reverse", 2);
//                            if ( chainIndexPattern == (pattern.length() - 2) && 
//                                    session.chain2 == target.charAt(target.length() - 1)) {
//                                similarityLog("weak chain is last <- join!", 2);
//                                session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent;
//                                session.similarityFound++;
//                                session.inconsistencySum++;
//                            }
//                        }
//                        session.previousChainFoundAsReverse = false;
//                        session.previousChainFoundAsWeak = true;
//                        session.weakChainLastChar = session.chain2;
//                    } else {
//                        if ( session.chainIndexTargetPrev > -1 ) {
//                            char prevChain1 = target.charAt(session.chainIndexTargetPrev);
//                            char prevChain2 = target.charAt(session.chainIndexTargetPrev + 1);
//                            if ( prevChain2 == session.chain1 || prevChain2 == session.chain2 ) {
//                                similarityLog("found partially", 2);
//                                session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent/2;
//                                session.similarityFound++;
//                                session.inconsistencySum++;
//                            } 
//                        }
//                        if ( chainIndexPattern == (pattern.length() - 2) && 
//                                    session.chain2 == target.charAt(target.length() - 1)) {
//                                similarityLog("last chars match", 2);
//                                session.similarityPercentSum = session.similarityPercentSum + session.similarityPercent/2;
//                                session.similarityFound++;
//                                session.inconsistencySum++;
//                            }
//                        session.previousChainFoundAsReverse = false;
//                        session.previousChainFoundAsWeak = false;
//                        session.weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
//                    }            
//                }
//            }
//        }         
//        
//        if ( session.similarityFound > 0 ) {
//            session.consistencyPercent = 100 - ( ( session.inconsistencySum * 100 / session.similarityFound ) ) / session.maxInconsistency;
//        } else {
//            session.consistencyPercent = 0;
//        }
//        similarityLog(format("consistency : %s%%", session.consistencyPercent), 0);
//        similarityLog(format("similarity  : %s%%", session.similarityPercentSum), 0);
//        
//        session.similarityResult = ( session.similarityPercentSum * session.consistencyPercent / 100 );
//        similarityLog(format("result : %s%%", session.similarityResult));
//        return session.similarityResult;
//    }
    
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
            if ( chainIndexTarget == -2 ) {
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
                        similarityLog(format("found as weak chain, not after reverse +%s%%", similarityPercent/2), 2);
                        similarityPercentSum = similarityPercentSum + similarityPercent/2;
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
                                char prevChain2 = target.charAt(chainIndexTargetPrev + 1);
                                if ( prevChain2 == chain1 || prevChain2 == chain2 ) {
                                    similarityLog(format("found partially +%s%%", (2 * similarityPercent / 3)), 2);
                                    similarityPercentSum = similarityPercentSum + (2 * similarityPercent / 3);
                                    previousChainsNotFoundQty--;
                                } else {
                                    if ( !previousChainFoundAsReverse && containsWeakChain(target, prevChain2, chain2) ) {
                                        similarityLog(format("found as combined weak chain +%s%%", similarityPercent/2), 2);
                                        similarityPercentSum = similarityPercentSum + similarityPercent/2;
                                    }
                                }
                            } else {
                                int chain1IndexTarget = target.indexOf(chain1);
                                int chain2IndexTarget = target.indexOf(chain2);
                                if ( chain1IndexTarget > -1 ) {
                                    if ( chain2IndexTarget > -1 ) {
                                        similarityLog(format("found partially +%s%%", (3 * similarityPercent / 4)), 2);
                                        similarityPercentSum = similarityPercentSum + (3 * similarityPercent / 4);
                                        previousChainsNotFoundQty--;
                                        if ( chain1IndexTarget > chain2IndexTarget ) {
                                            inconsistencySum++;
                                            chainIndexTargetPrev = chain1IndexTarget;
                                        } else {
                                            chainIndexTargetPrev = chain2IndexTarget;
                                        }
                                    } else {
                                        similarityLog(format("found partially +%s%%", (2 * similarityPercent / 3)), 2);
                                        similarityPercentSum = similarityPercentSum + (2 * similarityPercent / 3);
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
            similarityLog(format("char '%s' not found", pattern.charAt(pattern.length() - 1)), 2);
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
        int similarityPercent = calculateSimilarityPercent(target, pattern);
        int requiredPercent = requiredSimilarityPercentDependingOn(pattern.length());
        boolean similar = similarityPercent > requiredPercent;       
        similarityLog(format("result : %s %s%% (%s%% required)", 
                similar ? "OK" : "FAIL",
                similarityPercent, 
                requiredPercent));
        return similar;          
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
