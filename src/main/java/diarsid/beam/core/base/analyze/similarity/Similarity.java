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

import static diarsid.beam.core.base.util.ArraysUtil.array;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Similarity {

    private Similarity() {        
    }
    
    public static void main(String[] args) {
        String target = "folder_1AAaaDir";
        String pattern = "foldile";
//        String target = "engines";
//        String pattern = "eninges";

        System.out.println(calculateSimilarity("AAaaDir", "foldile"));
        System.out.println(calculateSimilarity("notes", "noets"));
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
        int indexOfChain1 = target.indexOf(chain1);
        while ( indexOfChain1 > -1 && indexOfChain1 < target.length() - 1 ) {            
            if ( target.charAt(indexOfChain1 + 1) == chain2 ) {
                return indexOfChain1;
            } else {
                indexOfChain1 = target.indexOf(chain1, indexOfChain1 + 1);
            }
        }
        return -1;
    }
    
    private static void log(String s, int indentLevel) {
        System.out.println("[SIMILARITY] " + );
    }
    
    private static String indentOf()
    
    public static int calculateSimilarity(String target, String pattern) {
        if ( target.equalsIgnoreCase(pattern) ) {
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
        int similarity = 0;
        
        int inconsistencySum = 0;
        int maxInconsistency = abs(target.length() - pattern.length());
        if ( maxInconsistency < pattern.length() ) {
            maxInconsistency = pattern.length();
        }
        
        for (int chainIndexPattern = 0; chainIndexPattern < pattern.length() - 1; chainIndexPattern++) {
            chain1 = pattern.charAt(chainIndexPattern);
            chain2 = pattern.charAt(chainIndexPattern + 1);
            
            chainIndexTarget = indexOfChain(target, chain1, chain2);
            if ( chainIndexTarget > -1 ) {
                System.out.println("chain " + chain1 + chain2 + " found.");
                if ( similarity == 0 ) {
                    similarity++;
                }
                similarity++;
                if ( chainIndexTargetPrev > -1 ) {
                    inconsistencySum = inconsistencySum + abs(chainIndexTarget - chainIndexTargetPrev) - 1;
                    System.out.println(format("diff for '%s%s' is %s", chain1, chain2, abs(chainIndexTarget - chainIndexTargetPrev) - 1));                    
                    
                }     
                chainIndexTargetPrev = chainIndexTarget;
            } else {
                reverseChain1 = chain2;
                reverseChain2 = chain1;
                reverseChainIndexTarget = indexOfChain(target, reverseChain1, reverseChain2);
                if ( reverseChainIndexTarget > -1 ) {
                    System.out.println("chain " + chain1 + chain2 + " found (reverse).");
                    if ( similarity == 0 ) {
                        similarity++;
                    }
                    similarity++;
                    if ( chainIndexTargetPrev > -1 ) {
                        inconsistencySum = inconsistencySum + abs(reverseChainIndexTarget - chainIndexTargetPrev) - 1;
                        System.out.println(format("diff for '%s%s' is %s", reverseChain1, reverseChain2, abs(reverseChainIndexTarget - chainIndexTargetPrev) - 1));
                    } 
                    chainIndexTargetPrev = reverseChainIndexTarget;
                } else {
                    System.out.println("chain " + chain1 + chain2 + " not found.");
                    inconsistencySum = inconsistencySum + maxInconsistency;
                }
            }
        }    
                
        int consistencyPercent = 100 - ( ( inconsistencySum * 100 / pattern.length() ) ) / maxInconsistency;        
        
        int similarityPercent;
        if ( similarity == 0 ) {
            similarityPercent = 0;
        } else if ( similarity >= pattern.length() ) {
            similarityPercent = 100;
        } else {    
            similarityPercent = ( (similarity * 100) / pattern.length() );
        }
        
        System.out.println(format("consistency : %s%%", consistencyPercent));
        System.out.println(format("similarity  : %s%%", similarityPercent));
        
        return ( similarityPercent + consistencyPercent ) / 2;
    }
    
    public static boolean isSimilar(String target, String pattern) {
        boolean similar = calculateSimilarity(target, pattern) > 59;
        return similar;
//        target = lower(target);
//        pattern = lower(pattern);
//        
//        if ( target.equals(pattern) ) {
//            return true;
//        }
//        
//        int notFoundChars = 0;
//        int realTargetCharIndex = 0;
//        int realTargetPreviousCharIndex = MIN_VALUE;
//        int clustered = 0;
//        
//        for (int patternCharIndex = 0; patternCharIndex < pattern.length(); patternCharIndex++) {
//            realTargetCharIndex = target.indexOf(pattern.charAt(patternCharIndex));
//            if ( realTargetCharIndex < 0 ) {
//                realTargetPreviousCharIndex = MIN_VALUE;
//                notFoundChars++;
//            } else {
//                if ( abs(realTargetCharIndex - realTargetPreviousCharIndex) == 1 || 
//                        realTargetCharIndex == realTargetPreviousCharIndex ) {
//                    if ( clustered == 0 ) {
//                        clustered++;
//                    } 
//                    clustered++;
//                }
//                realTargetPreviousCharIndex = realTargetCharIndex;
//            }
//        }
//        
//        debug("[SIMILARITY] " + target + "::" + pattern + " = clustered " + clustered);
//        
//        if ( notFoundChars == 0 ) {
//            return clustered >= ( pattern.length() / 2 );
//        } else if ( notFoundChars == pattern.length() ) {
//            return false;
//        } else {
//            return 
//                    ( ( (notFoundChars * 1.0f) / (pattern.length() * 1.0f) ) <= 0.2f ) &&
//                    ( clustered >= ( pattern.length() / 2 ) );
//        }               
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
            int inconsitency = measureInconsistency(target, pattern);
            boolean incosistencyLevelAcceptable = inconsitency <= ( ( target.length() / 4 ) + 1 );
            acceptable = incosistencyLevelAcceptable;
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
        StrictSimilarityAnalyzeData analyzeData = new StrictSimilarityAnalyzeData();
        return realTargets
                .stream()
                .anyMatch(target -> isStrictSimilar(target, pattern, analyzeData));
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
