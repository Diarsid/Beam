/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

/**
 *
 * @author Diarsid
 */
class StrictSimilarityAnalyzeData {
    
    int presentChars;
    int presentCharsInPattern;
    int presentCharsInTarget;
        
    String chain;
    String reverseChain;
    String prevChain;
    String nextChain;
    int chainIndexTarget;
    int reverseChainIndexTarget;        
    int inconsistency;
    int fixer;

    public StrictSimilarityAnalyzeData() {
        this.presentChars = 0;
        this.presentCharsInPattern = 0;
        this.presentCharsInTarget = 0;
        
        this.chain = null;
        this.reverseChain = null;
        this.prevChain = null;
        this.nextChain = null;
        this.chainIndexTarget = 0;
        this.reverseChainIndexTarget = 0;        
        this.inconsistency = 0;
        this.fixer = 0;
    }
    
    void clear() {
        this.presentChars = 0;
        this.presentCharsInPattern = 0;
        this.presentCharsInTarget = 0;
        
        this.chain = null;
        this.reverseChain = null;
        this.prevChain = null;
        this.nextChain = null;
        this.chainIndexTarget = 0;
        this.reverseChainIndexTarget = 0;        
        this.inconsistency = 0;
        this.fixer = 0;
    }
    
}
