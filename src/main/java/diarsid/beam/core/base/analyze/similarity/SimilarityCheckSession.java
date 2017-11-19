/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Logs.debug;


/**
 *
 * @author Diarsid
 */
public class SimilarityCheckSession {
    
    private Map<Integer, Boolean> cachedSimilarResults;
    private int freshCount;
    private int usedCount;
    
//    char chain1;
//    char chain2;
//    char reverseChain1;
//    char reverseChain2;
//    int chainIndexTarget;
//    int reverseChainIndexTarget;  
//    int chainIndexTargetPrev;
//    int similarityPercentSum;
//    int similarityPercent;
//    int similarityFound;
//    int similarityResult;
//    int indexDiff;
//    boolean previousChainFoundAsReverse;
//    boolean previousChainFoundAsWeak;
//    char weakChainLastChar;
//    int consistencyPercent;
//    int inconsistencySum;
//    int maxInconsistency;
//    
//    boolean isCurrentsSimilar;

    public SimilarityCheckSession() {
        this.cachedSimilarResults = new HashMap<>();
        debug("[SIMILARITY SESSION] [START]");
    }
    
    public boolean isSimilar(String target, String pattern) {
        Integer pairHash = pairHash(target, pattern);
        if ( this.cachedSimilarResults.containsKey(pairHash) ) {
            this.usedCount++;
            return this.cachedSimilarResults.get(pairHash);
        } else {
            this.freshCount++;
            boolean similar = Similarity.isSimilar(target, pattern);
            this.cachedSimilarResults.put(pairHash, similar);
            return similar;
        }        
//        this.clearSessionVairables();
//        return this.isCurrentsSimilar;
    }
    
    public void close() {
        this.cachedSimilarResults.clear();
        this.cachedSimilarResults = null;
        debug(format("[SIMILARITY SESSION] [END] fresh: %s, duplicates: %s", this.freshCount, this.usedCount));
    }
    
    private static int pairHash(String s1, String s2) {
        return s1.hashCode() * 17 + s2.hashCode() * 31;
    }
    
//    private void clearSessionVairables() {
//        this.chain1 = CHAIN_NOT_FOUND_CHAR;
//        this.chain2 = CHAIN_NOT_FOUND_CHAR;
//        this.reverseChain1 = CHAIN_NOT_FOUND_CHAR;
//        this.reverseChain2 = CHAIN_NOT_FOUND_CHAR;
//        this.chainIndexTarget = -1;
//        this.reverseChainIndexTarget = -1;  
//        this.chainIndexTargetPrev = -1;
//        this.similarityPercentSum = 0;
//        this.similarityPercent = 0;
//        this.similarityFound = 0;
//        this.similarityResult = 0;
//        this.indexDiff = 0;
//        this.previousChainFoundAsReverse = false;
//        this.previousChainFoundAsWeak = false;
//        this.weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
//        this.consistencyPercent = 0;
//        this.inconsistencySum = 0;
//        this.maxInconsistency = 0;
//    }
}
