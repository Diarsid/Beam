/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;

import static java.lang.Integer.MIN_VALUE;

/**
 *
 * @author Diarsid
 */
class SimilarityAnalyzeData {
    
    int notFoundChars;
    int realTargetCharIndex;
    int realTargetPreviousCharIndex;
    int clustered;

    public SimilarityAnalyzeData() {
        this.notFoundChars = 0;
        this.realTargetCharIndex = 0;
        this.realTargetPreviousCharIndex = MIN_VALUE;
        this.clustered = 0;        
    }
    
    void clear() {
        this.notFoundChars = 0;
        this.realTargetCharIndex = 0;
        this.realTargetPreviousCharIndex = MIN_VALUE;
        this.clustered = 0;
    }
}
