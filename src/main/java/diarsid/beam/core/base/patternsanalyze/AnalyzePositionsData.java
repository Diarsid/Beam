/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.patternsanalyze;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

import static diarsid.beam.core.base.patternsanalyze.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.patternsanalyze.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.base.patternsanalyze.AnalyzeUtil.countUsorted;
import static diarsid.beam.core.base.patternsanalyze.AnalyzeUtil.isWordsSeparator;
import static diarsid.beam.core.base.patternsanalyze.AnalyzeUtil.missedImportanceDependingOn;
import static diarsid.beam.core.base.patternsanalyze.AnalyzeUtil.unsortedImportanceDependingOn;
import static diarsid.beam.core.base.patternsanalyze.FindPositionsStep.STEP_1;
import static diarsid.beam.core.base.patternsanalyze.FindPositionsStep.STEP_2;
import static diarsid.beam.core.base.patternsanalyze.FindPositionsStep.STEP_3;
import static diarsid.beam.core.base.util.CollectionsUtils.first;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
class AnalyzePositionsData {
    
    static enum AnalyzePositionsDirection {
        FORWARD,
        REVERSE
    }
    
    static final int UNINITIALIZED = -9;    
    
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
    
    // v.2
    Set<Integer> filledPositions = new HashSet<>();
    Set<Integer> unclusteredPatternCharIndexes = new HashSet<>();
    Set<Integer> localUnclusteredPatternCharIndexes = new HashSet<>();
    FindPositionsStep findPositionsStep;
    boolean hasPreviousInPattern;
    boolean hasNextInPattern;
    boolean hasPreviousInVariant;
    boolean hasNextInVariant;
    boolean positionAlreadyFilled;
    boolean addCurrentCharFoundPositionToPositions;
    boolean continueFinding;
    int charsInClusterQty;
    int currentCharInVariantQty;
    int savedCurrentPatternCharPositionInVariant;
    // --
    
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
            if ( this.hasNextPosition(i) ) {
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
            this.findPatternCharsPositionsInForward_2();
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
                if ( this.nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) ) {
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
    
    private void findPatternCharsPositionsInForward_2() {
        
        for (int currentPatternCharIndex = 0; currentPatternCharIndex < data.patternChars.length; currentPatternCharIndex++) {
            findPositionsStep = STEP_1;
            processCurrentPatternCharOf(currentPatternCharIndex);
        }            
        unclusteredPatternCharIndexes.addAll(localUnclusteredPatternCharIndexes);
        localUnclusteredPatternCharIndexes.clear(); 
        // define if first step was good enough
        
        if ( nonEmpty(unclusteredPatternCharIndexes) ) {
            findPositionsStep = STEP_2;
            for (Integer currentPatternCharIndex : unclusteredPatternCharIndexes) {
                processCurrentPatternCharOf(currentPatternCharIndex);
            }
        }           
        this.unclusteredPatternCharIndexes.clear();
        this.unclusteredPatternCharIndexes.addAll(localUnclusteredPatternCharIndexes);
        this.localUnclusteredPatternCharIndexes.clear();
//        if ( firstStepTooBad ) {
//            runSecondStepOneMoreTime
//            define if secont step was good enough ???
//        }
//        if ( secondStepWasTooBad ) {
//            runSecondStepOneMoreTime
//        }
        
        if ( nonEmpty(unclusteredPatternCharIndexes) ) {
            findPositionsStep = STEP_3;
            for (Integer currentPatternCharIndex : unclusteredPatternCharIndexes) {
                processCurrentPatternCharOf(currentPatternCharIndex);
            }
        }      
        this.unclusteredPatternCharIndexes.clear();
        this.unclusteredPatternCharIndexes.addAll(this.localUnclusteredPatternCharIndexes);
        this.localUnclusteredPatternCharIndexes.clear();
        
        this.filledPositions.clear();
        this.unclusteredPatternCharIndexes.clear();
        this.hasPreviousInPattern = false;
        this.hasNextInPattern = false;
        this.hasPreviousInVariant = false;
        this.hasNextInVariant = false;
        this.positionAlreadyFilled = false;
        this.charsInClusterQty = 0;
        this.currentChar = ' ';
        this.findPositionsStep = STEP_1;
    }
    
    private void processCurrentPatternCharOf(int currentPatternCharIndex) {
        currentChar = data.patternChars[currentPatternCharIndex];

        hasPreviousInPattern = currentPatternCharIndex > 0;
        hasNextInPattern = currentPatternCharIndex < data.patternChars.length - 1;

        currentPatternCharPositionInVariant = data.variantText.indexOf(currentChar);
        currentCharInVariantQty = 0;
        if ( currentPatternCharPositionInVariant >= 0 ) {
            charsInClusterQty = 0;
            continueFinding = true;

            characterFinding : while ( currentPatternCharPositionInVariant >= 0 && continueFinding ) {
                
                hasPreviousInVariant = currentPatternCharPositionInVariant > 0;
                hasNextInVariant = currentPatternCharPositionInVariant < data.variantText.length() - 1;
            
                currentCharInVariantQty++;
                positionAlreadyFilled = filledPositions.contains(this.currentPatternCharPositionInVariant);
                charsInClusterQty = 0;
                
                if ( ! positionAlreadyFilled ) {
                    
                    if ( hasPreviousInPattern && hasPreviousInVariant ) {
                        if ( previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) 
                                || filledPositions.contains(currentPatternCharPositionInVariant - 1) ) {
                            charsInClusterQty++;
                        }
                    }
                    if ( hasNextInPattern && hasNextInVariant ) {
                        if ( nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) 
                                || filledPositions.contains(currentPatternCharPositionInVariant + 1) ) {
                            charsInClusterQty++;
                        }
                    }
                    if ( findPositionsStep.typoSearchingAllowed() ) {
                        if ( hasPreviousInPattern && hasNextInVariant ) {
                            
                            previousCharInPattern = data.patternChars[currentPatternCharIndex - 1];
                            nextCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant + 1);
                            
                            if ( previousCharInPattern == nextCharInVariant ) {
                                System.out.println(format("Positions [%s]: '%s':%s <- typo found", this.findPositionsStep, this.currentChar, this.currentPatternCharPositionInVariant));
                                charsInClusterQty++;
                            }
                        }
                        if ( hasPreviousInVariant && hasNextInPattern ) {
                            
                            previousCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant - 1);
                            nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];
                            
                            if ( previousCharInVariant == nextCharInPattern ) {
                                System.out.println(format("Positions [%s]: '%s':%s <- typo found", this.findPositionsStep, this.currentChar, this.currentPatternCharPositionInVariant));
                                charsInClusterQty++;
                            }
                        }
                    }
                    
