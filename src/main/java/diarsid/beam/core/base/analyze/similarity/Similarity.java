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
        String a = "webdirectory";
        String b = "webdrictory";
        String c = "tegr";
        String x = "srcq";
        
//        System.out.println(measureInconsistency(a, a));
        System.out.println(measureInconsistency(a, b));
//        System.out.println(measureInconsistency(a, c));
//        System.out.println(measureInconsistency(a, x));
    }    
    
    private static int fix(int index, int chainOrder, int fixer) {
        int diff = chainOrder - index;
        if ( diff == fixer ) {
            return 0;
        } else {
            return abs(diff);
        }
    }
    
    private static int measureInconsistency(String target, String pattern) {
        System.out.println("pattern: " + pattern);
        
        String chain;
        String reverseChain;
        String prevChain;
        String nextChain;
        int chainOrder;
        int reverseChainOrder;        
        int inconsistency = 0;
        int fixer = 0;
        
        for (int i = 0; i < pattern.length() - 1; i++) {
            chain = pattern.substring(i, i + 2);
            chainOrder = target.indexOf(chain);
            if ( chainOrder > -1 ) {
                inconsistency = inconsistency + fix(i, chainOrder, fixer);
            } else {
                reverseChain = reverse(chain);
                reverseChainOrder = target.indexOf(reverseChain);
                if ( reverseChainOrder > -1 ) {
                    prevChain = getChainByOrderOrFirst(target, reverseChainOrder - 1);
                    nextChain = getChainByOrderOrLast(target, reverseChainOrder + 1);
                    if ( areConsistent(prevChain, reverseChain, nextChain) ) {
                        inconsistency--;
                        fixer--;
                    } else {               
                        inconsistency = inconsistency + abs(i - reverseChainOrder);
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
        for (int i = 0; i < pattern.length() - 1; i++) {
            data.chain = pattern.substring(i, i + 2);
            data.chainOrder = target.indexOf(data.chain);
            if ( data.chainOrder > -1 ) {
                data.inconsistency = data.inconsistency + fix(i, data.chainOrder, data.fixer);
            } else {
                data.reverseChain = reverse(data.chain);
                data.reverseChainOrder = target.indexOf(data.reverseChain);
                if ( data.reverseChainOrder > -1 ) {
                    data.prevChain = getChainByOrderOrFirst(target, data.reverseChainOrder - 1);
                    data.nextChain = getChainByOrderOrLast(target, data.reverseChainOrder + 1);
                    if ( areConsistent(data.prevChain, data.reverseChain, data.nextChain) ) {
                        data.inconsistency--;                        
                        data.fixer--;
                    } else {               
                        data.inconsistency = data.inconsistency + abs(i - data.reverseChainOrder);
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
    
    private static String getChainByOrderOrFirst(String target, int chainOrder) {
        if ( chainOrder < 1 ) {
            return target.substring(0, 2);
        } else {
            return target.substring(chainOrder, chainOrder + 2);
        }                   
    }
    
    private static String getChainByOrderOrLast(String target, int chainOrder) {
        if ( chainOrder >= target.length() - 2 ) {
            return target.substring(target.length() - 2);
        } else {
            return target.substring(chainOrder, chainOrder + 2);
        }
    }
    
    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }
    
    public static boolean isSimilar(String target, String pattern) {
        target = lower(target);
        pattern = lower(pattern);
        
        if ( target.equals(pattern) ) {
            return true;
        }
        
        int notFoundChars = 0;
        int realTargetCharIndex = 0;
        int realTargetPreviousCharIndex = MIN_VALUE;
        int clustered = 0;
        
        for (int patternCharIndex = 0; patternCharIndex < pattern.length(); patternCharIndex++) {
            realTargetCharIndex = target.indexOf(pattern.charAt(patternCharIndex));
            if ( realTargetCharIndex < 0 ) {
                realTargetPreviousCharIndex = MIN_VALUE;
                notFoundChars++;
            } else {
                if ( abs(realTargetCharIndex - realTargetPreviousCharIndex) == 1 || 
                        realTargetCharIndex == realTargetPreviousCharIndex ) {
                    if ( clustered == 0 ) {
                        clustered++;
                    } 
                    clustered++;
                }
                realTargetPreviousCharIndex = realTargetCharIndex;
            }
        }
        
        debug("[SIMILARITY] " + target + "::" + pattern + " = clustered " + clustered);
        
        if ( notFoundChars == 0 ) {
            return clustered > 0;
        } else if ( notFoundChars == pattern.length() ) {
            return false;
        } else {
            return 
                    ( ( (notFoundChars * 1.0f) / (pattern.length() * 1.0f) ) <= 0.2f ) &&
                    ( clustered > 0 );
        }        
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
            similar = ( data.clustered > 0 );
        } else if ( data.notFoundChars == pattern.length() ) {
            similar = false;
        } else {
            similar = 
                    ( ( (data.notFoundChars * 1.0f) / (pattern.length() * 1.0f) ) <= 0.2f ) &&
                    ( data.clustered > 0 );
        }        
        
        data.clear();
        
        if ( similar ) {
            debug("[SIMILARITY] [FOUND] " + target + "::" + pattern);
        }
        return similar;
    }
    
    public static boolean hasSimilar(Collection<String> realTargets, String pattern) {
        SimilarityAnalyzeData analyzeData = new SimilarityAnalyzeData();
        return realTargets
                .stream()
                .anyMatch(target -> isSimilar(target, pattern, analyzeData));
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
        
        if ( acceptable && target.length() > 6 ) {
            acceptable = measureInconsistency(target, pattern) <= ( ( target.length() / 4 ) + 1 );
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
        
        if ( acceptable && target.length() > 6 ) {
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
