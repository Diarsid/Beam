/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import static diarsid.beam.core.base.analyze.similarity.Similarity.CHAIN_NOT_FOUND_CHAR;
import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilarUsingSession;

/**
 *
 * @author Diarsid
 */
public class SimilarityCheckSession {
    
    char chain1;
    char chain2;
    char reverseChain1;
    char reverseChain2;
    int chainIndexTarget;
    int reverseChainIndexTarget;  
    int chainIndexTargetPrev;
    int similarityPercentSum;
    int similarityPercent;
    int similarityFound;
    int similarityResult;
    int indexDiff;
    boolean previousChainFoundAsReverse;
    boolean previousChainFoundAsWeak;
    char weakChainLastChar;
    int consistencyPercent;
    int inconsistencySum;
    int maxInconsistency;
    
    boolean isCurrentsSimilar;

    public SimilarityCheckSession() {
    }
    
    public boolean isSimilar(String target, String pattern) {
        isSimilarUsingSession(target, pattern, this);
        this.clearSessionVairables();
        return this.isCurrentsSimilar;
    }
    
    private void clearSessionVairables() {
        this.chain1 = CHAIN_NOT_FOUND_CHAR;
        this.chain2 = CHAIN_NOT_FOUND_CHAR;
        this.reverseChain1 = CHAIN_NOT_FOUND_CHAR;
        this.reverseChain2 = CHAIN_NOT_FOUND_CHAR;
        this.chainIndexTarget = -1;
        this.reverseChainIndexTarget = -1;  
        this.chainIndexTargetPrev = -1;
        this.similarityPercentSum = 0;
        this.similarityPercent = 0;
        this.similarityFound = 0;
        this.similarityResult = 0;
        this.indexDiff = 0;
        this.previousChainFoundAsReverse = false;
        this.previousChainFoundAsWeak = false;
        this.weakChainLastChar = CHAIN_NOT_FOUND_CHAR;
        this.consistencyPercent = 0;
        this.inconsistencySum = 0;
        this.maxInconsistency = 0;
    }
}