                    if ( findPositionsStep.canAddToPositions(charsInClusterQty) ) {
                        addCurrentCharFoundPositionToPositions = true;
                        System.out.println(format("Positions [%s]: '%s':%s", this.findPositionsStep, this.currentChar, this.currentPatternCharPositionInVariant));
                        positions[currentPatternCharIndex] = currentPatternCharPositionInVariant;
                        filledPositions.add(currentPatternCharPositionInVariant);
                        continueFinding = false;
                    }
                     
                }         
                
                savedCurrentPatternCharPositionInVariant = currentPatternCharPositionInVariant;
                currentPatternCharPositionInVariant = 
                        data.variantText
                                .indexOf(
                                        currentChar, 
                                        currentPatternCharPositionInVariant + 1);

            }
            
            if ( ! addCurrentCharFoundPositionToPositions ) {
                if ( findPositionsStep.equals(STEP_1) && currentCharInVariantQty == 1 ) {
                    System.out.println(format("Positions [%s]: '%s':%s", this.findPositionsStep, this.currentChar, this.savedCurrentPatternCharPositionInVariant));
                    positions[currentPatternCharIndex] = savedCurrentPatternCharPositionInVariant;
                    filledPositions.add(savedCurrentPatternCharPositionInVariant);
                } else {
                    localUnclusteredPatternCharIndexes.add(currentPatternCharIndex);
                }                
            }
            
            addCurrentCharFoundPositionToPositions = false;
            savedCurrentPatternCharPositionInVariant = UNINITIALIZED;
        } else {
            positions[currentPatternCharIndex] = -1;
        }    
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
                if ( this.previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) ) {
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
                this.currentChar, this.betterCurrentPatternCharPositionInVariant + 1);
    }
    
    void findReverseBetterCurrentCharPositionFromPreviousCharPosition(int currentCharIndex) {
        this.betterCurrentPatternCharPositionInVariant = this.data.variantText.lastIndexOf(
                this.currentChar, this.betterCurrentPatternCharPositionInVariant - 1);
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
    
    boolean previousCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {
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
    
    boolean nextCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {        
        if ( this.currentPatternCharPositionInVariant < 0 || 
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
