/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import diarsid.beam.core.base.analyze.variantsweight.StepTwoSubclusterCandidate.PositionView;
import diarsid.support.objects.Possible;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_SEARCH;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.inconsistencyOf;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.nonClusteredImportanceDependingOn;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.processCluster;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_1;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_2;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_3;
import static diarsid.beam.core.base.analyze.variantsweight.FindPositionsStep.STEP_4;
import static diarsid.beam.core.base.analyze.variantsweight.MatchType.MATCH_DIRECTLY;
import static diarsid.beam.core.base.analyze.variantsweight.MatchType.MATCH_TYPO_1;
import static diarsid.beam.core.base.analyze.variantsweight.MatchType.MATCH_TYPO_2;
import static diarsid.beam.core.base.analyze.variantsweight.MatchType.MATCH_TYPO_3_1;
import static diarsid.beam.core.base.analyze.variantsweight.MatchType.MATCH_TYPO_3_2;
import static diarsid.beam.core.base.analyze.variantsweight.MatchType.MATCH_TYPO_3_3;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.BAD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimate;
import static diarsid.beam.core.base.util.CollectionsUtils.first;
import static diarsid.beam.core.base.util.CollectionsUtils.getNearestToValueFromSetExcluding;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.MathUtil.cube;
import static diarsid.beam.core.base.util.MathUtil.onePointRatio;
import static diarsid.beam.core.base.util.MathUtil.square;
import static diarsid.support.objects.Possibles.possibleButEmpty;
import static diarsid.support.strings.StringUtils.countWordSeparatorsInBetween;
import static diarsid.support.strings.StringUtils.isWordsSeparator;

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
    
    /* DEBUG UTIL */ static interface DebugCondition {
    /* DEBUG UTIL */     
    /* DEBUG UTIL */     boolean isMatch();
    /* DEBUG UTIL */ }
    /* DEBUG UTIL */
    /* DEBUG UTIL */ static boolean gotoBreakpointWhen(DebugCondition debugCondition) {
    /* DEBUG UTIL */     return debugCondition.isMatch();
    /* DEBUG UTIL */ }
    /* DEBUG UTIL */ 
    /* DEBUG UTIL */ static void breakpoint() {
    /* DEBUG UTIL */        
    /* DEBUG UTIL */ }
    /* DEBUG UTIL */ 
    /* DEBUG UTIL */ DebugCondition charAndPositionAre(char c, int position) {
    /* DEBUG UTIL */     return () -> {
    /* DEBUG UTIL */         return 
    /* DEBUG UTIL */                 this.currentPatternCharPositionInVariant == position &&
    /* DEBUG UTIL */                 this.currentChar == c;
    /* DEBUG UTIL */     };
    /* DEBUG UTIL */ }
    
    final AnalyzeData data;
    final AnalyzePositionsDirection direction;
    
    int[] positions;
    
    int clustersQty;
    int clustered;
    int nonClustered;
    
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
    Set<Integer> localUnclusteredPatternCharIndexes = new TreeSet<>();
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
    int nextPatternCharsToSkip;
    List<Integer> positionsInCluster = new ArrayList<>();
    int currentCharInVariantQty;
    int currentPatternCharPositionInVariantToSave;
    // --
    
    StepOneSubclusterCandidate currentClusterCandidate = new StepOneSubclusterCandidate();
    StepOneSubclusterCandidate prevClusterCandidate = new StepOneSubclusterCandidate();
    
    StepTwoSubclusterCandidate currentPositionCandidate = new StepTwoSubclusterCandidate();
    StepTwoSubclusterCandidate prevPositionCandidate = new StepTwoSubclusterCandidate();
    
    Possible<String> missedRepeatingsLog = possibleButEmpty();
    List<Integer> extractedMissedRepeatedPositionsIndexes = new ArrayList<>();
    List<Character> missedRepeatedChars = new ArrayList<>();
    List<Integer> missedRepeatedPositions = new ArrayList<>();
    
    // v.3
    Map<Integer, Integer> positionUnsortedOrders = new HashMap<>();
    Map<Integer, Integer> positionPatternIndexes = new HashMap<>();
    Map<Integer, FindPositionsStep> positionFoundSteps = new HashMap<>();
    Set<Integer> filledPositions = positionFoundSteps.keySet();
    private final PositionCandidate positionCandidate;
    int nearestPositionInVariant;
    List<Integer> currentClusterOrderDiffs = new ArrayList();
    List<Character> notFoundPatternChars = new ArrayList<>();
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
    
    AnalyzePositionsData(
            AnalyzeData data, 
            AnalyzePositionsDirection direction, 
            Clusters clusters, 
            PositionCandidate positionCandidate) {
        this.data = data;
        this.clusters = clusters;
        this.positionCandidate = positionCandidate;
        this.direction = direction;
        this.keyChars = new TreeSet<>();
        this.clearPositionsAnalyze();
    }
    
    static boolean arePositionsEquals(AnalyzePositionsData dataOne, AnalyzePositionsData dataTwo) {
        return Arrays.equals(dataOne.positions, dataTwo.positions);
    }
    
    int findFirstPosition() {
        int first = first(this.positions);
        if ( first > -1 ) {
            return first;
        }
        
        for (int i = 1; i < this.positions.length; i++) {
            first = this.positions[i];
            if ( first > -1 ) {
                return first;
            }
        }
        
        return POS_NOT_FOUND;
    }
    
    int findLastPosition() {
        int last = last(this.positions);
        if ( last > -1 ) {
            return last;
        }
        
        for (int i = this.positions.length - 2; i > -1; i--) {
            last = this.positions[i];
            if ( last > -1 ) {
                return last;
            } 
        }
        
        return POS_NOT_FOUND;
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
                
        this.clusters.arrange();
        
        if ( this.clusters.nonEmpty() ) {
            this.analyzeAllClustersOrderDiffs();
            int totalTearDown = this.clusters.lookupForTearDowns();
            if ( totalTearDown > 0 ) {
                this.clustered = this.clustered - totalTearDown;
                this.nonClustered = this.nonClustered + totalTearDown;
                logAnalyze(POSITIONS_CLUSTERS, "               [TEARDOWN] total : %s", totalTearDown);
            }            
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
//                logAnalyze(POSITIONS_CLUSTERS, "               [weight] -15.5 : there is cluster before separator!");
//                this.positionsWeight = this.positionsWeight - 15.5;
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
        
        fillNotFoundPositions();
        
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
    
    private void fillNotFoundPositions() {
        for (Character character : data.patternChars) {
            this.notFoundPatternChars.add(character);
        }
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
        this.notFoundPatternChars.clear();
        this.unclusteredPatternCharIndexes = null;
        this.previousPositionInVariantFound = false;
        this.nextPositionInVariantFound = false;
        this.hasPreviousInPattern = false;
        this.hasNextInPattern = false;
        this.hasPreviousInVariant = false;
        this.hasNextInVariant = false;
        this.positionAlreadyFilled = false;
        this.positionsInCluster.clear();
        this.currentChar = ' ';
        this.findPositionsStep = STEP_1;
    }
    
    private boolean isPositionSetAt(int patternIndex) {
        return this.positions[patternIndex] > -1;
    }
    
    private void processCurrentPatternCharOf(int currentPatternCharIndex, int charsRemained) {
        currentChar = data.patternChars[currentPatternCharIndex];
        logAnalyze(POSITIONS_SEARCH, "      [explore] '%s'(%s in pattern)", this.currentChar, currentPatternCharIndex);
        /* EXP BREAKING */ if ( nextPatternCharsToSkip > 0 ) {
        /* EXP BREAKING */     logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in pattern) is skipped!", this.currentChar, currentPatternCharIndex);
        /* EXP BREAKING */     nextPatternCharsToSkip--;
        /* EXP BREAKING */     return;
        /* EXP BREAKING */ }
        
        /* EXP */ //if ( prevClusterCandidate.skipIfPossible() ) {
        /* EXP */ //    logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in pattern) is skipped!", this.currentChar, currentPatternCharIndex);
        /* EXP */ //    return;
        /* EXP */ //}
        
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
        
        positionsInCluster.clear();
        continueSearching = true;

        characterSearching : while ( currentPatternCharPositionInVariant >= 0 && continueSearching ) {
            logAnalyze(POSITIONS_SEARCH, "        [assess] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
            currentPositionCandidate.setAssessed(currentChar, currentPatternCharIndex, currentPatternCharPositionInVariant);
        
            nearestPositionInVariant = POS_UNINITIALIZED;

            hasPreviousInVariant = currentPatternCharPositionInVariant > 0;
            hasNextInVariant = currentPatternCharPositionInVariant < data.variantText.length() - 1;

            currentCharInVariantQty++;
            positionAlreadyFilled = filledPositions.contains(currentPatternCharPositionInVariant);
            positionsInCluster.clear();

            if ( ! positionAlreadyFilled ) {
                
                if ( gotoBreakpointWhen(charAndPositionAre('p', 22)) ) {
                    breakpoint();
                }
                
                if ( hasPreviousInPattern && hasPreviousInVariant ) {
                    previousPositionInVariantFound = filledPositions.contains(currentPatternCharPositionInVariant - 1);
                    /* OLD BREAKING */ // if ( previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) || previousPositionInVariantFound ) {
                    if ( previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) ) {
                        if ( ! previousPositionInVariantFound && ! notFoundPatternChars.contains(this.previousCharInVariant) ) {
                            // omit this match
                        } else {
                            logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                                    previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                            positionsInCluster.add(this.currentPatternCharPositionInVariant - 1);
                            /* EXP */ currentPositionCandidate.add(
                            /* EXP */         previousCharInVariant,
                            /* EXP */         currentPatternCharIndex - 1,
                            /* EXP */         currentPatternCharPositionInVariant - 1, 
                            /* EXP */         previousPositionInVariantFound, 
                            /* EXP */         MATCH_DIRECTLY);
                            nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            /* POSSIBLE DEAD CODE */ // if ( findPositionsStep.equals(STEP_1) ) {
                            /* POSSIBLE DEAD CODE */ //     logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                            /* POSSIBLE DEAD CODE */ //             previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                            /* POSSIBLE DEAD CODE */ //     charsInClusterQty++;
                            /* POSSIBLE DEAD CODE */ //     nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            /* POSSIBLE DEAD CODE */ // } else {
                            /* POSSIBLE DEAD CODE */ //     if ( direction.equals(FORWARD) ) {
                            /* POSSIBLE DEAD CODE */ //         if ( previousPositionInVariantFound ) {
                            /* POSSIBLE DEAD CODE */ //             logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                            /* POSSIBLE DEAD CODE */ //                     previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                            /* POSSIBLE DEAD CODE */ //             charsInClusterQty++;
                            /* POSSIBLE DEAD CODE */ //             nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            /* POSSIBLE DEAD CODE */ //         }
                            /* POSSIBLE DEAD CODE */ //     } else {
                            /* POSSIBLE DEAD CODE */ //         logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                            /* POSSIBLE DEAD CODE */ //                 previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                            /* POSSIBLE DEAD CODE */ //         charsInClusterQty++;
                            /* POSSIBLE DEAD CODE */ //         nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            /* POSSIBLE DEAD CODE */ //     }                            
                            /* POSSIBLE DEAD CODE */ // }  
                        }                                              
                    }
                }
                if ( hasNextInPattern && hasNextInVariant ) {
                    boolean nextCharInVariantIncluded = filledPositions.contains(currentPatternCharPositionInVariant + 1) || isPositionSetAt(currentPatternCharIndex + 1);
                    /* OLD BREAKING */ // if ( nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) || nextCharInVariantIncluded ) {
                    if ( nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) ) {
                        logAnalyze(POSITIONS_SEARCH, "          [info] next '%s'(%s in variant) is in cluster with current '%s'", 
                                nextCharInVariant, currentPatternCharPositionInVariant + 1, currentChar);
                        positionsInCluster.add(this.currentPatternCharPositionInVariant + 1);                        
                        /* EXP */ currentPositionCandidate.add(
                        /* EXP */         nextCharInVariant,
                        /* EXP */         currentPatternCharIndex + 1,
                        /* EXP */         currentPatternCharPositionInVariant + 1, 
                        /* EXP */         nextCharInVariantIncluded,
                        /* EXP */         MATCH_DIRECTLY);
                        nearestPositionInVariant = currentPatternCharPositionInVariant + 1;
                    }
                }
                
                if ( findPositionsStep.typoSearchingAllowed() ) {
                    /* EXP BREAKING */ //int typosFound = 0;                    
                    boolean distanceOneTypoFound = false;
                    
                    if ( hasPreviousInPattern && hasNextInVariant ) {
                        
                        previousCharInPattern = data.patternChars[currentPatternCharIndex - 1];
                        nextCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant + 1);
                        
                        if ( previousCharInPattern == nextCharInVariant ) {
                            boolean patternOfTypoFilled = isPositionSetAt(currentPatternCharIndex - 1);
                            boolean variantOfTypeFilled = filledPositions.contains(currentPatternCharPositionInVariant + 1);                        
                            boolean respectMatch = true;
                            if ( patternOfTypoFilled && (! variantOfTypeFilled) ) {
                                respectMatch = notFoundPatternChars.contains(previousCharInPattern);
                            } 
                            if ( respectMatch ) {
                                logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and next in variant", 
                                        currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                                /* EXP BREAKING */ //typosFound++;
                                positionsInCluster.add(currentPatternCharPositionInVariant + 1);
                                /* EXP */ currentPositionCandidate.add(
                                /* EXP */         nextCharInVariant,
                                /* EXP */         currentPatternCharIndex - 1,
                                /* EXP */         currentPatternCharPositionInVariant + 1, 
                                /* EXP */         variantOfTypeFilled || patternOfTypoFilled,
                                /* EXP */         MATCH_TYPO_1);
                                distanceOneTypoFound = true;
                                nearestPositionInVariant = currentPatternCharPositionInVariant + 1;
                            }                            
                        }
                    }
                    if ( hasPreviousInVariant && hasNextInPattern ) {

                        previousCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant - 1);
                        nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];

                        if ( previousCharInVariant == nextCharInPattern ) {
                            if ( filledPositions.contains(currentPatternCharPositionInVariant - 1) || notFoundPatternChars.contains(nextCharInPattern) ) {
                                logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next in pattern and previous in variant", 
                                        currentChar, currentPatternCharPositionInVariant, nextCharInPattern);
                                /* EXP BREAKING */ //typosFound++;
                                positionsInCluster.add(currentPatternCharPositionInVariant - 1);
                                /* EXP */ currentPositionCandidate.add(
                                /* EXP */         previousCharInVariant,
                                /* EXP */         currentPatternCharIndex + 1,
                                /* EXP */         currentPatternCharPositionInVariant - 1, 
                                /* EXP */         filledPositions.contains(currentPatternCharPositionInVariant + 1) || isPositionSetAt(currentPatternCharIndex + 1),
                                /* EXP */         MATCH_TYPO_1);
                                distanceOneTypoFound = true;
                                nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            }                            
                        }
                    }
                    /* EXP BREAKING */ //if ( ! distanceOneTypoFound ) {
                    /* EXP */if ( true ) {
                        
                        boolean nextNextFoundAsTypo = false;
                        if ( hasNextInPattern && hasNextInVariant ) {
                            nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];
                            // if there are at least two characters ahead in variant...
                            if ( data.variantText.length() - currentPatternCharPositionInVariant > 2 ) {
                                int nextNextPosition = currentPatternCharPositionInVariant + 2;
                                if ( ! data.variantSeparators.contains(nextNextPosition - 1) ) {
                                    char nextNextCharInVariant = data.variantText.charAt(nextNextPosition);
                                    if ( nextCharInPattern == nextNextCharInVariant ) {
                                        boolean nextNextPositionIncluded = filledPositions.contains(nextNextPosition);
                                        if ( nextNextPositionIncluded || notFoundPatternChars.contains(nextCharInPattern) ) {
                                            logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next in pattern and next*2 in variant", 
                                                    currentChar, currentPatternCharPositionInVariant, nextCharInPattern);
                                            /* EXP BREAKING */ //typosFound++;
                                            positionsInCluster.add(nextNextPosition);
                                            /* EXP */ currentPositionCandidate.add(
                                            /* EXP */         nextNextCharInVariant,
                                            /* EXP */         currentPatternCharIndex + 1,
                                            /* EXP */         nextNextPosition, 
                                            /* EXP */         nextNextPositionIncluded,
                                            /* EXP */         MATCH_TYPO_2);
                                            nearestPositionInVariant = nextNextPosition;
                                            nextNextFoundAsTypo = true;
                                            
                                            /* EXP */ if ( data.pattern.length() - currentPatternCharIndex > 2 && 
                                            /* EXP */      data.variantText.length() - currentPatternCharPositionInVariant > 3) {
                                            /* EXP */     char next2CharInPattern = data.patternChars[currentPatternCharIndex + 2];
                                            /* EXP */     int next3Position = currentPatternCharPositionInVariant + 3;
                                            /* EXP */     char next3CharInVariant = data.variantText.charAt(next3Position);
                                            /* EXP */     if ( next2CharInPattern == next3CharInVariant ) {
                                            /* EXP */         positionsInCluster.add(next3Position);
                                            /* EXP */         currentPositionCandidate.add(
                                            /* EXP */                  next2CharInPattern,
                                            /* EXP */                  currentPatternCharIndex + 2,
                                            /* EXP */                  next3Position, 
                                            /* EXP */                  filledPositions.contains(next3Position) || isPositionSetAt(currentPatternCharIndex + 2),
                                            /* EXP */                  MATCH_TYPO_3_2);
                                            
                                            if ( data.pattern.length() - currentPatternCharIndex > 3 ) {
                                                char next3CharInPattern = data.patternChars[currentPatternCharIndex + 3];
                                                if ( next3CharInPattern == nextCharInVariant ) {
                                                    boolean needToInclude = notFoundPatternChars.contains(nextCharInVariant);
                                                    if ( needToInclude ) {
                                                        boolean isAlreadyIncluded = 
                                                                filledPositions.contains(currentPatternCharPositionInVariant + 1) || 
                                                                isPositionSetAt(currentPatternCharIndex + 3);
                                                        positionsInCluster.add(currentPatternCharPositionInVariant + 1);
                                                        currentPositionCandidate.add(
                                                                next3CharInPattern,
                                                                currentPatternCharIndex + 3,
                                                                currentPatternCharPositionInVariant + 1,
                                                                isAlreadyIncluded,
                                                                MATCH_TYPO_3_3);
                                                    }                                                    
                                                }
                                            }
                                            /* EXP */     }
                                            
                                            
                                            
                                            /* EXP */ }
                                        }                                         
                                    }
                                }
                                
                                if ( data.pattern.length() - currentPatternCharIndex > 2 ) {
                                    char next2CharInPattern = data.patternChars[currentPatternCharIndex + 2];
                                    if ( nextCharInVariant == next2CharInPattern ) {
                                        if ( notFoundPatternChars.contains(next2CharInPattern) ) {
                                            logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next*2 in pattern and next in variant", 
                                                    currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                                            if ( nonEmpty(positionsInCluster) ) {
                                                positionsInCluster.add(currentPatternCharPositionInVariant + 1);
                                                /* EXP */ currentPositionCandidate.add(
                                                /* EXP */         next2CharInPattern,
                                                /* EXP */         currentPatternCharIndex + 2,
                                                /* EXP */         currentPatternCharPositionInVariant + 1, 
                                                /* EXP */         filledPositions.contains(currentPatternCharPositionInVariant + 1),
                                                /* EXP */         MATCH_TYPO_2);

                                                if ( data.pattern.length() - currentPatternCharIndex > 3 && 
                                                     data.variantText.length() - currentPatternCharPositionInVariant > 3 ) {
                                                    int next3Position = currentPatternCharPositionInVariant + 3;
                                                    char next3CharInPattern = data.patternChars[currentPatternCharIndex + 3];
                                                    char next3CharInVariant = data.variantText.charAt(next3Position);
                                                    if ( next3CharInPattern == next3CharInVariant ) {
                                                        boolean next3PositionIncluded = filledPositions.contains(next3Position);
                                                        if ( next3PositionIncluded || notFoundPatternChars.contains(next3CharInVariant) ) {
                                                            logAnalyze(POSITIONS_SEARCH, "          [info] cluster continuation '%s'(%s in variant) found", 
                                                                    next3CharInVariant, currentPatternCharPositionInVariant + 3);
                                                            positionsInCluster.add(currentPatternCharPositionInVariant + 3);
                                                            /* EXP */ currentPositionCandidate.add(/* EXP */         next3CharInVariant,
                                                            /* EXP */         currentPatternCharIndex + 3,
                                                            /* EXP */         next3Position, 
                                                            /* EXP */         next3PositionIncluded,
                                                            /* EXP */         MATCH_TYPO_3_1);
                                                        } 
                                                    }
                                                }
                                            }                                            
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
                                    if ( previousCharInPattern == data.variantText.charAt(prevPrevPosition) ) {
                                        boolean prevPrevPositionIncluded = filledPositions.contains(prevPrevPosition);
                                        if ( prevPrevPositionIncluded || notFoundPatternChars.contains(previousCharInPattern) ) {
                                            logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and previous*2 in variant", 
                                                    currentChar, currentPatternCharPositionInVariant, previousCharInPattern);
                                            /* EXP BREAKING */ //typosFound++;
                                            positionsInCluster.add(prevPrevPosition);
                                            /* EXP */ currentPositionCandidate.add(/* EXP */         previousCharInPattern,
                                            /* EXP */         currentPatternCharIndex - 1,
                                            /* EXP */         prevPrevPosition, 
                                            /* EXP */         prevPrevPositionIncluded,
                                            /* EXP */         MATCH_TYPO_3_1);
                                            nearestPositionInVariant = prevPrevPosition;
                                        }                                        
                                    }
                                }                                                               
                            }
                        }
                    }
                    
                    /* EXP BREAKING */ //if ( positionsInCluster == 0 && typosFound > 0 ) {
                    /* EXP BREAKING */ //    positionsInCluster = 1;
                    /* EXP BREAKING */ //}
                }

                if ( findPositionsStep.equals(STEP_1) ) {
                    if ( findPositionsStep.canAddToPositions(positionsInCluster.size()) ) {
                        /* EXP do not save positions directly, just fill them in candidate-to-save positions List. */ 
                        /* EXP */ currentClusterCandidate.setMain(currentPatternCharIndex, currentPatternCharPositionInVariant);
                        isCurrentCharPositionAddedToPositions = true;
                        /* EXP BREAKING */ //fillPosition(currentPatternCharIndex, currentPatternCharPositionInVariant);
                        
                        logAnalyze(POSITIONS_SEARCH, "        [STEP_1 SAVE CANDIDATE] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
                        logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
                        logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variantText);
                        
                        if ( canFillPosition(currentPatternCharIndex - 1, currentPatternCharPositionInVariant - 1) ) {     
                            /* EXP */ currentClusterCandidate.setPrev(currentPatternCharPositionInVariant - 1);
                            /* EXP BREAKING */ //fillPosition(currentPatternCharIndex - 1, currentPatternCharPositionInVariant - 1);
                            logAnalyze(POSITIONS_SEARCH, "          [STEP_1 SAVE CANDIDATE] '%s'(%s in variant) is previous both in pattern and variant", previousCharInVariant, currentPatternCharPositionInVariant - 1);
                            logAnalyze(POSITIONS_SEARCH, "                 %s", displayPositions());
                            logAnalyze(POSITIONS_SEARCH, "                 %s : %s", data.pattern, data.variantText);
                            if ( direction.equals(REVERSE) ) {
                                /* EXP BREAKING */ // nextPatternCharsToSkip++;
                                /* EXP */ currentClusterCandidate.incrementSkip();
                            }
                            
                            /* EXP */ int i = 2;
                            /* EXP */ step1BackwardLoop: while ( positionsExistAndCanFillPosition(currentPatternCharIndex - i, currentPatternCharPositionInVariant - i) ) {                                
                            /* EXP */     char patChar = data.pattern.charAt(currentPatternCharIndex - i);
                            /* EXP */     char varChar = data.variantText.charAt(currentPatternCharPositionInVariant - i);
                            /* EXP */     if ( patChar == varChar ) {
                            /* EXP */         if ( direction.equals(REVERSE) ) {
                            /* EXP */             currentClusterCandidate.incrementSkip();
                            /* EXP */         }
                            /* EXP */         currentClusterCandidate.addPrev(currentPatternCharPositionInVariant - i);    
                            /* EXP */         logAnalyze(POSITIONS_SEARCH, "          [EXP STEP_1 SAVE CANDIDATE] '%s'(%s in variant) precigind <<<", varChar, currentPatternCharPositionInVariant - i);
                            /* EXP */     } else {
                            /* EXP */         break step1BackwardLoop;    
                            /* EXP */     }
                            /* EXP */     i++;
                            /* EXP */ }
                        }
                        
                        if ( canFillPosition(currentPatternCharIndex + 1, currentPatternCharPositionInVariant + 1) ) {       
                            /* EXP */ currentClusterCandidate.setNext(currentPatternCharPositionInVariant + 1);                          
                            /* EXP BREAKING */ //fillPosition(currentPatternCharIndex + 1, currentPatternCharPositionInVariant + 1);
                            logAnalyze(POSITIONS_SEARCH, "          [STEP_1 SAVE CANDIDATE] '%s'(%s in variant) is next both in pattern and variant", nextCharInVariant, currentPatternCharPositionInVariant + 1);
                            logAnalyze(POSITIONS_SEARCH, "                 %s", displayPositions());
                            logAnalyze(POSITIONS_SEARCH, "                 %s : %s", data.pattern, data.variantText);
                            if ( direction.equals(FORWARD) ) {
                                /* EXP BREAKING */ // nextPatternCharsToSkip++;
                                /* EXP */ currentClusterCandidate.incrementSkip();
                            }
                            
                            /* EXP */ int i = 2;
                            /* EXP */ step1ForwardLoop: while ( positionsExistAndCanFillPosition(currentPatternCharIndex + i, currentPatternCharPositionInVariant + i) ) {  
                            /* EXP */     char patChar = data.pattern.charAt(currentPatternCharIndex + i);
                            /* EXP */     char varChar = data.variantText.charAt(currentPatternCharPositionInVariant + i);
                            /* EXP */     if ( patChar == varChar ) {
                            /* EXP */         if ( direction.equals(FORWARD) ) {
                            /* EXP */             currentClusterCandidate.incrementSkip();
                            /* EXP */         }
                            /* EXP */         currentClusterCandidate.addNext(currentPatternCharPositionInVariant + i);      
                            /* EXP */         logAnalyze(POSITIONS_SEARCH, "          [EXP STEP_1 SAVE CANDIDATE] '%s'(%s in variant) following >>>", varChar, currentPatternCharPositionInVariant + i);
                            /* EXP */     } else {
                            /* EXP */         break step1ForwardLoop;    
                            /* EXP */     }
                            /* EXP */     i++;
                            /* EXP */ }
                        }                        
                        
                        /* EXP BREAKING */ //continueSearching = false;
                    }
                /* EXP BREAKING */ } else if ( findPositionsStep.typoSearchingAllowed()) {
                /* EXP BREAKING */     // do nothing                   
                /* EXP BREAKING */ } else {    
                    // on steps, other than STEP_1, do not save positions directly, just record them as appropriate for saving (excluding STEP_4).
                    if ( findPositionsStep.canAddToPositions(positionsInCluster.size()) ) {
                        
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
                        if ( orderDiffInVariant == POS_UNINITIALIZED && nonEmpty(filledPositions) ) {
                            int nearestFilledPosition = findNearestFilledPositionTo(currentPatternCharIndex);
                            orderDiffInVariant = absDiff(currentPatternCharPositionInVariant, nearestFilledPosition);
                        }     
                        
                        boolean isNearSeparator = 
                                currentPatternCharPositionInVariant == 0 ||
                                currentPatternCharPositionInVariant == data.variantText.length() - 1 ||
                                data.variantSeparators.contains(currentPatternCharPositionInVariant + 1) ||
                                data.variantSeparators.contains(currentPatternCharPositionInVariant - 1);
                        Integer nearestFilledPosition = getNearestToValueFromSetExcluding(currentPatternCharPositionInVariant, filledPositions);
                        Integer distanceToNearestFilledPosition = null;
                        if ( nonNull(nearestFilledPosition) ) {
                            distanceToNearestFilledPosition = absDiff(nearestFilledPosition, currentPatternCharPositionInVariant);
                        }
                        positionCandidate.tryToMutate(currentPatternCharPositionInVariant, 
                                currentPatternCharIndex,
                                orderDiffInVariant, 
                                orderDiffInPattern, 
                                positionsInCluster.size(),
                                isNearSeparator,
                                distanceToNearestFilledPosition,
                                charsRemained);
                    }
                }                
            } else {
                logAnalyze(POSITIONS_SEARCH, "          [info] already filled, skip");
            } 
            
            if ( findPositionsStep.equals(STEP_1) ) {
                if ( currentClusterCandidate.isSet() ) {
                    if ( prevClusterCandidate.isSet() ) {
                        if ( currentClusterCandidate.isBetterThan(prevClusterCandidate) ) {
                            logAnalyze(POSITIONS_SEARCH, "        [EXP CLUSTER CANDIDATE] %s is better than %s", currentClusterCandidate, prevClusterCandidate);
                            swapStepOneSubclusters();
                        } else {
                            currentClusterCandidate.clear();
                        }
                    } else {
                        swapStepOneSubclusters();
                    }                    
                }                
            }
            
            if ( findPositionsStep.typoSearchingAllowed() ) {
                if ( currentPositionCandidate.isSet() ) {
                    if ( prevPositionCandidate.isSet() ) {
                        if ( currentPositionCandidate.isBetterThan(prevPositionCandidate) ) {
                            logAnalyze(POSITIONS_SEARCH, "        [EXP POSITION CANDIDATE] %s is better than %s", currentPositionCandidate, prevPositionCandidate);
                            swapStepTwoSubclusters();
                        } else {
                            currentPositionCandidate.clear();
                        }
                    } else {
                        swapStepTwoSubclusters();
                    }                    
                }      
            } else {
                currentPositionCandidate.clear();
                prevPositionCandidate.clear();
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
        /* 
         * end of characterFinding loop 
         */
        
        if ( findPositionsStep.equals(STEP_1)) {
            if ( prevClusterCandidate.isSet() ) {
                logAnalyze(POSITIONS_SEARCH, "        [EXP SAVE CLUSTER] %s", prevClusterCandidate);
                fillPositionsFrom(prevClusterCandidate);
            }
        } else if ( findPositionsStep.typoSearchingAllowed() ) { 
            if ( prevPositionCandidate.isSet() ) {
                logAnalyze(POSITIONS_SEARCH, "        [EXP SAVE POSITIONS] %s", prevPositionCandidate);
                fillPositionsFrom(prevPositionCandidate);
            }
        } else if ( positionCandidate.isPresent() ) {
            int position = positionCandidate.position();
                                                
            isCurrentCharPositionAddedToPositions = true;
            fillPosition(currentPatternCharIndex, position);
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
                    fillPosition(currentPatternCharIndex, currentPatternCharPositionInVariantToSave);
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
        nextPatternCharsToSkip = prevClusterCandidate.skip();
        
        prevClusterCandidate.clear();
        currentClusterCandidate.clear();
        
        prevPositionCandidate.clear();
        currentPositionCandidate.clear();
    }

    private void swapStepOneSubclusters() {
        StepOneSubclusterCandidate swap = currentClusterCandidate;
        prevClusterCandidate.clear();
        currentClusterCandidate = prevClusterCandidate;
        prevClusterCandidate = swap;
    }

    private void swapStepTwoSubclusters() {
        StepTwoSubclusterCandidate swap = currentPositionCandidate;
        prevPositionCandidate.clear();
        currentPositionCandidate = prevPositionCandidate;
        prevPositionCandidate = swap;
    }
    
    private boolean canFillPosition(int positionIndex, int positionValue) {
        if ( positions[positionIndex] == POS_UNINITIALIZED ) {
            if ( ! filledPositions.contains(positionValue) ) {
                return true;
            }
        }
        return false;
    }
    
    private boolean positionsExistAndCanFillPosition(int positionIndex, int positionValue) {
        if ( positionIndex > -1 && 
             positionValue > -1 && 
             positionIndex < positions.length && 
             positionValue < data.variantText.length() ) {
            return canFillPosition(positionIndex, positionValue);
        } else {
            return false;
        }
    }
    
    private void fillPositionsFrom(StepOneSubclusterCandidate subcluster) {
        List<Integer> found = subcluster.found();
        List<Integer> indexes = subcluster.foundIndexes();
        logAnalyze(POSITIONS_SEARCH, "               FOUND positions %s", found);
        logAnalyze(POSITIONS_SEARCH, "               FOUND indexes   %s", indexes);
        for (int i = 0; i < found.size(); i++) {
            fillPosition(indexes.get(i), found.get(i));
            logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
        }        
        logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variantText);
    }
    
    private void fillPositionsFrom(StepTwoSubclusterCandidate subcluster) {
        PositionView position = subcluster.positionView();
        fillPosition(subcluster.charPatternPosition(), subcluster.charVariantPosition());
        logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
        if ( position.notIncluded() ) {
            fillPosition(position.patternPosition(), position.variantPosition());
            logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
        }
        while ( position.goToNext() ) {
            if ( position.notIncluded() ) {
                fillPosition(position.patternPosition(), position.variantPosition());
                logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
            }
        }  
        logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variantText);
    }

    private void fillPosition(int positionIndex, int positionValue) {
        positions[positionIndex] = positionValue;
        positionPatternIndexes.put(positionValue, positionIndex);
        positionFoundSteps.put(positionValue, findPositionsStep);
        localUnclusteredPatternCharIndexes.remove(positionIndex);
        Character c = data.patternChars[positionIndex];
        this.notFoundPatternChars.remove(c);
        isCurrentCharPositionAddedToPositions = true;
    }
    
    private int findNearestFilledPositionTo(int patternCharIndex) {
        int nearestFoundPosition;
        
        if ( patternCharIndex == 0 ) {
            nearestFoundPosition = searchForwardNearestFilledPositionTo(patternCharIndex);
        } else if ( patternCharIndex == data.pattern.length() - 1 ) {
            nearestFoundPosition = searchBackwardNearestFilledPositionTo(patternCharIndex);
        } else {
            nearestFoundPosition = searchForwardAndBackwardNearestFilledPositionTo(patternCharIndex);
        }
        
        return nearestFoundPosition;
    }
    
    private int searchForwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        for (int i = patternCharIndex + 1; i < positions.length; i++) {
            position = positions[i];
            if ( position != POS_UNINITIALIZED ) {
                return position;
            }
        }
        return POS_NOT_FOUND;
    }
    
    private int searchBackwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        for (int i = patternCharIndex - 1; i > -1; i--) {
            position = positions[i];
            if ( position != POS_UNINITIALIZED ) {
                return position;
            }
        }
        return POS_NOT_FOUND;
    }
    
    private int searchForwardAndBackwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        
        if ( patternCharIndex > positions.length / 2 ) {
            position = searchForwardNearestFilledPositionTo(patternCharIndex);
            if ( position == POS_NOT_FOUND ) {
                position = searchBackwardNearestFilledPositionTo(patternCharIndex);
            }
        } else {
            position = searchBackwardNearestFilledPositionTo(patternCharIndex);
            if ( position == POS_NOT_FOUND ) {
                position = searchForwardNearestFilledPositionTo(patternCharIndex);
            }
        }
        
        return position;
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
        
        this.processClusterPositionOrderStats();
        
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
        this.processClusterPositionOrderStats();
    }

    void clusterEnds() {
        this.clustered++;
        this.currentClusterLength++;
        this.clusterContinuation = false;  
        
        this.processClusterPositionOrderStats();
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
        boolean missedRepeatDetected = false;
        
        patternIterating : for (int i = 0; i < this.data.patternChars.length; i++) {
            patternChar = this.data.patternChars[i];
            if ( first == patternChar ) {                
                found = true;
                j = 1;
                
                clusterIterating : for ( ; j < this.currentClusterLength; j++) {
                    if ( missedRepeatDetected ) {
                        clusteredCharPos = this.currentClusterFirstPosition + j + 1;
                        missedRepeatDetected = false;
                    } else {
                        clusteredCharPos = this.currentClusterFirstPosition + j;
                    }                    
                    patternCharPos = i + j;
                    
                    if ( clusteredCharPos >= variantLength || patternCharPos >= patternLength ) {
                        found = false;
                        break patternIterating;
                    }
                    
                    clusteredChar = this.data.variantText.charAt(clusteredCharPos);
                    patternChar = this.data.patternChars[patternCharPos];
                    
                    if ( clusteredChar != patternChar ) {
                        found = false;  
                        if ( nonEmpty(this.missedRepeatedPositions) ) {
                            int missedRepeatIndex = this.missedRepeatedPositions.indexOf(clusteredCharPos);
                            if ( missedRepeatIndex > -1 ) {
                                char missedRepeatChar = this.missedRepeatedChars.get(missedRepeatIndex);
                                if ( missedRepeatChar == clusteredChar ) {
                                    found = true;
                                    this.extractedMissedRepeatedPositionsIndexes.add(missedRepeatIndex);
                                    missedRepeatDetected = true;
                                    j--;
                                }
                            }
                        }
                        
                        if ( ! found ) {
                            this.extractedMissedRepeatedPositionsIndexes.clear();
                            break clusterIterating;
                        }                        
                    }
                }
                i = i + j;
                
                if ( found ) {
                    if ( nonEmpty(this.extractedMissedRepeatedPositionsIndexes) ) {
                        sort(this.extractedMissedRepeatedPositionsIndexes, reverseOrder());
                        int missedRepeatedPositionsIndex;
                        for (int k = 0; k < this.extractedMissedRepeatedPositionsIndexes.size(); k++) {
                            missedRepeatedPositionsIndex = this.extractedMissedRepeatedPositionsIndexes.get(k);
                            this.missedRepeatedPositions.remove(missedRepeatedPositionsIndex);
                            this.missedRepeatedChars.remove(missedRepeatedPositionsIndex);
                        }
                        this.extractedMissedRepeatedPositionsIndexes.clear();
                    }
                    break patternIterating;
                }                
            }
        }        
                              
        this.missedRepeatedChars.clear();
        this.missedRepeatedPositions.clear();
        this.extractedMissedRepeatedPositionsIndexes.clear();
        
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
    
    void processClusterPositionOrderStats() {  
        int currentPositionUnsortedOrder = this.positionUnsortedOrders.get(this.currentPosition);
        int orderDiff = currentPositionUnsortedOrder - (this.currentPositionIndex - this.missed);
        this.currentClusterOrderDiffs.add(orderDiff);
        logAnalyze(
                POSITIONS_CLUSTERS, 
                "    pos. %s (char '%s') S-Order: %s U-Order: %s orderDiff: %s", 
                this.currentPosition, 
                this.data.variantText.charAt(this.currentPosition),
                (this.currentPositionIndex - this.missed), 
                currentPositionUnsortedOrder, 
                orderDiff);
        
        if ( this.missedRepeatingsLog.isPresent() ) {
            logAnalyze(POSITIONS_CLUSTERS, 
                    "         %s",
                    this.missedRepeatingsLog.extractOrThrow());
        }
    }
    
    private int consistencyRewardDependingOnCurrentClusterLength() {
        int consistencyReward = this.currentClusterLength;
        if ( this.currentClusterLength >= this.data.pattern.length() / 2 ) {
            consistencyReward = consistencyReward * 2;
        }
        return consistencyReward;
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
        Cluster cluster = clusters.getUnprocessed();
        processCluster(
                data.pattern.length(),
                cluster,
                this.currentClusterOrderDiffs,                 
                this.currentClusterFirstPosition,
                this.currentClusterLength);

        this.currentClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();
        this.currentClusterOrdersHaveDiffCompensations = cluster.haveOrdersDiffCompensations();

        if ( cluster.hasOrdersDiff() ) {
            boolean teardown = this.clusters.testOnTeardown(cluster);
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
                boolean teardown = this.clusters.testOnTeardown(cluster);
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
        this.clusters.acceptProcessed(cluster);
    }

    boolean isCurrentAndNextPositionInCluster() {
        boolean clusteredDirectly =  
                this.currentPosition == this.nextPosition - 1 &&
                this.positionFoundSteps.get(this.currentPosition).foundPositionCanBeClustered();
        
        if ( ! clusteredDirectly ) {
            if ( this.currentPosition == this.nextPosition - 2 ) {
                char currChar = this.data.variantText.charAt(this.currentPosition);
                char missChar = this.data.variantText.charAt(this.currentPosition + 1);
                char nextChar = this.data.variantText.charAt(this.nextPosition);

                if ( currChar == missChar ) {                        
                    this.missedRepeatedChars.add(missChar);
                    this.missedRepeatedPositions.add(this.currentPosition + 1);
                    if ( POSITIONS_CLUSTERS.isEnabled() ) {
                        String log = format(
                                "    [cluster fix] missed repeat detected %s(%s)%s", 
                                currChar, 
                                missChar,
                                nextChar);
                        this.missedRepeatingsLog.resetTo(log);
                    }
                    return true;
                }
            }
        }
        
        return clusteredDirectly;
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
        this.nextPatternCharsToSkip = 0;
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
        this.missedRepeatedChars.clear();
        this.missedRepeatedPositions.clear();
        this.extractedMissedRepeatedPositionsIndexes.clear();
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
