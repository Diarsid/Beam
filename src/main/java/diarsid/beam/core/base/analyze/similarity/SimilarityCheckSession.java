/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Logs.debug;


/**
 *
 * @author Diarsid
 */
public class SimilarityCheckSession {
    
    private Set<Integer> pairHashes;
    private Integer currentPairHash;
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
        this.pairHashes = new HashSet<>();
        debug("[SIMILARITY SESSION] [START]");
    }
    
    public boolean isSimilar(String target, String pattern) {
        if ( this.notUsedBefore(target, pattern) ) {
            this.freshCount++;
            return Similarity.isSimilar(target, pattern);
        } else {
            this.usedCount++;
            return false;
        }        
//        this.clearSessionVairables();
//        return this.isCurrentsSimilar;
    }
    
    public void close() {
        this.currentPairHash = null;
        this.pairHashes.clear();
        this.pairHashes = null;
        debug(format("[SIMILARITY SESSION] [END] fresh: %s, duplicates: %s", this.freshCount, this.usedCount));
    }
    
    private boolean notUsedBefore(String target, String pattern) {
        this.currentPairHash = pairHash(target, pattern);
        boolean usedBefore = this.pairHashes.contains(this.currentPairHash);
        this.pairHashes.add(this.currentPairHash);
        return ! usedBefore;
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
