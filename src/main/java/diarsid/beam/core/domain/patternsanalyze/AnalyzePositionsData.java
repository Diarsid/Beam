/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.patternsanalyze;

import java.util.Arrays;

import static java.lang.String.format;

import static diarsid.beam.core.domain.patternsanalyze.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.countUsorted;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.isWordsSeparator;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.missedImportanceDependingOn;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.unsortedImportanceDependingOn;

/**
 *
 * @author Diarsid
 */
class AnalyzePositionsData {
    
    static enum AnalyzePositionsDirection {
        FORWARD,
        REVERSE
    }
    static final int UNINITIALIZED = -10;
    
    final AnalyzeData data;
    final AnalyzePositionsDirection direction;
    
    int[] positions;
    
    int clustersQty;
    int clustered;
    int nonClustered;
    
    int missed;    
    int unsorted;
    
    int currentClusterLength;
    int currentPosition;
    int nextPosition;
    char currentChar;
    int currentCharPosition;
    int betterCurrentCharPosition;
    
    boolean clusterContinuation;
    
    double missedImportance;
    double unsortedImportance;
    double clustersImportance;    
    
    AnalyzePositionsData(AnalyzeData data, AnalyzePositionsDirection direction) {
        this.data = data;
        this.direction = direction;
    }
    
    void findPositionsClusters() {
        clustersCounting: for (int i = 0; i < this.positions.length; i++) {
            this.setCurrentPosition(i);
            if ( this.isCurrentPositionMissed() ) {
                this.missed++;
                continue clustersCounting;
            }
            if ( this.isCurrentPositionAtVariantStart()  ) {
                this.data.variantWeight = this.data.variantWeight - 5.9;
            }
            if ( this.isCurrentPositionAtVariantEnd() ) {
                this.data.variantWeight = this.data.variantWeight - 5.9;
            }
            if ( this.hasNextPosition(i)) {
                this.setNextPosition(i);
                if ( this.isCurrentAndNextPositionInCluster() ) {
                    if ( this.clusterContinuation ) {
                        this.clusterIsContinuing();
                    } else {
                        this.newClusterStarts();
                    }
                } else {
                    if ( this.clusterContinuation ) {
                        this.clusterEnds();
                    } else {
                        this.nonClustered++;
                    }
                }
            } else {
                if ( this.isCurrentPositionNotMissed() ) {
                    if ( this.isPreviousCharWordSeparator() ) {
                        this.data.variantWeight = this.data.variantWeight - 3;
                    }
                }
                if ( this.clusterContinuation ) {
                    this.clustered++;
                } else {
                    this.nonClustered++;
                }
            }
        }        
        this.nonClustered = this.nonClustered + this.missed;
    }
    
    void findPatternCharsPositions() {        
        if ( this.direction.equals(FORWARD) ) {
            this.findPatternCharsPositionsInForward();
        } else {
            this.findPatternCharsPositionsInReverse();
        }        
    }
    
    private void findPatternCharsPositionsInReverse() {
        for (int currentCharIndex = this.data.patternChars.length - 1; currentCharIndex > -1 ; currentCharIndex--) {
            this.currentCharIs(currentCharIndex);
            if ( this.isCurrentCharAlreadyVisited() ) {
                this.setCurrentCharPositionPreviousFoundBeforeLastVisitedOne();
            } else {
                this.setCurrentCharReversePosition();
            }
            if ( this.currentCharFound() ) {
                this.addCurrentCharToVisited();
            }
            if ( this.currentCharIndexInReverseRange(currentCharIndex) ) {
                this.findReverseBetterCurrentCharPosition();
                if ( this.isBetterCharPositionFound() ) {
                    if ( this.isReverseCurrentCharBetterPostionInCluster(currentCharIndex) ) {
                        this.replaceCurrentPositionWithBetterPosition();
                    } else {
                        this.findReverseBetterCurrentCharPositionFromPreviousCharPosition(currentCharIndex);
                        if ( this.isReverseBetterCharPositionFoundAndInCluster(currentCharIndex) ) {
                            this.replaceCurrentPositionWithBetterPosition();
                        }
                    }
                }
            } 
            this.saveCurrentCharFinalPosition(currentCharIndex);
        }
        this.data.reusableVisitedChars.clear();
    }

