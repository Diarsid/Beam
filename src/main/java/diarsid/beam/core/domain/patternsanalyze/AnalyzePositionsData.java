/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.patternsanalyze;

import java.util.Arrays;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.CollectionsUtils.first;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
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
    int patternInVariantLength;
    
    int missed;    
    int unsorted;
    
    int currentClusterLength;
    int currentPosition;
    int nextPosition;
    
    char currentChar;
    char nextCharInPattern;
    char nextCharInVariant;
    char previousCharInPattern;
    char previousCharInVariant;
    int currentPatternCharPositionInVariant;
    int betterCurrentPatternCharPositionInVariant;
    
    boolean clusterContinuation;
    
    double missedImportance;
    double unsortedImportance;
    double clustersImportance; 
    
    double positionsWeight;
    
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
                System.out.println(" -8.6 : current poisition is at variant start, " + this.direction);
                this.positionsWeight = this.positionsWeight - 8.6;
            }
            if ( this.isCurrentPositionAtVariantEnd() ) {
                System.out.println(" -8.6 : current poisition is at variant end, " + this.direction);
                this.positionsWeight = this.positionsWeight - 8.6;
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
                        System.out.println(" -3.0 : previous char is word separator, " + this.direction);
                        this.positionsWeight = this.positionsWeight - 3;
                    }
                }
                if ( this.clusterContinuation ) {
                    this.clusterEnds();                    
                } else {
                    this.nonClustered++;
                }
            }
        }        
        this.nonClustered = this.nonClustered + this.missed;
        this.patternInVariantLength = last(this.positions) - first(this.positions) + 1;
    }
    
    void findPatternCharsPositions() {        
        if ( this.direction.equals(FORWARD) ) {
            this.findPatternCharsPositionsInForward();
        } else {
            this.findPatternCharsPositionsInReverse();
        }        
    }
    
    private void findPatternCharsPositionsInReverse() {
        for (int currentPatternCharIndex = this.data.patternChars.length - 1; currentPatternCharIndex > -1 ; currentPatternCharIndex--) {
            this.currentCharIs(currentPatternCharIndex);
            
            if ( this.isCurrentCharAlreadyVisited() ) {
                this.setCurrentCharPositionPreviousFoundBeforeLastVisitedOne();
            } else {
                this.setCurrentCharReversePosition();
            }
            
            if ( this.currentCharFound() ) {
                this.addCurrentCharToVisited();
            }
            
            if ( this.currentCharIndexInReverseRange(currentPatternCharIndex) ) {
                if ( this.nextCharInClusterWithCurrentChar(currentPatternCharIndex) ) {
                    this.fillNextAndCurrentPositions(currentPatternCharIndex);
                } else {
                    this.findReverseBetterCurrentCharPosition();
                    if ( this.isBetterCharPositionFound() ) {
                        if ( this.isReverseCurrentCharBetterPostionInCluster(currentPatternCharIndex) ) {
                            this.replaceCurrentPositionWithBetterPosition();
                        } else {
                            this.findReverseBetterCurrentCharPositionFromPreviousCharPosition(currentPatternCharIndex);
                            if ( this.isReverseBetterCharPositionFoundAndInCluster(currentPatternCharIndex) ) {
                                this.replaceCurrentPositionWithBetterPosition();
                            }
                        }
                    }  
                }    
            } 
            this.saveCurrentCharFinalPosition(currentPatternCharIndex);
        }
        this.data.reusableVisitedChars.clear();
        this.data.skippedChars.clear();
    }

    private void findPatternCharsPositionsInForward() {
        for (int currentPatternCharIndex = 0; currentPatternCharIndex < this.data.patternChars.length; currentPatternCharIndex++) {
            this.currentCharIs(currentPatternCharIndex);
            
            if ( this.isCurrentCharAlreadyVisited() ) {
                this.setCurrentCharPositionNextFoundAfterLastVisitedOne();
            } else {
                this.setCurrentCharForwardPosition();
            }
            
            if ( this.currentCharFound() ) {
                this.addCurrentCharToVisited();
            }
            
            if ( this.currentCharIndexInForwardRange(currentPatternCharIndex) ) {
                if ( this.previousCharInClusterWithCurrentChar(currentPatternCharIndex) ) {
                    this.fillPreviousAndCurrentPositions(currentPatternCharIndex);
                } else {
                    this.findForwardBetterCurrentCharPosition();
                    //System.out.println(format("better position of '%s' in '%s' is: %s instead of: %s", currentChar, variantText, possibleBetterCurrentCharPosition, currentCharPosition));
                    if ( this.isBetterCharPositionFound() ) {
                        if ( this.isForwardCurrentCharBetterPostionInCluster(currentPatternCharIndex) ) {
                            this.replaceCurrentPositionWithBetterPosition();
                        } else {
                            this.findForwardBetterCurrentCharPositionFromPreviousCharPosition(currentPatternCharIndex);
                            if ( this.isForwardBetterCharPositionFoundAndInCluster(currentPatternCharIndex) ) {
                                this.replaceCurrentPositionWithBetterPosition();
                            }
                        }
                    } 
                }                                              
            }
            this.saveCurrentCharFinalPosition(currentPatternCharIndex);
        }
        this.data.reusableVisitedChars.clear();
        this.data.skippedChars.clear();
    }

    void newClusterStarts() {
        this.clustered++;
        this.clustersQty++;
        this.clusterContinuation = true;
        this.currentClusterLength = 1;
        if ( this.isCurrentPositionNotMissed() ) {
            if ( this.isPreviousCharWordSeparator() ) {                
                System.out.println(" -8.6 : cluster start, previous char is word separator, " + this.direction);
                this.positionsWeight = this.positionsWeight - 8.6;
            }
        }
    }

    void clusterEnds() {
        this.clustered++;
        this.clusterContinuation = false;
        if ( this.isCurrentPositionNotMissed() && 
             this.isNotVariantEnd() && 
             this.isNextCharWordSeparator() ) {
            System.out.println(" -8.6 : cluster ends, next char is word separator, " + this.direction);
                this.positionsWeight = this.positionsWeight - 8.6;
        }
    }
    
    boolean isNotVariantEnd() {
        return this.currentPosition < this.data.variantText.length() - 1;
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
                this.patternInVariantLength,
                this.data.patternChars.length,
                this.unsorted,  
                this.clustered, 
                this.clustersImportance);
        int a = 5;
    }
    
    boolean isCurrentPositionNotMissed() {
        return this.currentPosition > 0;
    }

    boolean isPreviousCharWordSeparator() {
        return isWordsSeparator(this.data.variantText.charAt(this.currentPosition - 1));
    }
    
    boolean isNextCharWordSeparator() {
        return isWordsSeparator(this.data.variantText.charAt(this.currentPosition + 1));
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
        this.unsorted = 0;
        this.currentClusterLength = 0;
        this.clusterContinuation = false;
        this.currentPosition = UNINITIALIZED;
        this.nextPosition = UNINITIALIZED;
        this.currentPatternCharPositionInVariant = UNINITIALIZED;
        this.missedImportance = 0;
        this.clustersImportance = 0;
        this.positionsWeight = 0;
        this.currentChar = ' ';
        this.patternInVariantLength = 0;
    }
    
    void saveCurrentCharFinalPosition(int currentCharIndex) {
        this.positions[currentCharIndex] = this.currentPatternCharPositionInVariant;        
        this.data.reusableVisitedChars.put(this.currentChar, this.currentPatternCharPositionInVariant);
    }

    void findForwardBetterCurrentCharPositionFromPreviousCharPosition(int currentCharIndex) {
        this.betterCurrentPatternCharPositionInVariant = this.data.variantText.indexOf(
                this.currentChar, this.positions[currentCharIndex - 1] + 1);
    }
    
    void findReverseBetterCurrentCharPositionFromPreviousCharPosition(int currentCharIndex) {
        this.betterCurrentPatternCharPositionInVariant = this.data.variantText.lastIndexOf(
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
        System.out.println(format("%s: assign position of '%s' in '%s' as %s instead of %s", this.direction, this.currentChar, this.data.variantText, this.betterCurrentPatternCharPositionInVariant, this.currentPatternCharPositionInVariant));
        this.currentPatternCharPositionInVariant = this.betterCurrentPatternCharPositionInVariant;
        this.data.skippedChars.add(this.currentChar);
    }
    
    boolean previousCharInClusterWithCurrentChar(int currentPatternCharIndex) {
        if ( this.currentPatternCharPositionInVariant <= 0 ) {
            return false;
        }
        this.previousCharInPattern = this.data.patternChars[currentPatternCharIndex - 1];
        this.previousCharInVariant = this.data.variantText.charAt(this.currentPatternCharPositionInVariant - 1);
        if ( this.previousCharInPattern == this.previousCharInVariant ) {
            System.out.println("previous matching found: " + this.previousCharInPattern + " while tesing: " + this.data.patternChars[currentPatternCharIndex]);
        }
        return ( this.previousCharInPattern == this.previousCharInVariant );
    }
    
    boolean nextCharInClusterWithCurrentChar(int currentPatternCharIndex) {        
        if ( this.currentPatternCharPositionInVariant <= 0 || 
             this.currentPatternCharPositionInVariant >= this.data.variantText.length() - 1 ) {
            return false;
        }
        this.nextCharInPattern = this.data.patternChars[currentPatternCharIndex + 1];
        this.nextCharInVariant = this.data.variantText.charAt(this.currentPatternCharPositionInVariant + 1);
        if ( this.nextCharInPattern == this.nextCharInVariant ) {
            System.out.println("next matching found: " + this.nextCharInPattern + " while tesing: " + this.data.patternChars[currentPatternCharIndex]);
        }
        return ( this.nextCharInPattern == this.nextCharInVariant );
    }
    
//    boolean previousCharInClusterWithCurrentBetterChar(int currentPatternCharIndex) {
//        char previousPatternChar = this.data.patternChars[currentPatternCharIndex - 1];
//        char previousVariantChar = this.data.variantText.charAt(this.betterCurrentPatternCharPositionInVariant - 1);
//        if ( previousPatternChar == previousVariantChar ) {
//            System.out.println("previous matching found: " + previousPatternChar);
//        }
//        return ( previousPatternChar == previousVariantChar );
//    }
    
    void fillPreviousAndCurrentPositions(int currentPatternCharIndex) {
        this.positions[currentPatternCharIndex - 1] = this.currentPatternCharPositionInVariant - 1;
        this.positions[currentPatternCharIndex] = this.currentPatternCharPositionInVariant;
    }
    
    void fillNextAndCurrentPositions(int currentPatternCharIndex) {
        this.positions[currentPatternCharIndex + 1] = this.currentPatternCharPositionInVariant + 1;
        this.positions[currentPatternCharIndex] = this.currentPatternCharPositionInVariant;
    }

    boolean isForwardCurrentCharBetterPostionInCluster(int currentCharIndex) {
        return ( this.betterCurrentPatternCharPositionInVariant == this.positions[currentCharIndex - 1] + 1 ) || 
                ( this.betterCurrentPatternCharPositionInVariant == this.positions[currentCharIndex - 1] - 1 ) ;
    }
    
    boolean isReverseCurrentCharBetterPostionInCluster(int currentCharIndex) {
        if ( currentCharIndex == this.positions.length - 1 ) {
            return ( this.betterCurrentPatternCharPositionInVariant == this.positions[currentCharIndex] + 1 ) || 
                    ( this.betterCurrentPatternCharPositionInVariant == this.positions[currentCharIndex] - 1 ) ;
        } else {
            return ( this.betterCurrentPatternCharPositionInVariant == this.positions[currentCharIndex + 1] + 1 ) || 
                    ( this.betterCurrentPatternCharPositionInVariant == this.positions[currentCharIndex + 1] - 1 ) ;
        }        
    }

    boolean isBetterCharPositionFound() {
        return this.betterCurrentPatternCharPositionInVariant > -1;
    }

    void findForwardBetterCurrentCharPosition() {
        this.betterCurrentPatternCharPositionInVariant =
                this.data.variantText.indexOf(this.currentChar, this.currentPatternCharPositionInVariant + 1);
        
        if ( this.betterCurrentPatternCharPositionInVariant < 0 && 
             this.data.skippedChars.contains(this.currentChar) ) {
            this.betterCurrentPatternCharPositionInVariant = this.data.variantText.indexOf(this.currentChar);
            this.data.skippedChars.remove(this.currentChar);
        }
    }
    
    void findReverseBetterCurrentCharPosition() {
        this.betterCurrentPatternCharPositionInVariant = 
                this.data.variantText.lastIndexOf(this.currentChar, this.currentPatternCharPositionInVariant - 1);
        
        if ( this.betterCurrentPatternCharPositionInVariant < 0 && 
             this.data.skippedChars.contains(this.currentChar) ) {
            this.betterCurrentPatternCharPositionInVariant = this.data.variantText.lastIndexOf(this.currentChar);
            this.data.skippedChars.remove(this.currentChar);
        }
    }

    boolean currentCharIndexInForwardRange(int currentCharIndex) {
        return ( currentCharIndex > 0 ) && ( currentCharIndex < this.positions.length );
//                ( this.currentPatternCharPositionInVariant < this.positions[currentCharIndex - 1] );
    }
    
    boolean currentCharIndexInReverseRange(int currentCharIndex) {
        return ( currentCharIndex > 0 ) && ( currentCharIndex < this.positions.length - 1 );                 
//                ( this.currentPatternCharPositionInVariant > this.positions[currentCharIndex - 1] );
    }

    boolean currentCharFound() {
        return this.currentPatternCharPositionInVariant > -1;
    }

    void currentCharIs(int currentCharIndex) {
        this.currentChar = this.data.patternChars[currentCharIndex];
    }
    
    private boolean isCurrentCharAlreadyVisited() {
        return this.data.reusableVisitedChars.containsKey(this.currentChar);
    }

    private void setCurrentCharForwardPosition() {
        this.currentPatternCharPositionInVariant = this.data.variantText.indexOf(this.currentChar);
    }
    
    private void setCurrentCharReversePosition() {
        this.currentPatternCharPositionInVariant = this.data.variantText.lastIndexOf(this.currentChar);
    }

    private void setCurrentCharPositionNextFoundAfterLastVisitedOne() {
        this.currentPatternCharPositionInVariant = this.data.variantText.indexOf(
                this.currentChar,
                this.data.reusableVisitedChars.get(this.currentChar) + 1);
    }
    
    private void setCurrentCharPositionPreviousFoundBeforeLastVisitedOne() {
        this.currentPatternCharPositionInVariant = this.data.variantText.lastIndexOf(
                this.currentChar, 
                this.data.reusableVisitedChars.get(this.currentChar) - 1);
    }

    private void addCurrentCharToVisited() {
        this.data.reusableVisitedChars.put(this.currentChar, this.currentPatternCharPositionInVariant);
    }    
    
    void sortPositions() {
        Arrays.sort(this.positions);
    }

    void strangeConditionOnUnsorted() {
        if ( ( this.clustered < 2 ) && ( this.unsorted > 0 ) ) {            
            System.out.println(" *1.8 : clustered < 2 && unsorted > 0");
            this.data.variantWeight = this.data.variantWeight * 1.8;
        }
    }
}
