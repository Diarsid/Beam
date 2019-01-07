/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.Math.abs;
import static java.util.Arrays.stream;
import static java.util.Collections.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_SEARCH;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.calculateCluster;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.inconsistencyOf;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.nonClusteredImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_1;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_2;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_3;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_4;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.BAD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimate;
import static diarsid.beam.core.base.util.CollectionsUtils.first;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.MathUtil.cube;
import static diarsid.beam.core.base.util.MathUtil.onePointRatio;
import static diarsid.beam.core.base.util.MathUtil.square;
import static diarsid.beam.core.base.util.StringUtils.countWordSeparatorsInBetween;
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
    
    static final int POS_UNINITIALIZED = -9; 
    static final int POS_ERASED = -9; 
    static final int POS_NOT_FOUND = -3;
    static final String NO_REASON = "";
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
    
    int previousClusterLength;
    int currentClusterLength;
    int currentPosition;
    int currentPositionIndex;
    int nextPosition;
    int alonePositionBeforePreviousSeparator;
    boolean prevCharIsSeparator;
    boolean nextCharIsSeparator;
    
    char currentChar;
    char nextCharInPattern;
    char nextCharInVariant;
    char previousCharInPattern;
    char previousCharInVariant;
    int currentPatternCharPositionInVariant;
    int betterCurrentPatternCharPositionInVariant;
    
    // v.2
    SortedSet<Integer> forwardUnclusteredIndexes = new TreeSet<>();
    SortedSet<Integer> reverseUnclusteredIndexes = new TreeSet<>(reverseOrder());
    Set<Integer> unclusteredPatternCharIndexes = null;
    Set<Integer> localUnclusteredPatternCharIndexes = new HashSet<>();
    FindPositionsStep findPositionsStep;
    boolean previousPositionInVariantFound;
    boolean nextPositionInVariantFound;
    boolean hasPreviousInPattern;
    boolean hasNextInPattern;
    boolean hasPreviousInVariant;
    boolean hasNextInVariant;
    boolean positionAlreadyFilled;
    boolean isCurrentCharPositionAddedToPositions;
    boolean continueSearching;
    boolean skipNextPatternChar;
    int charsInClusterQty;
    int currentCharInVariantQty;
    int currentPatternCharPositionInVariantToSave;
    // --
    
    // v.3
    Map<Integer, Integer> positionUnsortedOrders = new HashMap<>();
    Map<Integer, Integer> positionPatternIndexes = new HashMap<>();
    Map<Integer, FindPositionsStep> positionFoundSteps = new HashMap<>();
    Set<Integer> filledPositions = positionFoundSteps.keySet();
    PositionCandidate positionCandidate = new PositionCandidate();
    int nearestPositionInVariant;
    List<Integer> currentClusterOrderDiffs = new ArrayList();
    Clusters clusters;
    final TreeSet<Integer> keyChars;
    boolean currentClusterOrdersIsConsistent;
    boolean previousClusterOrdersIsConsistent;
    boolean currentClusterOrdersHaveDiffCompensations;
    int unsortedPositions;
    // --
    
    int previousClusterLastPosition = POS_UNINITIALIZED;
    int previousClusterFirstPosition = POS_UNINITIALIZED;
    int currentClusterFirstPosition;
    String badReason;
    
    boolean clusterContinuation;
    boolean clusterStartsWithVariant;
    boolean clusterStartsWithSeparator;
    boolean clusterEndsWithSeparator;
    boolean previousClusterEndsWithSeparator;
    int clustersFacingEdges;
    int clustersFacingStartEdges;
    int clustersFacingEndEdges;
    int separatorsBetweenClusters;
    int allClustersInconsistency;
    
    double missedImportance;
    double clustersImportance; 
    int nonClusteredImportance;
    
    double positionsWeight;
    
    AnalyzePositionsData(AnalyzeData data, AnalyzePositionsDirection direction) {
        this.data = data;
        this.clusters = new Clusters(this.data, direction);
        this.direction = direction;
        this.keyChars = new TreeSet<>();
        this.clearPositionsAnalyze();
    }
    
    static boolean arePositionsEquals(AnalyzePositionsData dataOne, AnalyzePositionsData dataTwo) {
        return Arrays.equals(dataOne.positions, dataTwo.positions);
    }
    
    void fillPositionsFromIndex(int patternInVariantIndex) {
        int length = positions.length;
        int position = patternInVariantIndex;
        logAnalyze(POSITIONS_SEARCH, "  %s, pattern found directly", direction);
        for (int i = 0; i < length; i++) {
            positions[i] = position;
            positionFoundSteps.put(position, STEP_1);
            logAnalyze(POSITIONS_SEARCH, "    [SAVE] %s : %s", data.patternChars[i], position);    
            position++;        
        }
        logAnalyze(POSITIONS_SEARCH, "         %s", displayPositions());
        clearPositionsSearchingState();
    }
    
    private String displayPositions() {
        if ( POSITIONS_SEARCH.isDisabled() ) {
            return "";
        }
        
        String patternPositions = stream(positions)
                .mapToObj(position -> {
                    if ( position == POS_UNINITIALIZED || position == POS_NOT_FOUND ) {
                        return "_";
                    } else {
                        return String.valueOf(data.variantText.charAt(position));
                    }
                })
                .collect(joining());
        
        String variantPositions = range(0, data.variantText.length())
                .mapToObj(position -> {
                    if ( filledPositions.contains(position) ) {
                        return String.valueOf(data.variantText.charAt(position));
                    } else {
                        return "_";                        
                    }
                })
                .collect(joining());
        
        return patternPositions + " : " + variantPositions;
    }
    
    void analyzePositionsClusters() {
        logAnalyze(POSITIONS_CLUSTERS, "  %s positions clusters processing", this.direction);
        clustersCounting: for (int i = 0; i < this.positions.length; i++) {
            this.setCurrentPosition(i);
            if ( this.isCurrentPositionMissed() ) {
                this.missed++;
                this.nonClustered++;
                continue clustersCounting;
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
                        this.prevCharIsSeparator = this.isPreviousCharWordSeparator();
                        this.nextCharIsSeparator = this.isNextCharWordSeparator();
                        
                        if ( this.prevCharIsSeparator && this.nextCharIsSeparator ) {
                            this.doWhenNextAndPreviousCharsAreSeparators();
                            this.clustered++;
                            this.nonClustered--;
                        } else if ( this.prevCharIsSeparator ) {
                            this.doWhenOnlyPreviousCharacterIsSeparator();
                        } else if ( this.nextCharIsSeparator ) {
                            this.doWhenOnlyNextCharacterIsSeparator();
                        }
                        
                        this.nonClustered++;
                    }
                }
            } else {                
                if ( this.clusterContinuation ) {
                    this.clusterEnds();                    
                } else {
                    if ( this.isCurrentPositionNotMissed() ) {
                        this.prevCharIsSeparator = this.isPreviousCharWordSeparator();
                        this.nextCharIsSeparator = this.isNextCharWordSeparator();
                        
                        if ( this.prevCharIsSeparator && this.nextCharIsSeparator ) {
                            this.doWhenNextAndPreviousCharsAreSeparators();
                            this.clustered++;
                            this.nonClustered--;
                        } else if ( this.prevCharIsSeparator ) {
                            this.doWhenOnlyPreviousCharacterIsSeparator();
                        } else if ( this.nextCharIsSeparator ) {
                            this.doWhenOnlyNextCharacterIsSeparator();
                        }
                    }
                    
                    this.nonClustered++;
                }
            }            
        }        
        
        this.patternInVariantLength = last(this.positions) - first(this.positions) + 1;
                
        this.clusters.arrange();
        
        if ( this.clusters.nonEmpty() ) {
            this.analyzeAllClustersOrderDiffs();
        }
        
        if ( this.clustersQty > 1 && this.allClustersInconsistency == 0 ) {
            if ( this.separatorsBetweenClusters > 0 ) {
                if ( this.clusters.distanceBetweenClusters() == this.separatorsBetweenClusters ) {
                    logAnalyze(POSITIONS_CLUSTERS, "               [weight] all clusters are one pattern, can be regarded as one cluster!");
                    this.clustersQty = 1;
                }
            } else {
                
            }           
        }
        
        this.analyzeAllClustersPlacing();
    }

    private void doWhenNextAndPreviousCharsAreSeparators() {
        logAnalyze(POSITIONS_CLUSTERS, "               [weight] -19.2 : char is one-char-word");
        this.keyChars.add(this.currentPosition);
        this.positionsWeight = this.positionsWeight - 19.2;
        if ( this.alonePositionBeforePreviousSeparator != POS_UNINITIALIZED ) {
            this.alonePositionBeforePreviousSeparator = POS_ERASED;
        }
    }

    private void doWhenOnlyPreviousCharacterIsSeparator() {
        if ( this.currentPositionCharIsPatternStart() ) {
            this.keyChars.add(this.currentPosition);
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -17.71 : previous char is word separator, current char is at pattern start!");
            this.positionsWeight = this.positionsWeight - 17.71;
            if ( this.isClusterBeforeSeparator() ) {
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] -15.5 : there is cluster before separator!");
                this.positionsWeight = this.positionsWeight - 15.5;
            }
        } else if ( this.isClusterBeforeSeparator() ) {
            this.keyChars.add(this.currentPosition);
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -10.5 : there is cluster before separator!");
            this.positionsWeight = this.positionsWeight - 10.5;
        } else {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -3.1 : previous char is word separator");
            this.positionsWeight = this.positionsWeight - 3.1;
        }
        
        this.alonePositionBeforePreviousSeparator = this.currentPosition;
    }
    
    private boolean isClusterBeforeSeparator() {
        if ( this.previousClusterLastPosition != POS_UNINITIALIZED && this.previousClusterEndsWithSeparator ) {
            return this.previousClusterLastPosition == this.currentPosition - 2;
        } else {
            return false;
        }
    }

    private void doWhenOnlyNextCharacterIsSeparator() {
        logAnalyze(POSITIONS_CLUSTERS, "               [weight] -3.1 : next char is word separator");
        this.positionsWeight = this.positionsWeight - 3.1;
        
        if ( this.previousClusterLastPosition != POS_UNINITIALIZED && ! this.previousClusterEndsWithSeparator && this.previousClusterOrdersIsConsistent ) {
            if ( ! this.areSeparatorsPresentBetween(this.previousClusterLastPosition, this.currentPosition) ) {
                int bonus = this.previousClusterLength > 2 ? 
                        square(this.previousClusterLength) : this.previousClusterLength;
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : previous cluster and current char belong to one word", bonus);
                this.positionsWeight = this.positionsWeight - bonus;
            } 
        }
    }
    
    private boolean areSeparatorsPresentBetween(final int fromExcl, final int toExcl) {
        logAnalyze(POSITIONS_CLUSTERS, "               [weight] ...searching for separators between %s and %s", fromExcl, toExcl);
        if ( absDiff(toExcl, toExcl) < 2 ) {
            return false;
        }
        String variantText = this.data.variantText;
        for (int pointer = fromExcl + 1; pointer < toExcl; pointer++) {
            if ( isWordsSeparator(variantText.charAt(pointer)) ) {
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] separator found - %s", pointer);
                return true;
            }
        }
        return false;
    }
    
    private void analyzeAllClustersOrderDiffs() {
        int orderMeansDifferentFromZero = 0;
        boolean allClustersHaveLength2 = true;
        
        for (Cluster cluster : this.clusters.all()) {
            if ( cluster.ordersDiffMean() != 0  ) {
                orderMeansDifferentFromZero++;
            }       
            if ( cluster.length() > 2 ) {
                allClustersHaveLength2 = false;
            }
        }
        
        if ( orderMeansDifferentFromZero > 0 ) {
            this.allClustersInconsistency = orderMeansDifferentFromZero * 2;
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : clusters order incosistency", allClustersInconsistency);
            this.positionsWeight = this.positionsWeight + this.allClustersInconsistency;
        }        
        
        if ( allClustersHaveLength2 ) {
            int penalty = square(this.clusters.quantity());
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : all clusters are weak (2 length)", penalty);
            this.positionsWeight = this.positionsWeight + penalty;
        }
    }
    
    private void analyzeAllClustersPlacing() {
        if ( this.clusters.isEmpty() ) {
            return;
        }
        
        if ( estimate(this.positionsWeight + this.data.variantWeight).equals(BAD) ) {
            double placingPenalty = square( ( 10.0 - this.clustersQty ) / this.clustered );
            logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] positions weight is too bad for placing assessment");
            logAnalyze(POSITIONS_CLUSTERS, "        [weight] +%s", placingPenalty);
            
            this.positionsWeight = this.positionsWeight + placingPenalty;
        } else {
            float placingBonus = this.clusters.calculatePlacingBonus();
            logAnalyze(POSITIONS_CLUSTERS, "        [weight] -%s", placingBonus);

            this.positionsWeight = this.positionsWeight - placingBonus;
        }
    }
        
    void findPatternCharsPositions() {
                
        proceedWith(STEP_1);
        logAnalyze(POSITIONS_SEARCH, "  %s", direction);
        logAnalyze(POSITIONS_SEARCH, "    %s", findPositionsStep);
        
        if ( direction.equals(FORWARD) ) {
            unclusteredPatternCharIndexes = forwardUnclusteredIndexes;
            for (int currentPatternCharIndex = 0, charsRemained = data.patternChars.length - 1; currentPatternCharIndex < data.patternChars.length; currentPatternCharIndex++, charsRemained--) {                
                processCurrentPatternCharOf(currentPatternCharIndex, charsRemained);
            } 
        } else {
            unclusteredPatternCharIndexes = reverseUnclusteredIndexes;
            for (int currentPatternCharIndex = data.patternChars.length - 1, charsRemained = data.patternChars.length - 1; currentPatternCharIndex > -1 ; currentPatternCharIndex--, charsRemained--) {
                processCurrentPatternCharOf(currentPatternCharIndex, charsRemained);
            }
        }
        swapUnclusteredPatternCharIndexes();
        
        proceedWith(STEP_2);
        if ( isAllowedToProceedOnCurrentStep() ) {
            processAccumulatedUnclusteredPatternCharIndexes();           
            swapUnclusteredPatternCharIndexes();

            processAccumulatedUnclusteredPatternCharIndexes();        
            swapUnclusteredPatternCharIndexes();
            
            proceedWith(STEP_3);
            if ( isAllowedToProceedOnCurrentStep() ) {
                processAccumulatedUnclusteredPatternCharIndexes();      
                swapUnclusteredPatternCharIndexes();
            } else {
                proceedWith(STEP_4);
                processAccumulatedUnclusteredPatternCharIndexes();      
                swapUnclusteredPatternCharIndexes();
            }
        } else {
            proceedWith(STEP_4);
            processAccumulatedUnclusteredPatternCharIndexes();      
            swapUnclusteredPatternCharIndexes();
        }
        
        clearPositionsSearchingState();
    }
    
    private void proceedWith(FindPositionsStep step) {
        this.findPositionsStep = step;
    }

    private boolean isAllowedToProceedOnCurrentStep() {
        boolean allowed = findPositionsStep.canProceedWith(data.pattern.length());
        if ( ! allowed ) {
            logAnalyze(POSITIONS_SEARCH, "    %s is not allowed for pattern with length %s", findPositionsStep, data.pattern.length());
        }
        return allowed;
    }
    
    private void processAccumulatedUnclusteredPatternCharIndexes() {
        if ( nonEmpty(unclusteredPatternCharIndexes) ) {
            logAnalyze(POSITIONS_SEARCH, "    %s", findPositionsStep);
            int charsRemained = unclusteredPatternCharIndexes.size();
            for (Integer currentPatternCharIndex : unclusteredPatternCharIndexes) {
                charsRemained--;
                processCurrentPatternCharOf(currentPatternCharIndex, charsRemained);
            }            
        }
    }

    private void swapUnclusteredPatternCharIndexes() {
        unclusteredPatternCharIndexes.clear();
        unclusteredPatternCharIndexes.addAll(localUnclusteredPatternCharIndexes);
        localUnclusteredPatternCharIndexes.clear();
    }

    private void clearPositionsSearchingState() {
        if ( nonNull(this.unclusteredPatternCharIndexes) ) {
            this.unclusteredPatternCharIndexes.clear();
        }
        this.unclusteredPatternCharIndexes = null;
        this.previousPositionInVariantFound = false;
        this.nextPositionInVariantFound = false;
        this.hasPreviousInPattern = false;
        this.hasNextInPattern = false;
        this.hasPreviousInVariant = false;
        this.hasNextInVariant = false;
        this.positionAlreadyFilled = false;
        this.charsInClusterQty = 0;
        this.currentChar = ' ';
        this.findPositionsStep = STEP_1;
    }
    
    private void processCurrentPatternCharOf(int currentPatternCharIndex, int charsRemained) {
        currentChar = data.patternChars[currentPatternCharIndex];
        logAnalyze(POSITIONS_SEARCH, "      [explore] '%s'(%s in pattern)", this.currentChar, currentPatternCharIndex);
        if ( skipNextPatternChar ) {
            logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in pattern) is skipped!", this.currentChar, currentPatternCharIndex);
            skipNextPatternChar = false;
            return;
        }
        if ( positions[currentPatternCharIndex] != POS_UNINITIALIZED ) {
            logAnalyze(POSITIONS_SEARCH, "          [info] '%s' in pattern is already found - %s", this.currentChar, positions[currentPatternCharIndex]);
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
            positions[currentPatternCharIndex] = POS_NOT_FOUND;
            logAnalyze(POSITIONS_SEARCH, "          [info] '%s' not found in variant", this.currentChar);
            return;
        }        
        
        charsInClusterQty = 0;
        continueSearching = true;

        characterSearching : while ( currentPatternCharPositionInVariant >= 0 && continueSearching ) {
            logAnalyze(POSITIONS_SEARCH, "        [assess] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
        
            nearestPositionInVariant = POS_UNINITIALIZED;

            hasPreviousInVariant = currentPatternCharPositionInVariant > 0;
            hasNextInVariant = currentPatternCharPositionInVariant < data.variantText.length() - 1;

            currentCharInVariantQty++;
            positionAlreadyFilled = filledPositions.contains(currentPatternCharPositionInVariant);
            charsInClusterQty = 0;

            if ( ! positionAlreadyFilled ) {

                if ( hasPreviousInPattern && hasPreviousInVariant ) {
                    previousPositionInVariantFound = filledPositions.contains(currentPatternCharPositionInVariant - 1);
                    if ( previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) || 
                         previousPositionInVariantFound ) {
                        if ( findPositionsStep.equals(STEP_1) ) {
                            logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                                    previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                            charsInClusterQty++;
                            nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                        } else {
                            if ( direction.equals(FORWARD) ) {
                                if ( previousPositionInVariantFound ) {
                                    logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                                            previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                                    charsInClusterQty++;
                                    nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                                }
                            } else {
                                logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                                        previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                                charsInClusterQty++;
                                nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            }                            
                        }                        
                    }
                }
                if ( hasNextInPattern && hasNextInVariant ) {
                    if ( nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) 
                            || filledPositions.contains(currentPatternCharPositionInVariant + 1) ) {
                        logAnalyze(POSITIONS_SEARCH, "          [info] next '%s'(%s in variant) is in cluster with current '%s'", 
                                nextCharInVariant, currentPatternCharPositionInVariant + 1, currentChar);
                        charsInClusterQty++;
                        nearestPositionInVariant = currentPatternCharPositionInVariant + 1;
                    }
                }
                
                if ( findPositionsStep.typoSearchingAllowed() ) {
                    int typosFound = 0;                    
                    boolean distanceOneTypoFound = false;
                    
                    if ( hasPreviousInPattern && hasNextInVariant ) {
                        
                        previousCharInPattern = data.patternChars[currentPatternCharIndex - 1];
                        nextCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant + 1);
                        
                        if ( previousCharInPattern == nextCharInVariant ) {
                            logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and next in variant", 
                                    currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                            typosFound++;
                            distanceOneTypoFound = true;
                            nearestPositionInVariant = currentPatternCharPositionInVariant + 1;
                        }
                    }
                    if ( hasPreviousInVariant && hasNextInPattern ) {

                        previousCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant - 1);
                        nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];

                        if ( previousCharInVariant == nextCharInPattern ) {
                            if ( filledPositions.contains(currentPatternCharPositionInVariant - 1) ) {
                                logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next in pattern and previous in variant", 
                                        currentChar, currentPatternCharPositionInVariant, nextCharInPattern);
                                typosFound++;
                                distanceOneTypoFound = true;
                                nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            }                            
                        }
                    }
                    
                    if ( ! distanceOneTypoFound ) {
                        
                        boolean nextNextFoundAsTypo = false;
                        if ( hasNextInPattern && hasNextInVariant ) {
                            nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];
                            // if there are at least two characters ahead in variant...
                            if ( data.variantText.length() - currentPatternCharPositionInVariant > 2 ) {
                                int nextNextPosition = currentPatternCharPositionInVariant + 2;
                                if ( ! data.variantSeparators.contains(nextNextPosition - 1) ) {
                                    if ( filledPositions.contains(nextNextPosition) ) {
                                        if ( nextCharInPattern == data.variantText.charAt(nextNextPosition) ) {
                                            typosFound++;
                                            nearestPositionInVariant = nextNextPosition;
                                            nextNextFoundAsTypo = true;
                                        }
                                    }  
                                }                                                              
                            }
                        } 
                        if ( ! nextNextFoundAsTypo && hasPreviousInPattern && hasPreviousInVariant ) {
                            previousCharInPattern = data.patternChars[currentPatternCharIndex - 1];
                            // if there are at least two characters behind in variant...
                            if ( currentPatternCharPositionInVariant > 1 ) {
                                int prevPrevPosition = currentPatternCharPositionInVariant - 2;
                                if ( ! data.variantSeparators.contains(prevPrevPosition + 1) ) {
                                    if ( filledPositions.contains(prevPrevPosition) ) {
                                        if ( previousCharInPattern == data.variantText.charAt(prevPrevPosition) ) {
                                            typosFound++;
                                            nearestPositionInVariant = prevPrevPosition;
                                        }
                                    } 
                                }                                                               
                            }
                        }
                    }
                    
                    if ( charsInClusterQty == 0 && typosFound > 0 ) {
                        charsInClusterQty = 1;
                    }
                }

                if ( findPositionsStep.equals(STEP_1) ) {
                    if ( findPositionsStep.canAddToPositions(charsInClusterQty) ) {
                        
                        isCurrentCharPositionAddedToPositions = true;
                        positions[currentPatternCharIndex] = currentPatternCharPositionInVariant;
                        positionPatternIndexes.put(currentPatternCharPositionInVariant, currentPatternCharIndex);
                        positionFoundSteps.put(currentPatternCharPositionInVariant, findPositionsStep);
                        logAnalyze(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
                        logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
                        logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variantText);
                        
                        if ( fillPositionIfPossible(currentPatternCharIndex - 1, currentPatternCharPositionInVariant - 1) ) {
                            logAnalyze(POSITIONS_SEARCH, "          [SAVE] '%s'(%s in variant) is previous both in pattern and variant", previousCharInVariant, currentPatternCharPositionInVariant - 1);
                            logAnalyze(POSITIONS_SEARCH, "                 %s", displayPositions());
                            logAnalyze(POSITIONS_SEARCH, "                 %s : %s", data.pattern, data.variantText);
                            if ( direction.equals(REVERSE) ) {
                                skipNextPatternChar = true;
                            }
                        }
                        
                        if ( fillPositionIfPossible(currentPatternCharIndex + 1, currentPatternCharPositionInVariant + 1) ) {
                            logAnalyze(POSITIONS_SEARCH, "          [SAVE] '%s'(%s in variant) is next both in pattern and variant", nextCharInVariant, currentPatternCharPositionInVariant + 1);
                            logAnalyze(POSITIONS_SEARCH, "                 %s", displayPositions());
                            logAnalyze(POSITIONS_SEARCH, "                 %s : %s", data.pattern, data.variantText);
                            if ( direction.equals(FORWARD) ) {
                                skipNextPatternChar = true;
                            }
                        }                        
                        
                        continueSearching = false;
                    }
                } else {
                    // on steps, other than STEP_1, do not save positions directly, just record them as appropriate for saving (excluding STEP_4).
                    if ( findPositionsStep.canAddToPositions(charsInClusterQty) ) {
                                                
                        isCurrentCharPositionAddedToPositions = positionCandidate.hasAtLeastOneAcceptedCandidate();
                        
                        int orderDiffInPattern;
                        if ( nearestPositionInVariant > POS_NOT_FOUND ) {
                            Integer nearestPatternCharIndex = positionPatternIndexes.get(nearestPositionInVariant);
                            if ( isNull(nearestPatternCharIndex) ) {
                                // nearest char can be null only when current char is clastered with next pattern position
                                if ( direction.equals(FORWARD) ) {
                                    nearestPatternCharIndex = currentPatternCharIndex + 1;
                                } else {
                                    nearestPatternCharIndex = currentPatternCharIndex - 1;
                                }
                            } 
                            orderDiffInPattern = abs(currentPatternCharIndex - nearestPatternCharIndex);
                        } else {
                            orderDiffInPattern = nearestPositionInVariant;
                        }                        
                        
                        int previousCharInVariantByPattern = POS_UNINITIALIZED;
                        int nextCharInVariantByPattern = POS_UNINITIALIZED;
                        int orderDiffInVariant = POS_UNINITIALIZED;
                        
                        if ( direction.equals(FORWARD) ) {
                            if ( hasPreviousInPattern ) {
                                previousCharInVariantByPattern = positions[currentPatternCharIndex - 1];
                            }
                            if ( previousCharInVariantByPattern > -1 ) {
                                orderDiffInVariant = absDiff(previousCharInVariantByPattern, currentPatternCharPositionInVariant);
                            }
                        } else {
                            if ( hasNextInPattern ) {
                                nextCharInVariantByPattern = positions[currentPatternCharIndex + 1];
                            }
                            if ( nextCharInVariantByPattern > -1 ) {
                                orderDiffInVariant = absDiff(currentPatternCharPositionInVariant, nextCharInVariantByPattern);
                            }
                        }     
                        
                        // debugging zone
                        boolean runtimeConditionalDebug = false;
                        if ( runtimeConditionalDebug ) {
                            if ( direction.equals(FORWARD) ) {
                                if ( orderDiffInVariant > POS_UNINITIALIZED ) {
                                    if ( orderDiffInVariant == 1 ) {
                                        if ( orderDiffInPattern == 1 ) {
                                            boolean breakpoint = true;
                                        } else {
                                            boolean breakpoint = true;
                                        }                                
                                    } else if ( orderDiffInVariant > 1 ) {
                                        if ( orderDiffInPattern == 1 ) {
                                            boolean breakpoint = true;
                                        } else {
                                            boolean breakpoint = true;
                                        }
                                    }
                                } else if ( hasPreviousInPattern ) {
                                    if ( orderDiffInPattern == 1 ) {
                                        boolean breakpoint = true;
                                    } else {
                                        boolean breakpoint = true;
                                    }
                                }
                            } else {
                                if ( orderDiffInVariant > POS_UNINITIALIZED ) {
                                    if ( orderDiffInVariant == 1 ) {
                                        if ( orderDiffInPattern == 1 ) {
                                            boolean breakpoint = true;
                                        } else {
                                            boolean breakpoint = true;
                                        }                                
                                    } else if ( orderDiffInVariant > 1 ) {
                                        if ( orderDiffInPattern == 1 ) {
                                            boolean breakpoint = true;
                                        } else {
                                            boolean breakpoint = true;
                                        }
                                    }
                                } else {
                                    if ( orderDiffInPattern == 1 ) {
                                        boolean breakpoint = true;
                                    } else {
                                        boolean breakpoint = true;
                                    }
                                }
                            } 
                        }    
                        // end of debugging zone
                                                
                        positionCandidate.tryToMutate(
                                currentPatternCharPositionInVariant, 
                                orderDiffInVariant, 
                                orderDiffInPattern, 
                                charsInClusterQty,
                                charsRemained);
                    }
                }
                
            }         

            currentPatternCharPositionInVariantToSave = currentPatternCharPositionInVariant;
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
        // end of characterFinding loop
        
        if ( findPositionsStep.isAfter(STEP_1) && positionCandidate.isPresent() ) {
            int position = positionCandidate.position();
            
            if ( positionCandidate.committedMutations() > 0 && positionCandidate.hasRejectedMutations() ) {
                int a = 5;
            }
            
            positions[currentPatternCharIndex] = position;
            positionPatternIndexes.put(position, currentPatternCharIndex);
            positionFoundSteps.put(position, findPositionsStep);
            logAnalyze(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant), %s", currentChar, position, positionCandidate);
            logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
            logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variantText);
        }
        positionCandidate.clear();

        // if current position has not been added because it does not satisfy requirements...
        if ( ! isCurrentCharPositionAddedToPositions ) {
            // ...but if it is STEP_1 and there are only 1 such char in the whole pattern, there is not sense
            // to do operation for this char in subsequent steps - add this char to filled positions and exclude
            // it from subsequent iterations
            if ( findPositionsStep.canAddSingleUnclusteredPosition() && currentCharInVariantQty == 1 ) {
                if ( positionAlreadyFilled ) {
                    logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in variant) is single char in variant and already saved", currentChar, currentPatternCharPositionInVariantToSave);
                    positions[currentPatternCharIndex] = POS_NOT_FOUND;
                } else {
                    positions[currentPatternCharIndex] = currentPatternCharPositionInVariantToSave;
                    positionPatternIndexes.put(currentPatternCharPositionInVariantToSave, currentPatternCharIndex);
                    positionFoundSteps.put(currentPatternCharPositionInVariantToSave, findPositionsStep);
                    logAnalyze(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant) is single char in variant", currentChar, currentPatternCharPositionInVariantToSave);
                    logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
                    logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variantText);
                }                
            } else {
                logAnalyze(POSITIONS_SEARCH, "        [info] position of '%s' is not defined", currentChar);
                localUnclusteredPatternCharIndexes.add(currentPatternCharIndex);
            }                
        }

        isCurrentCharPositionAddedToPositions = false;
        currentPatternCharPositionInVariantToSave = POS_UNINITIALIZED;            
    }
    
    private boolean fillPositionIfPossible(int positionIndex, int positionValue) {
        if ( positions[positionIndex] == POS_UNINITIALIZED ) {
            if ( ! filledPositions.contains(positionValue) ) {
                positions[positionIndex] = positionValue;
                positionPatternIndexes.put(positionValue, positionIndex);
                positionFoundSteps.put(positionValue, findPositionsStep);
                return true;
            }
        }
        return false;
    }
    
    boolean isCurrentCharVariantEnd() {
        return this.currentPosition == this.data.variantText.length() - 1;
    }
    
    void setCurrentPosition(int i) {
        this.currentPositionIndex = i;
        this.currentPosition = this.positions[i];
    }

    void newClusterStarts() {
        this.currentClusterFirstPosition = this.currentPosition;
        this.clustered++;
        this.clustersQty++;
        this.clusterContinuation = true;
        this.currentClusterLength = 1;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        
        this.processClusterPositionOrderStats("+- ");
        
        if ( this.currentPosition == 0 ) {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -6.6 : cluster starts with variant");
            this.positionsWeight = this.positionsWeight - 6.6;
            
            this.clusterStartsWithVariant = true;
            this.clusterStartsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingStartEdges++;
        } else if ( this.isPreviousCharWordSeparator() ) {                
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -6.6 : cluster start, previous char is word separator");
            this.positionsWeight = this.positionsWeight - 6.6;
            this.clusterStartsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingStartEdges++;
        } else if ( this.isCurrentCharWordSeparator() ) {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -6.6 : cluster start, current char is word separator");
            this.positionsWeight = this.positionsWeight - 6.6;
            this.clusterStartsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingStartEdges++;
        }
    }
    
    void clusterIsContinuing() {
        this.clustered++;
        this.currentClusterLength++;
        this.processClusterPositionOrderStats("|  ");
    }

    void clusterEnds() {
        this.clustered++;
        this.currentClusterLength++;
        this.clusterContinuation = false;  
        
        this.processClusterPositionOrderStats("+- ");
        this.accumulateClusterPositionOrdersStats();        

        if ( this.isCurrentCharVariantEnd() ) {
            float bonus = 3.6f; 
            if ( this.currentClusterLength > 2 ) {
                bonus = bonus + this.currentClusterLength;
            }
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : cluster ends with variant", bonus);
            this.positionsWeight = this.positionsWeight - bonus;
            this.clusterEndsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingEndEdges++;
        } else if ( this.isNextCharWordSeparator() ) {
            float bonus = 3.6f; 
            if ( this.currentClusterLength > 2 ) {
                bonus = bonus + this.currentClusterLength;
            }
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : cluster ends, next char is word separator", bonus);
            this.positionsWeight = this.positionsWeight - bonus;
            this.clusterEndsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingEndEdges++;
        } else if ( this.isCurrentCharWordSeparator() ) {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -6.6 : cluster ends, current char is word separator");
            this.positionsWeight = this.positionsWeight - 6.6;
            this.clusterEndsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingEndEdges++;
        }
        
        if ( this.clusterStartsWithVariant && this.currentClusterLength > 2 ) {
            
        }

        boolean isClusterLongWord = false;
        if ( this.clusterEndsWithSeparator ) {
            if ( this.clusterStartsWithSeparator ) {
                float bonus = 10.25f;
                if ( this.currentClusterLength > 2 ) {
                    bonus = bonus + (this.currentClusterLength * 2);
                    isClusterLongWord = true;
                } 
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : cluster is a word", bonus);
                this.positionsWeight = this.positionsWeight - bonus;
            } else {
                if ( this.alonePositionBeforePreviousSeparator != POS_UNINITIALIZED && 
                     this.alonePositionBeforePreviousSeparator != POS_ERASED ) {
                    float bonus = 7.25f;
                        if ( this.currentClusterLength > 2 ) {
                        bonus = bonus + (this.currentClusterLength * 2) - (this.clusters.lastAddedCluster().firstPosition() - this.alonePositionBeforePreviousSeparator - 1);
                        isClusterLongWord = true;
                    } 
                    if ( ! containsSeparatorsInVariantInSpan(this.alonePositionBeforePreviousSeparator, this.currentPosition - this.currentClusterLength + 1) ) {
                        logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : char before previous separator and cluster enclosing single word - %s__%s!", 
                                                       bonus, 
                                                       this.alonePositionBeforePreviousSeparator,
                                                       this.clusters.lastAddedCluster().toString());
                        this.positionsWeight = this.positionsWeight - bonus;
                    }                   
                }
            }
        } else {
            if ( this.previousClusterLastPosition > 0 ) {
                if ( ! this.previousClusterEndsWithSeparator && ! this.clusterStartsWithSeparator ) {
                    int distance = this.currentClusterFirstPosition - this.previousClusterLastPosition;

                    if ( distance < this.previousClusterLength + this.currentClusterLength) {
                        boolean containsSeparators = containsSeparatorsInVariantInSpan(
                                this.previousClusterLastPosition, this.currentClusterFirstPosition);
                        if ( ! containsSeparators ) {
                            int improve = this.previousClusterLength + this.currentClusterLength;
                            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : clusters near, are in one part", improve);
                            this.positionsWeight = this.positionsWeight - improve;
                        }
                    }            
                }
            }
        }       
        
        if ( this.currentClusterLength > 2 && this.currentClusterOrdersIsConsistent && ! this.currentClusterOrdersHaveDiffCompensations ) {
            if ( this.patternContainsClusterFoundInVariant() ) {
                int containingReward;
                if ( isClusterLongWord ) {
                    logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] pattern contains cluster and it is a long word!");
                    containingReward = cube(this.currentClusterLength) + square(this.currentClusterLength);
                } else {
                    logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] pattern contains cluster!");                
                    containingReward = cube(this.currentClusterLength);
                }  
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : ^ ", containingReward);
                this.positionsWeight = this.positionsWeight - containingReward;              
            } else {
                logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] pattern DOES NOT contain cluster!");
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : ^ ", this.currentClusterLength);
                this.positionsWeight = this.positionsWeight + this.currentClusterLength;
            }
        }
        
        this.countSeparatorsBetweenClusters();
        
        if ( this.alonePositionBeforePreviousSeparator != POS_UNINITIALIZED ) {
            this.alonePositionBeforePreviousSeparator = POS_ERASED;
        }
        this.previousClusterLastPosition = this.currentPosition;
        this.previousClusterFirstPosition = this.currentClusterFirstPosition;
        this.previousClusterEndsWithSeparator = this.clusterEndsWithSeparator;
        this.previousClusterLength = this.currentClusterLength;
        this.previousClusterOrdersIsConsistent = this.currentClusterOrdersIsConsistent;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        this.clusterEndsWithSeparator = false;
    }
    
    private void countSeparatorsBetweenClusters() {
        if ( this.clustersQty == 1 ) {
            return;
        }
        
        int distanceBetweenTwoClusters = 
                    this.currentClusterFirstPosition - this.previousClusterLastPosition - 1;
        
        switch ( distanceBetweenTwoClusters ) {
            case 0:
                // impossible block
                break;
            case 1:
                if ( this.previousClusterEndsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                break;
            case 2:
                if ( this.previousClusterEndsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                } 
                if ( this.clusterStartsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                break;
            default:
                if ( this.previousClusterEndsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                if ( this.clusterStartsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                this.separatorsBetweenClusters = this.separatorsBetweenClusters + 
                        countWordSeparatorsInBetween(
                                this.data.variantText,
                                this.previousClusterLastPosition + 2, 
                                this.currentClusterFirstPosition - 2);
                break;
        }
    }
    
    private boolean patternContainsClusterFoundInVariant() {
        char first = this.data.variantText.charAt(this.currentClusterFirstPosition);
        char patternChar;
        char clusteredChar;
        
        int clusteredCharPos;
        int patternCharPos;
        
        int variantLength = this.data.variantText.length();
        int patternLength = this.data.patternChars.length;
        
        int j;
        
        boolean found = false;
        
        patternIterating : for (int i = 0; i < this.data.patternChars.length; i++) {
            patternChar = this.data.patternChars[i];
            if ( first == patternChar ) {                
                found = true;
                j = 1;
                
                clusterIterating : for ( ; j < this.currentClusterLength; j++) {
                    clusteredCharPos = this.currentClusterFirstPosition + j;
                    patternCharPos = i + j;
                    
                    if ( clusteredCharPos >= variantLength || patternCharPos >= patternLength ) {
                        found = false;
                        break patternIterating;
                    }
                    
                    clusteredChar = this.data.variantText.charAt(clusteredCharPos);
                    patternChar = this.data.patternChars[patternCharPos];
                    
                    if ( clusteredChar != patternChar ) {
                        found = false;                        
                        break clusterIterating;
                    }
                }
                i = i + j;
                
                if ( found ) {
                    break patternIterating;
                }                
            }
        }
        
        return found;
    }
    
    private boolean containsSeparatorsInVariantInSpan(int fromExcl, int toExcl) {
        for (int i = fromExcl + 1; i < toExcl; i++) {
            if ( isWordsSeparator(data.variantText.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    
    void processClusterPositionOrderStats(String clusterMark) {  
        int currentPositionUnsortedOrder = this.positionUnsortedOrders.get(this.currentPosition);
        int orderDiff = currentPositionUnsortedOrder - (this.currentPositionIndex - this.missed);
        this.currentClusterOrderDiffs.add(orderDiff);
        logAnalyze(
                POSITIONS_CLUSTERS, 
                "    %spos. %s (char '%s') S-Order: %s U-Order: %s orderDiff: %s", 
                clusterMark, 
                this.currentPosition, 
                this.data.variantText.charAt(this.currentPosition),
                (this.currentPositionIndex - this.missed), 
                currentPositionUnsortedOrder, 
                orderDiff);
    }
    
    private int consistencyRewardDependingOnCurrentClusterLength() {
        return this.currentClusterLength;
    }
    
    void accumulateClusterPositionOrdersStats() {
        if ( this.currentClusterOrderDiffs.isEmpty() ) {
            int consistencyReward = this.consistencyRewardDependingOnCurrentClusterLength();
            logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster is consistent");
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : for consistency", consistencyReward);
            this.positionsWeight = this.positionsWeight - consistencyReward;
            this.currentClusterOrdersIsConsistent = true;
            this.currentClusterOrdersHaveDiffCompensations = false;
            return;
        }
        Cluster cluster = calculateCluster(
                this.currentClusterOrderDiffs,                 
                this.currentClusterFirstPosition,
                this.currentClusterLength);

        this.currentClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();
        this.currentClusterOrdersHaveDiffCompensations = cluster.haveOrdersDiffCompensations();

        if ( cluster.hasOrdersDiff() ) {
            boolean teardown = this.tryToTearDown(cluster);
            if ( ! teardown ) {
                int incosistency = inconsistencyOf(cluster, this.currentClusterLength);
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : for inconsistency", incosistency);
                this.positionsWeight = this.positionsWeight + incosistency;
            }            
        } else {
            if ( cluster.hasOrdersDiffShifts() ) {
                double shiftDeviation;
                if ( cluster.ordersDiffShifts() == this.currentClusterLength ) {
                    shiftDeviation = square(cluster.ordersDiffShifts());
                } else {
                    shiftDeviation = cluster.ordersDiffShifts() * onePointRatio(cluster.ordersDiffShifts(), this.currentClusterLength);
                }
                logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster has %s shifts", cluster.ordersDiffShifts());
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : for shifts", shiftDeviation);
                this.positionsWeight = this.positionsWeight + shiftDeviation;    
            } else {
                boolean teardown = this.tryToTearDown(cluster);
                if ( ! teardown ) {
                    if ( this.currentClusterLength == 2 ) {
                        // no reward
                    } else {
                        int consistencyReward = this.consistencyRewardDependingOnCurrentClusterLength();
                        logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] cluster is consistent");
                        logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : for consistency", consistencyReward);
                        this.positionsWeight = this.positionsWeight - consistencyReward;  
                    }  
                }                
            }
        }
        
        this.previousClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();
        
        this.currentClusterOrderDiffs.clear();  
        this.clusters.add(cluster);
    }
    
    private boolean tryToTearDown(Cluster cluster) {
        if ( cluster.compensationSum() > cluster.length() ) {
            this.tearDownOn(cluster.length());
            return true;
        } else {
            if ( cluster.ordersDiffCount() == 0 && cluster.ordersDiffSum() == 0 ) {
                return false;
            } else if ( cluster.ordersDiffCount() > 0 && cluster.ordersDiffSum() == 0 ) {
                return this.tryToTearDownBasingOnDiffCountOnly(cluster);
            } else if ( cluster.ordersDiffSum() > 0 && cluster.ordersDiffCount() == 0 ) {
                return this.tryToTearDownBasingOnDiffSumOnly(cluster);
            } else {
                return this.tryToTearDownBasingOnDiffSumAndCount(cluster);
            }
        }        
    }
    
    private static boolean considerDiffCountCompensationWhen(int clusterLength, int patternLength) {
        boolean tolerate = true;
        
        if ( clusterLength <= patternLength / 2 ) {
            return true;
        }
        
        if ( clusterLength < 4 ) {
            return false;
        }
        
        return tolerate;
    }
    
    private boolean tryToTearDownBasingOnDiffCountOnly(Cluster cluster) {
        boolean teardown = false;
        
        if ( cluster.ordersDiffCount() > cluster.length() / 2 ) {
            if ( considerDiffCountCompensationWhen(cluster.length(), data.pattern.length()) ) {
                if ( cluster.compensationSum() < cluster.ordersDiffCount() ) {
                    this.tearDownOn(cluster.ordersDiffCount() - cluster.compensationSum());
                    teardown = true;
                }
            } else {
                this.tearDownOn(cluster.ordersDiffCount());
                teardown = true;
            }            
        } else {
            if ( cluster.haveOrdersDiffCompensations() ) {
                if ( cluster.compensationSum() < cluster.ordersDiffCount() ) {
                    this.tearDownOn(cluster.ordersDiffCount() - cluster.compensationSum());
                    teardown = true;
                }
            }
        }
        
        return teardown;
    }
    
    private boolean tryToTearDownBasingOnDiffSumOnly(Cluster cluster) {
        this.tearDownOn(cluster.ordersDiffSum());
        return true;
    }
    
    private boolean tryToTearDownBasingOnDiffSumAndCount(Cluster cluster) {
        int tearDown = cluster.ordersDiffCount();
        
        if ( cluster.haveOrdersDiffCompensations() ) {
            if ( considerDiffCountCompensationWhen(cluster.length(), data.pattern.length()) ) {
                tearDown = tearDown - cluster.compensationSum();
            }
        }       
        
        this.tearDownOn(tearDown);
        return true;
    }
    
    private void tearDownOn(int positionsQty) {
        positionsQty = abs(positionsQty);
        this.clustered = this.clustered - positionsQty;
        this.nonClustered = this.nonClustered + positionsQty;
        logAnalyze(POSITIONS_CLUSTERS, "               [TEARDOWN] cluster is to be teardown by %s", positionsQty);
    }

    boolean isCurrentAndNextPositionInCluster() {
        return 
                this.currentPosition == this.nextPosition - 1 &&
                this.positionFoundSteps.get(this.currentPosition).foundPositionCanBeClustered();
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
        this.nonClusteredImportance = nonClusteredImportanceDependingOn(
                this.nonClustered, this.missed, this.data.patternChars.length);
        this.missedImportance = missedImportanceDependingOn(
                this.missed, 
                this.clustersImportance,
                this.data.patternChars.length,
                this.data.variantText.length());
        logAnalyze(POSITIONS_CLUSTERS, "    [importance] clusters: %s", this.clustersImportance);
        logAnalyze(POSITIONS_CLUSTERS, "    [importance] non-clustered: %s", this.nonClusteredImportance);
        logAnalyze(POSITIONS_CLUSTERS, "    [importance] missed: %s", this.missedImportance);
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
        return this.data.patternChars[0] == this.data.variantText.charAt(this.currentPosition);
    }
    
    private boolean currentPositionCharIsDifferentFromFirstFoundPositionChar() {
        return 
                this.data.variantText.charAt(this.currentPosition) != 
                this.data.variantText.charAt(this.positions[0 + this.missed]);
    }
    
    final void clearPositionsAnalyze() {
        this.positions = null;
        this.missed = 0;
        this.clustersQty = 0;
        this.clustered = 0;
        this.nonClustered = 0;
        this.currentClusterLength = 0;
        this.previousClusterLength = 0;
        this.clusterContinuation = false;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        this.clusterEndsWithSeparator = false;
        this.previousClusterEndsWithSeparator = false;
        this.clustersFacingEdges = 0;
        this.clustersFacingStartEdges = 0;
        this.clustersFacingEndEdges = 0;
        this.separatorsBetweenClusters = 0;
        this.currentPosition = POS_UNINITIALIZED;
        this.currentPositionIndex = POS_UNINITIALIZED;
        this.nextPosition = POS_UNINITIALIZED;
        this.alonePositionBeforePreviousSeparator = POS_UNINITIALIZED;
        this.prevCharIsSeparator = false;
        this.nextCharIsSeparator = false;
        this.currentPatternCharPositionInVariant = POS_UNINITIALIZED;
        this.missedImportance = 0;
        this.clustersImportance = 0;
        this.nonClusteredImportance = 0;
        this.positionsWeight = 0;
        this.previousClusterLastPosition = POS_UNINITIALIZED;
        this.previousClusterFirstPosition = POS_UNINITIALIZED;
        this.currentClusterFirstPosition = POS_UNINITIALIZED;
        this.badReason = NO_REASON;
        this.currentChar = ' ';
        this.patternInVariantLength = 0;
        this.skipNextPatternChar = false;
        this.positionUnsortedOrders.clear();
        this.positionPatternIndexes.clear();
        this.positionFoundSteps.clear();
        this.positionCandidate.clear();
        this.nearestPositionInVariant = POS_UNINITIALIZED;
        this.currentClusterOrderDiffs.clear();
        this.allClustersInconsistency = 0;
        this.clusters.clear();
        this.keyChars.clear();
        this.currentClusterOrdersIsConsistent = false;
        this.previousClusterOrdersIsConsistent = false;
        this.currentClusterOrdersHaveDiffCompensations = false;
        this.unsortedPositions = 0;
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
        if ( ! this.positionUnsortedOrders.isEmpty() ) {
            this.positionUnsortedOrders.clear();
        }
        int position;
        int previousPosition = POS_UNINITIALIZED;
        int notFoundOrderOffset = 0;
        for (int i = 0; i < this.positions.length; i++) {
            position = this.positions[i];
            if ( position > -1 ) {
                this.positionUnsortedOrders.put(position, i - notFoundOrderOffset);
                if ( previousPosition != POS_UNINITIALIZED ) {
                    if ( previousPosition > position ) {
                        this.unsortedPositions++;
                    }
                }
                previousPosition = position;
            } else {
                notFoundOrderOffset++;
            }
        }
        Arrays.sort(this.positions);
    }
    
}