    private void findPatternCharsPositionsInForward() {
        for (int currentCharIndex = 0; currentCharIndex < this.data.patternChars.length; currentCharIndex++) {
            this.currentCharIs(currentCharIndex);
            if ( this.isCurrentCharAlreadyVisited() ) {
                this.setCurrentCharPositionNextFoundAfterLastVisitedOne();
            } else {
                this.setCurrentCharForwardPosition();
            }
            if ( this.currentCharFound() ) {
                this.addCurrentCharToVisited();
            }
            if ( this.currentCharIndexInForwardRange(currentCharIndex) ) {
                this.findForwardBetterCurrentCharPosition();
                //System.out.println(format("better position of '%s' in '%s' is: %s instead of: %s", currentChar, variantText, possibleBetterCurrentCharPosition, currentCharPosition));
                if ( this.isBetterCharPositionFound() ) {
                    if ( this.isForwardCurrentCharBetterPostionInCluster(currentCharIndex) ) {
                        this.replaceCurrentPositionWithBetterPosition();
                    } else {
                        this.findForwardBetterCurrentCharPositionFromPreviousCharPosition(currentCharIndex);
                        if ( this.isForwardBetterCharPositionFoundAndInCluster(currentCharIndex) ) {
                            this.replaceCurrentPositionWithBetterPosition();
                        }
                    }
                }
            }
            this.saveCurrentCharFinalPosition(currentCharIndex);
        }
        this.data.reusableVisitedChars.clear();
    }

    void newClusterStarts() {
        this.clustered++;
        this.clustersQty++;
        this.clusterContinuation = true;
        this.currentClusterLength = 1;
        if ( this.isCurrentPositionNotMissed() ) {
            if ( this.isPreviousCharWordSeparator() ) {
                this.data.variantWeight = this.data.variantWeight - 8.6;
            }
        }
    }
    
    void setCurrentPosition(int i) {
        this.currentPosition = this.positions[i];
    }
    
    void clusterIsContinuing() {
        this.clustered++;
        this.currentClusterLength++;
    }

    boolean isCurrentAndNextPositionInCluster() {
        return this.currentPosition == this.nextPosition - 1;
    }

    void setNextPosition(int i) {
        this.nextPosition = this.positions[i + 1];
    }

    boolean hasNextPosition(int i) {
        return i < this.positions.length - 1;
    }

    boolean isCurrentPositionAtVariantStart() {
        return this.currentPosition == 0;
    }
    
    boolean isCurrentPositionAtVariantEnd() {
        return this.currentPosition == this.data.variantText.length() - 1;
    }

    boolean isCurrentPositionMissed() {
        return this.currentPosition < 0;
    }
    
    void calculateImportance() {
        this.clustersImportance = clustersImportanceDependingOn(
                this.clustersQty, this.clustered, this.nonClustered);
        this.missedImportance = missedImportanceDependingOn(
                this.missed, this.clustersImportance);
        this.unsortedImportance = unsortedImportanceDependingOn(
                this.unsorted, this.clustersImportance);
    }
    
    boolean isCurrentPositionNotMissed() {
        return this.currentPosition > 0;
    }

    void clusterEnds() {
        this.clustered++;
        this.clusterContinuation = false;
    }

    boolean isPreviousCharWordSeparator() {
        return isWordsSeparator(this.data.variantText.charAt(this.currentPosition - 1));
    }
    
    void countUnsortedPositions() {
        this.unsorted = countUsorted(this.positions);
    }
    
    void clearPositionsAnalyze() {
        this.positions = null;
        this.missed = 0;
        this.clustersQty = 0;
        this.clustered = 0;
        this.nonClustered = 0;
        this.currentClusterLength = 0;
        this.clusterContinuation = false;
        this.currentPosition = UNINITIALIZED;
        this.nextPosition = UNINITIALIZED;
        this.currentCharPosition = UNINITIALIZED;
        this.missedImportance = 0;
        this.clustersImportance = 0;
        this.currentChar = ' ';
    }
    
    void saveCurrentCharFinalPosition(int currentCharIndex) {
        this.positions[currentCharIndex] = this.currentCharPosition;
    }

    void findForwardBetterCurrentCharPositionFromPreviousCharPosition(int currentCharIndex) {
        this.betterCurrentCharPosition = this.data.variantText.indexOf(
                this.currentChar, this.positions[currentCharIndex - 1] + 1);
    }
    
