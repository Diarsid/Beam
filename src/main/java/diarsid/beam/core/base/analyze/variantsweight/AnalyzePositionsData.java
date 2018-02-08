/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.reverseOrder;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.countUsorted;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.unsortedImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_1;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_2;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_3;
import static diarsid.beam.core.base.util.CollectionsUtils.first;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.StringUtils.isWordsSeparator;

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
    static boolean logEnabled;
    
    static {
        logEnabled = true;
    }
    
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
    int currentPositionIndex;
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
    SortedSet<Integer> forwardUnclusteredIndexes = new TreeSet<>();
    SortedSet<Integer> reverseUnclusteredIndexes = new TreeSet<>(reverseOrder());
    Set<Integer> unclusteredPatternCharIndexes = null;
    Set<Integer> localUnclusteredPatternCharIndexes = new HashSet<>();
    FindPositionsStep findPositionsStep;
    boolean hasPreviousInPattern;
    boolean hasNextInPattern;
    boolean hasPreviousInVariant;
    boolean hasNextInVariant;
    boolean positionAlreadyFilled;
    boolean addCurrentCharFoundPositionToPositions;
    boolean continueFinding;
    boolean skipNextPatternChar;
    int charsInClusterQty;
    int currentCharInVariantQty;
    int savedCurrentPatternCharPositionInVariant;
    // --
    
    int distanceBetweenClusters;
    int previousClusterLastPosition;
    int nextClusterFirstPosition;
    
    boolean clusterContinuation;
    boolean clusterStartsWithSeparator;
    boolean clusterEndsWithSeparator;
    boolean lastClusterEndsWithSeparator;
    int separatorsBetweenClusters;
    
    double missedImportance;
    double unsortedImportance;
    double clustersImportance; 
    
    double positionsWeight;
    
    AnalyzePositionsData(AnalyzeData data, AnalyzePositionsDirection direction) {
        this.data = data;
        this.direction = direction;
    }
    
    void analyzePositionsClusters() {
        clustersCounting: for (int i = 0; i < this.positions.length; i++) {
            this.setCurrentPosition(i);
            if ( this.isCurrentPositionMissed() ) {
                this.missed++;
                continue clustersCounting;
            }
            if ( this.isCurrentPositionAtVariantStart()  ) {
                logAnalyze("  %s -4.62 : current poisition is at variant start", this.direction);
                this.positionsWeight = this.positionsWeight - 4.62;
            }
            if ( this.isCurrentPositionAtVariantEnd() ) {
                logAnalyze("  %s -4.62 : current poisition is at variant end", this.direction);
                this.positionsWeight = this.positionsWeight - 4.62;
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
                        if ( this.isPreviousCharWordSeparator() ) {                            
                            if ( this.isNextCharWordSeparator() ) {
                                logAnalyze("  %s -11.2 : char is one-char-word", this.direction);
                                this.positionsWeight = this.positionsWeight - 11.2;
                            } else {
                                if ( this.positionAfterWordSeparatorIsContinuingPreviousCluster() ) {
                                    logAnalyze("  %s cluster is separated by separator %s_%s", 
                                            this.direction, this.previousClusterLastPosition, this.currentPosition);
                                    this.nonClustered--;
                                    this.clustered++;
                                    this.previousClusterLastPosition = this.currentPosition;
                                } else {
                                    logAnalyze("  %s -3.1 : previous char is word separator", this.direction);
                                    this.positionsWeight = this.positionsWeight - 3.1;
                                }
                            }
                        } else if ( this.isNextCharWordSeparator() ) {
                            logAnalyze("  %s -3.1 : next char is word separator", this.direction);
                            this.positionsWeight = this.positionsWeight - 3.1;
                        }                        
                        this.nonClustered++;
                    }
                }
            } else {                
                if ( this.clusterContinuation ) {
                    this.clusterEnds();                    
                } else {
                    if ( this.isCurrentPositionNotMissed() ) {
                        if ( this.isPreviousCharWordSeparator() ) {                            
                            if ( this.isNextCharWordSeparator() ) {
                                logAnalyze("  %s -11.2 : char is one-char-word", this.direction);
                                this.positionsWeight = this.positionsWeight - 11.2;
                            } else {
                                if ( this.positionAfterWordSeparatorIsContinuingPreviousCluster() ) {
                                    logAnalyze("  %s cluster is separated by separator %s_%s", 
                                            this.direction, this.previousClusterLastPosition, this.currentPosition);
                                    this.nonClustered--;
                                    this.clustered++;
                                } else {
                                    if ( this.currentPositionCharIsPatternStart() ) {
                                        logAnalyze("  %s -17.71 : previous char is word separator, current char is at pattern start!", this.direction);
                                        this.positionsWeight = this.positionsWeight - 17.71;
                                    } else {
                                        logAnalyze("  %s -3.1 : previous char is word separator", this.direction);
                                        this.positionsWeight = this.positionsWeight - 3.1;
                                    }
                                }                            
                            }
                        } else if ( this.isNextCharWordSeparator() ) {
                            logAnalyze("  %s -3.1 : next char is word separator", this.direction);
                            this.positionsWeight = this.positionsWeight - 3.1;
                        }
                    }
                    this.nonClustered++;
                }
            }
        }        
        this.nonClustered = this.nonClustered + this.missed;
        this.patternInVariantLength = last(this.positions) - first(this.positions) + 1;
        
        if ( clustersQty > 1 ) {
            if ( this.separatorsBetweenClusters > 0 ) {
                if ( this.distanceBetweenClusters == this.separatorsBetweenClusters) {
                    logAnalyze("  %s all clusters are one pattern, can be regarded as one cluster!", this.direction);
                    this.clustersQty = 1;
                }
            } else {
                
            }           
        }
    }
        
    void findPatternCharsPositions() {
        
        findPositionsStep = STEP_1;
        logAnalyze("  %s", direction);
        logAnalyze("    %s", findPositionsStep);
        if ( direction.equals(FORWARD) ) {
            unclusteredPatternCharIndexes = forwardUnclusteredIndexes;
            for (int currentPatternCharIndex = 0; currentPatternCharIndex < data.patternChars.length; currentPatternCharIndex++) {                
                processCurrentPatternCharOf(currentPatternCharIndex);
            } 
        } else {
            unclusteredPatternCharIndexes = reverseUnclusteredIndexes;
            for (int currentPatternCharIndex = this.data.patternChars.length - 1; currentPatternCharIndex > -1 ; currentPatternCharIndex--) {
                processCurrentPatternCharOf(currentPatternCharIndex);
            }
        }
        
        unclusteredPatternCharIndexes.addAll(localUnclusteredPatternCharIndexes);
        localUnclusteredPatternCharIndexes.clear(); 
        // define if first step was good enough
        
        if ( nonEmpty(unclusteredPatternCharIndexes) ) {
            findPositionsStep = STEP_2;
            logAnalyze("    %s", findPositionsStep);
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
            logAnalyze("    %s", findPositionsStep);
            for (Integer currentPatternCharIndex : unclusteredPatternCharIndexes) {
                processCurrentPatternCharOf(currentPatternCharIndex);
            }
        }      
        this.unclusteredPatternCharIndexes.clear();
        this.unclusteredPatternCharIndexes.addAll(this.localUnclusteredPatternCharIndexes);
        this.localUnclusteredPatternCharIndexes.clear();
        
        this.filledPositions.clear();
        this.unclusteredPatternCharIndexes.clear();
        this.unclusteredPatternCharIndexes = null;
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
        logAnalyze("      [iterate] '%s'(%s in pattern)", this.currentChar, currentPatternCharIndex);
        if ( skipNextPatternChar ) {
            logAnalyze("        [info] '%s'(%s in pattern) is skipped!", this.currentChar, currentPatternCharIndex);
            skipNextPatternChar = false;
            return;
        }
        if ( positions[currentPatternCharIndex] != UNINITIALIZED ) {
            logAnalyze("        [info] '%s' in pattern is already found - %s", this.currentChar, positions[currentPatternCharIndex]);
            return;
        }

        hasPreviousInPattern = currentPatternCharIndex > 0;
        hasNextInPattern = currentPatternCharIndex < data.patternChars.length - 1;

        if ( direction.equals(FORWARD) ) {
            currentPatternCharPositionInVariant = data.variantText.indexOf(currentChar);
        } else {
            currentPatternCharPositionInVariant = data.variantText.lastIndexOf(currentChar);
        }
        
        currentCharInVariantQty = 0;
        if ( currentPatternCharPositionInVariant < 0 ) {
            positions[currentPatternCharIndex] = -1;
            logAnalyze("        [info] '%s' not found in variant", this.currentChar);
            return;
        }        
        
        charsInClusterQty = 0;
        continueFinding = true;

        characterFinding : while ( currentPatternCharPositionInVariant >= 0 && continueFinding ) {

            hasPreviousInVariant = currentPatternCharPositionInVariant > 0;
            hasNextInVariant = currentPatternCharPositionInVariant < data.variantText.length() - 1;

            currentCharInVariantQty++;
            positionAlreadyFilled = filledPositions.contains(currentPatternCharPositionInVariant);
            charsInClusterQty = 0;

            if ( ! positionAlreadyFilled ) {

                if ( hasPreviousInPattern && hasPreviousInVariant ) {
                    if ( previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) 
                            || filledPositions.contains(currentPatternCharPositionInVariant - 1) ) {
                        logAnalyze("        [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                                previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                        charsInClusterQty++;
                    }
                }
                if ( hasNextInPattern && hasNextInVariant ) {
                    if ( nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) 
                            || filledPositions.contains(currentPatternCharPositionInVariant + 1) ) {
                        logAnalyze("        [info] next '%s'(%s in variant) is in cluster with current '%s'", 
                                nextCharInVariant, currentPatternCharPositionInVariant + 1, currentChar);
                        charsInClusterQty++;
                    }
                }
                if ( findPositionsStep.typoSearchingAllowed() ) {
                    if ( hasPreviousInPattern && hasNextInVariant ) {

                        previousCharInPattern = data.patternChars[currentPatternCharIndex - 1];
                        nextCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant + 1);

                        if ( previousCharInPattern == nextCharInVariant ) {
                            logAnalyze("        [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and next in variant", 
                                    currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                            charsInClusterQty++;
                        }
                    }
                    if ( hasPreviousInVariant && hasNextInPattern ) {

                        previousCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant - 1);
                        nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];

                        if ( previousCharInVariant == nextCharInPattern ) {
                            logAnalyze("        [info] typo found '%s'(%s in variant) - '%s' is next in pattern and previous in variant", 
                                    currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                            charsInClusterQty++;
                        }
                    }
                }

                if ( findPositionsStep.canAddToPositions(charsInClusterQty) ) {
                    addCurrentCharFoundPositionToPositions = true;
                    logAnalyze("        [SAVE] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
                    positions[currentPatternCharIndex] = currentPatternCharPositionInVariant;
                    if ( findPositionsStep.equals(STEP_1) ) {
                        if ( fillPositionIfPossible(currentPatternCharIndex - 1, currentPatternCharPositionInVariant - 1) ) {
                            logAnalyze("          [SAVE] '%s'(%s in variant) is previous both in pattern and variant", previousCharInVariant, currentPatternCharPositionInVariant - 1);
                            if ( direction.equals(REVERSE) ) {
                                skipNextPatternChar = true;
                            }
                        }
                        if ( fillPositionIfPossible(currentPatternCharIndex + 1, currentPatternCharPositionInVariant + 1) ) {
                            logAnalyze("          [SAVE] '%s'(%s in variant) is next both in pattern and variant", nextCharInVariant, currentPatternCharPositionInVariant + 1);
                            if ( direction.equals(FORWARD) ) {
                                skipNextPatternChar = true;
                            }
                        }                        
                    }
                    filledPositions.add(currentPatternCharPositionInVariant);
                    continueFinding = false;
                }

            }         

            savedCurrentPatternCharPositionInVariant = currentPatternCharPositionInVariant;
            if ( direction.equals(FORWARD) ) {
                currentPatternCharPositionInVariant = 
                        data.variantText
                                .indexOf(
                                        currentChar, 
                                        currentPatternCharPositionInVariant + 1);
            } else {
                currentPatternCharPositionInVariant = 
                        data.variantText
                                .lastIndexOf(
                                        currentChar, 
                                        currentPatternCharPositionInVariant - 1);
            }    
        }

        // if current position has not been added because it does not satisfy requirements...
        if ( ! addCurrentCharFoundPositionToPositions && ! positionAlreadyFilled ) {
            // ...but if it is STEP_1 and there are only 1 such char in the whole pattern, there is not sense
            // to do operation for this char in subsequent steps - add this char to filled positions and exclude
            // it from subsequent iterations
            if ( findPositionsStep.equals(STEP_1) && currentCharInVariantQty == 1 ) {
                logAnalyze("        [SAVE] '%s'(%s in variant) is single char in variant", currentChar, savedCurrentPatternCharPositionInVariant);
                positions[currentPatternCharIndex] = savedCurrentPatternCharPositionInVariant;
                filledPositions.add(savedCurrentPatternCharPositionInVariant);
            } else {
                logAnalyze("        [info] position of '%s' is not defined", currentChar);
                localUnclusteredPatternCharIndexes.add(currentPatternCharIndex);
            }                
        }

        addCurrentCharFoundPositionToPositions = false;
        savedCurrentPatternCharPositionInVariant = UNINITIALIZED;
            
    }
    
    private boolean fillPositionIfPossible(int positionIndex, int positionValue) {
        if ( positions[positionIndex] == UNINITIALIZED ) {
            if ( ! filledPositions.contains(positionValue) ) {
                positions[positionIndex] = positionValue;
                filledPositions.add(positionValue);
                return true;
            }
        }
        return false;
    }

    void newClusterStarts() {
        if ( this.clustersQty > 0 ) {
            this.nextClusterFirstPosition = this.currentPosition;
            this.distanceBetweenClusters = this.distanceBetweenClusters +
                    this.nextClusterFirstPosition - this.previousClusterLastPosition - 1;
        }
        this.clustered++;
        this.clustersQty++;
        this.clusterContinuation = true;
        this.currentClusterLength = 1;
        this.clusterStartsWithSeparator = false;
//        if ( this.isCurrentPositionNotMissed() ) {
            if ( this.currentPosition == 0 ) {
                logAnalyze("  %s -8.6 : cluster starts with variant", this.direction);
                this.positionsWeight = this.positionsWeight - 8.6;
                this.clusterStartsWithSeparator = true;
            } else if ( this.isPreviousCharWordSeparator() ) {                
                logAnalyze("  %s -8.6 : cluster start, previous char is word separator", this.direction);
                this.positionsWeight = this.positionsWeight - 8.6;
                this.clusterStartsWithSeparator = true;
            } else if ( this.isCurrentCharWordSeparator() ) {
                logAnalyze("  %s -8.6 : cluster start, current char is word separator", this.direction);
                this.positionsWeight = this.positionsWeight - 8.6;
                this.clusterStartsWithSeparator = true;
            }
            
            if ( this.lastClusterEndsWithSeparator ) {
                this.separatorsBetweenClusters++;
                if ( this.clusterStartsWithSeparator && this.distanceBetweenClusters > 1 ) {
                    this.separatorsBetweenClusters++;
                }
            }            
//        }
    }

    void clusterEnds() {
        this.previousClusterLastPosition = this.currentPosition;
        this.clustered++;
        this.clusterContinuation = false;
        
        if ( this.isCurrentPositionNotMissed() ) {
            if ( this.isCurrentCharVariantEnd() ) {
                logAnalyze("  %s -4.6 : cluster ends with variant", this.direction);
                this.positionsWeight = this.positionsWeight - 4.6;
                this.clusterEndsWithSeparator = true;
            } else if ( this.isNextCharWordSeparator() ) {
                logAnalyze("  %s -4.6 : cluster ends, next char is word separator", this.direction);
                this.positionsWeight = this.positionsWeight - 4.6;
                this.clusterEndsWithSeparator = true;
            } else if ( this.isCurrentCharWordSeparator() ) {
                logAnalyze("  %s -4.6 : cluster ends, current char is word separator", this.direction);
                this.positionsWeight = this.positionsWeight - 4.6;
                this.clusterEndsWithSeparator = true;
            }
            
            if ( this.clusterStartsWithSeparator && this.clusterEndsWithSeparator ) {
                logAnalyze("  %s -10.25 : cluster is a word", this.direction);
                this.positionsWeight = this.positionsWeight - 10.25;
            }
        }
        
        this.lastClusterEndsWithSeparator = this.clusterEndsWithSeparator;
        this.clusterStartsWithSeparator = false;
        this.clusterEndsWithSeparator = false;
    }
    
    boolean isCurrentCharVariantEnd() {
        return this.currentPosition == this.data.variantText.length() - 1;
    }
    
    void setCurrentPosition(int i) {
        this.currentPositionIndex = i;
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
                this.missed, 
                this.clustersImportance,
                this.data.patternChars.length,
                this.data.variantText.length());
        this.unsortedImportance = unsortedImportanceDependingOn(
                this.patternInVariantLength,
                this.data.patternChars.length,
                this.unsorted,  
                this.clustered, 
                this.clustersImportance);
    }
    
    boolean isCurrentPositionNotMissed() {
        return this.currentPosition >= 0;
    }
    
    boolean isCurrentCharWordSeparator() {
        return isWordsSeparator(this.data.variantText.charAt(this.currentPosition));
    }

    boolean isPreviousCharWordSeparator() {
        return 
                this.currentPosition == 0 || 
                isWordsSeparator(this.data.variantText.charAt(this.currentPosition - 1));
    }
    
    boolean isNextCharWordSeparator() {
        return 
                this.currentPosition == this.data.variantText.length() - 1 ||
                isWordsSeparator(this.data.variantText.charAt(this.currentPosition + 1));
    }
    
    private boolean positionAfterWordSeparatorIsContinuingPreviousCluster() {
        if ( this.currentPosition < 1 || this.currentPositionIndex == 0) {
            return false;
        } 
        return (this.currentPosition - this.previousClusterLastPosition) == 2;
    }
    
    private boolean currentPositionCharIsPatternStart() {
        if ( this.currentPosition == 0 || this.positions[0] == 0 ) {
            return false;
        } 
        return this.data.patternChars[0] == this.data.variantText.charAt(this.currentPosition);
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
        this.clusterStartsWithSeparator = false;
        this.clusterEndsWithSeparator = false;
        this.lastClusterEndsWithSeparator = false;
        this.separatorsBetweenClusters = 0;
        this.currentPosition = UNINITIALIZED;
        this.currentPositionIndex = UNINITIALIZED;
        this.nextPosition = UNINITIALIZED;
        this.currentPatternCharPositionInVariant = UNINITIALIZED;
        this.missedImportance = 0;
        this.clustersImportance = 0;
        this.positionsWeight = 0;
        this.distanceBetweenClusters = 0;
        this.previousClusterLastPosition = UNINITIALIZED;
        this.nextClusterFirstPosition = UNINITIALIZED;
        this.currentChar = ' ';
        this.patternInVariantLength = 0;
        this.skipNextPatternChar = false;
    }
    
    boolean previousCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {
        if ( this.currentPatternCharPositionInVariant <= 0 ) {
            return false;
        }
        this.previousCharInPattern = this.data.patternChars[currentPatternCharIndex - 1];
        this.previousCharInVariant = this.data.variantText.charAt(this.currentPatternCharPositionInVariant - 1);
        
        return ( this.previousCharInPattern == this.previousCharInVariant );
    }
    
    boolean nextCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {        
        if ( this.currentPatternCharPositionInVariant < 0 || 
             this.currentPatternCharPositionInVariant >= this.data.variantText.length() - 1 ) {
            return false;
        }
        this.nextCharInPattern = this.data.patternChars[currentPatternCharIndex + 1];
        this.nextCharInVariant = this.data.variantText.charAt(this.currentPatternCharPositionInVariant + 1);
        
        return ( this.nextCharInPattern == this.nextCharInVariant );
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