    void findReverseBetterCurrentCharPositionFromPreviousCharPosition(int currentCharIndex) {
        this.betterCurrentCharPosition = this.data.variantText.lastIndexOf(
                this.currentChar, this.positions[currentCharIndex - 1] - 1);
    }

    boolean isForwardBetterCharPositionFoundAndInCluster(int currentCharIndex) {
        return isBetterCharPositionFound() && 
                isForwardCurrentCharBetterPostionInCluster(currentCharIndex);
    }
    
    boolean isReverseBetterCharPositionFoundAndInCluster(int currentCharIndex) {
        return isBetterCharPositionFound() && 
                isReverseCurrentCharBetterPostionInCluster(currentCharIndex);
    }

    void replaceCurrentPositionWithBetterPosition() {
        System.out.println(format("assign position of '%s' in '%s' as %s instead of %s", this.currentChar, this.data.variantText, this.betterCurrentCharPosition, this.currentCharPosition));
        this.currentCharPosition = this.betterCurrentCharPosition;
    }

    boolean isForwardCurrentCharBetterPostionInCluster(int currentCharIndex) {
        return ( this.betterCurrentCharPosition == this.positions[currentCharIndex - 1] + 1 ) || 
                ( this.betterCurrentCharPosition == this.positions[currentCharIndex - 1] - 1 ) ;
    }
    
    boolean isReverseCurrentCharBetterPostionInCluster(int currentCharIndex) {
        if ( currentCharIndex == this.positions.length - 1 ) {
            return ( this.betterCurrentCharPosition == this.positions[currentCharIndex] + 1 ) || 
                    ( this.betterCurrentCharPosition == this.positions[currentCharIndex] - 1 ) ;
        } else {
            return ( this.betterCurrentCharPosition == this.positions[currentCharIndex + 1] + 1 ) || 
                    ( this.betterCurrentCharPosition == this.positions[currentCharIndex + 1] - 1 ) ;
        }        
    }

    boolean isBetterCharPositionFound() {
        return this.betterCurrentCharPosition > -1;
    }

    void findForwardBetterCurrentCharPosition() {
        this.betterCurrentCharPosition =
                this.data.variantText.indexOf(this.currentChar, this.currentCharPosition + 1);
    }
    
    void findReverseBetterCurrentCharPosition() {
        this.betterCurrentCharPosition = 
                this.data.variantText.lastIndexOf(this.currentChar, this.currentCharPosition - 1);
    }

    boolean currentCharIndexInForwardRange(int currentCharIndex) {
        return ( currentCharIndex > 0 ) && 
                ( this.currentCharPosition < this.positions[currentCharIndex - 1] );
    }
    
    boolean currentCharIndexInReverseRange(int currentCharIndex) {
        return ( currentCharIndex < this.positions.length ) && 
                ( currentCharIndex > 0 ) && 
                ( this.currentCharPosition > this.positions[currentCharIndex - 1] );
    }

    boolean currentCharFound() {
        return this.currentCharPosition > -1;
    }

    void currentCharIs(int currentCharIndex) {
        this.currentChar = this.data.patternChars[currentCharIndex];
    }
    
    private boolean isCurrentCharAlreadyVisited() {
        return this.data.reusableVisitedChars.containsKey(this.currentChar);
    }

    private void setCurrentCharForwardPosition() {
        this.currentCharPosition = this.data.variantText.indexOf(this.currentChar);
    }
    
    private void setCurrentCharReversePosition() {
        this.currentCharPosition = this.data.variantText.lastIndexOf(this.currentChar);
    }

    private void setCurrentCharPositionNextFoundAfterLastVisitedOne() {
        this.currentCharPosition = this.data.variantText.indexOf(
                this.currentChar,
                this.data.reusableVisitedChars.get(this.currentChar) + 1);
    }
    
    private void setCurrentCharPositionPreviousFoundBeforeLastVisitedOne() {
        this.currentCharPosition = this.data.variantText.lastIndexOf(
                this.currentChar, 
                this.data.reusableVisitedChars.get(this.currentChar) - 1);
    }

    private void addCurrentCharToVisited() {
        this.data.reusableVisitedChars.put(this.currentChar, this.currentCharPosition);
    }    
    
    void sortPositions() {
        Arrays.sort(this.positions);
    }

    void strangeConditionOnUnsorted() {
        if ( ( this.clustered < 2 ) && ( this.unsorted > 0 ) ) {
            this.data.variantWeight = this.data.variantWeight * 1.8;
        }
    }
}
