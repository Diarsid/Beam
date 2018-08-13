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
import static diarsid.beam.core.base.objects.Pools.giveBackAllToPool;
import static diarsid.beam.core.base.util.CollectionsUtils.first;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.MathUtil.cube;
import static diarsid.beam.core.base.util.MathUtil.onePointRatio;
import static diarsid.beam.core.base.util.MathUtil.square;
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
    Set<Integer> filledPositions = new HashSet<>();
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
    PositionCandidate positionCandidate = new PositionCandidate();
    int nearestPositionInVariant;
    List<Integer> currentClusterOrderDiffs = new ArrayList();
    List<Cluster> clusters = new ArrayList<>();
    boolean currentClusterOrdersIsConsistent;
    boolean previousClusterOrdersIsConsistent;
    boolean currentClusterOrdersHaveDiffCompensations;
    int unsortedPositions;
    // --
    
    int distanceBetweenClusters;
    int previousClusterLastPosition = POS_UNINITIALIZED;
    int currentClusterFirstPosition;
    String badReason;
    
    boolean clusterContinuation;
    boolean clusterStartsWithVariant;
    boolean clusterStartsWithSeparator;
    boolean clusterEndsWithSeparator;
    boolean previousClusterEndsWithSeparator;
    int separatorsBetweenClusters;
    
    double missedImportance;
    double clustersImportance; 
    int nonClusteredImportance;
    
    double positionsWeight;
    
    AnalyzePositionsData(AnalyzeData data, AnalyzePositionsDirection direction) {
        this.data = data;
        this.direction = direction;
    }
    
    static boolean arePositionsEquals(AnalyzePositionsData dataOne, AnalyzePositionsData dataTwo) {
        return Arrays.equals(dataOne.positions, dataTwo.positions);
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
        
        if ( this.clustersQty > 1 ) {
            if ( this.separatorsBetweenClusters > 0 ) {
                if ( this.distanceBetweenClusters == this.separatorsBetweenClusters) {
                    logAnalyze(POSITIONS_CLUSTERS, "               [weight] all clusters are one pattern, can be regarded as one cluster!");
                    this.clustersQty = 1;
                }
            } else {
                
            }           
        }
        
        if ( nonEmpty(this.clusters) ) {
            this.analyzeAllClustersOrderDiffs();
        }
    }

    private void doWhenNextAndPreviousCharsAreSeparators() {
        logAnalyze(POSITIONS_CLUSTERS, "               [weight] -11.2 : char is one-char-word");
        this.positionsWeight = this.positionsWeight - 11.2;
    }

    private void doWhenOnlyPreviousCharacterIsSeparator() {
        if ( this.currentPositionCharIsPatternStart() ) {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -17.71 : previous char is word separator, current char is at pattern start!");
//            17.71 ?
            this.positionsWeight = this.positionsWeight - 17.71;
        } else {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -3.1 : previous char is word separator");
            this.positionsWeight = this.positionsWeight - 3.1;
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
        
        for (Cluster cluster : this.clusters) {
            if ( cluster.ordersDiffMean() != 0  ) {
                orderMeansDifferentFromZero++;
            }       
            if ( cluster.length() > 2 ) {
                allClustersHaveLength2 = false;
            }
        }
        
        if ( orderMeansDifferentFromZero > 0 ) {
            int incosistency = orderMeansDifferentFromZero * 2;
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : clusters order incosistency", incosistency);
            this.positionsWeight = this.positionsWeight + incosistency;
        }        
        
        if ( allClustersHaveLength2 ) {
            int penalty = square(clusters.size());
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : all clusters are weak (2 length)", penalty);
            this.positionsWeight = this.positionsWeight + penalty;
        }
        
        giveBackAllToPool(this.clusters);
    }
        
    void findPatternCharsPositions() {
                
        findPositionsStep = STEP_1;
        logAnalyze(POSITIONS_SEARCH, "  %s", direction);
        logAnalyze(POSITIONS_SEARCH, "    %s", findPositionsStep);
        
        if ( direction.equals(FORWARD) ) {
            unclusteredPatternCharIndexes = forwardUnclusteredIndexes;
            for (int currentPatternCharIndex = 0; currentPatternCharIndex < data.patternChars.length; currentPatternCharIndex++) {                
                processCurrentPatternCharOf(currentPatternCharIndex);
            } 
        } else {
            unclusteredPatternCharIndexes = reverseUnclusteredIndexes;
            for (int currentPatternCharIndex = data.patternChars.length - 1; currentPatternCharIndex > -1 ; currentPatternCharIndex--) {
                processCurrentPatternCharOf(currentPatternCharIndex);
            }
        }
        swapUnclusteredPatternCharIndexes();
        
        findPositionsStep = STEP_2;
        processAccumulatedUnclusteredPatternCharIndexes();           
        swapUnclusteredPatternCharIndexes();
        
        processAccumulatedUnclusteredPatternCharIndexes();        
        swapUnclusteredPatternCharIndexes();
        
        findPositionsStep = STEP_3;
        processAccumulatedUnclusteredPatternCharIndexes();      
        swapUnclusteredPatternCharIndexes();
        
        clearState();
    }
    
    private void processAccumulatedUnclusteredPatternCharIndexes() {
        if ( nonEmpty(unclusteredPatternCharIndexes) ) {
            logAnalyze(POSITIONS_SEARCH, "    %s", findPositionsStep);
            for (Integer currentPatternCharIndex : unclusteredPatternCharIndexes) {
                processCurrentPatternCharOf(currentPatternCharIndex);
            }
        }
    }

    private void swapUnclusteredPatternCharIndexes() {
        unclusteredPatternCharIndexes.clear();
        unclusteredPatternCharIndexes.addAll(localUnclusteredPatternCharIndexes);
        localUnclusteredPatternCharIndexes.clear();
    }

    private void clearState() {
        this.filledPositions.clear();
        this.unclusteredPatternCharIndexes.clear();
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
    
    private void processCurrentPatternCharOf(int currentPatternCharIndex) {
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
                    if ( hasPreviousInPattern && hasNextInVariant ) {

                        previousCharInPattern = data.patternChars[currentPatternCharIndex - 1];
                        nextCharInVariant = data.variantText.charAt(currentPatternCharPositionInVariant + 1);

                        if ( previousCharInPattern == nextCharInVariant ) {
                            logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and next in variant", 
                                    currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                            typosFound++;
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
                                nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
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
                        filledPositions.add(currentPatternCharPositionInVariant);
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
                    // on steps, other than STEP_1, do not save positions directly, just record them as appropriate for saving.
                    if ( findPositionsStep.canAddToPositions(charsInClusterQty) ) {
                        
                        isCurrentCharPositionAddedToPositions = true;
                        Integer nearestPatternCharIndex = positionPatternIndexes.get(nearestPositionInVariant);
                        if ( isNull(nearestPatternCharIndex) ) {
                            // nearest char can be null only when current char is clastered with next pattern position
                            if ( direction.equals(FORWARD) ) {
                                nearestPatternCharIndex = currentPatternCharIndex + 1;
                            } else {
                                nearestPatternCharIndex = currentPatternCharIndex - 1;
                            }
                        } 
                        int orderDiffInPattern = abs(currentPatternCharIndex - nearestPatternCharIndex);
                        
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
                                                
                        positionCandidate.mutate(
                                currentPatternCharPositionInVariant, 
                                orderDiffInVariant, 
                                orderDiffInPattern, 
                                charsInClusterQty);
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
        
        if ( findPositionsStep.isLaterThan(STEP_1) && positionCandidate.isPresent() ) {
            int position = positionCandidate.position();
            
            if ( positionCandidate.committedMutations() > 0 && positionCandidate.hasRejectedMutations() ) {
                int a = 5;
            }
            
            positions[currentPatternCharIndex] = position;
            positionPatternIndexes.put(position, currentPatternCharIndex);
            filledPositions.add(position);
            logAnalyze(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant)", currentChar, position);
            logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
            logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variantText);
        }
        positionCandidate.clear();

        // if current position has not been added because it does not satisfy requirements...
        if ( ! isCurrentCharPositionAddedToPositions ) {
            // ...but if it is STEP_1 and there are only 1 such char in the whole pattern, there is not sense
            // to do operation for this char in subsequent steps - add this char to filled positions and exclude
            // it from subsequent iterations
            if ( findPositionsStep.equals(STEP_1) && currentCharInVariantQty == 1 ) {
                if ( positionAlreadyFilled ) {
                    logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in variant) is single char in variant and already saved", currentChar, currentPatternCharPositionInVariantToSave);
                    positions[currentPatternCharIndex] = POS_NOT_FOUND;
                } else {
                    positions[currentPatternCharIndex] = currentPatternCharPositionInVariantToSave;
                    positionPatternIndexes.put(currentPatternCharPositionInVariantToSave, currentPatternCharIndex);
                    filledPositions.add(currentPatternCharPositionInVariantToSave);
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
                filledPositions.add(positionValue);
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
        int distanceBetweenTwoClusters = 0;
        if ( this.clustersQty > 0 ) {            
            distanceBetweenTwoClusters = 
                    this.currentClusterFirstPosition - this.previousClusterLastPosition - 1;
            this.distanceBetweenClusters = 
                    this.distanceBetweenClusters + distanceBetweenTwoClusters;
        }
        this.clustered++;
        this.clustersQty++;
        this.clusterContinuation = true;
        this.currentClusterLength = 1;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        
        this.processClusterPositionOrderStats("+- ");
        
        if ( this.currentPosition == 0 ) {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -8.6 : cluster starts with variant");
            this.positionsWeight = this.positionsWeight - 8.6;            
            this.clusterStartsWithVariant = true;
            this.clusterStartsWithSeparator = true;
        } else if ( this.isPreviousCharWordSeparator() ) {                
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -4.6 : cluster start, previous char is word separator");
            this.positionsWeight = this.positionsWeight - 4.6;
            this.clusterStartsWithSeparator = true;
        } else if ( this.isCurrentCharWordSeparator() ) {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -4.6 : cluster start, current char is word separator");
            this.positionsWeight = this.positionsWeight - 4.6;
            this.clusterStartsWithSeparator = true;
        }

        if ( this.previousClusterEndsWithSeparator ) {
            this.separatorsBetweenClusters++;
            if ( this.clusterStartsWithSeparator && distanceBetweenTwoClusters > 1 ) {
                this.separatorsBetweenClusters++;
            }
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
            float bonus = 1.6f; 
            if ( this.currentClusterLength > 2 ) {
                bonus = bonus + this.currentClusterLength;
            }
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : cluster ends with variant", bonus);
            this.positionsWeight = this.positionsWeight - bonus;
            this.clusterEndsWithSeparator = true;
        } else if ( this.isNextCharWordSeparator() ) {
            float bonus = 1.6f; 
            if ( this.currentClusterLength > 2 ) {
                bonus = bonus + this.currentClusterLength;
            }
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : cluster ends, next char is word separator", bonus);
            this.positionsWeight = this.positionsWeight - bonus;
            this.clusterEndsWithSeparator = true;
        } else if ( this.isCurrentCharWordSeparator() ) {
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -4.6 : cluster ends, current char is word separator");
            this.positionsWeight = this.positionsWeight - 4.6;
            this.clusterEndsWithSeparator = true;
        }
        
        if ( this.clusterStartsWithVariant && this.currentClusterLength > 2 ) {
            float bonus = this.currentClusterLength / 2;
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : cluster starts with variant bonus", bonus);
            this.positionsWeight = this.positionsWeight - bonus;
        }

        if ( this.clusterStartsWithSeparator && this.clusterEndsWithSeparator ) {
            float bonus = 10.25f;
            if ( this.currentClusterLength > 2 ) {
                bonus = bonus + (this.currentClusterLength - 2);
            }
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : cluster is a word", bonus);
            this.positionsWeight = this.positionsWeight - bonus;
        }

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

        if ( this.currentClusterLength > 2 && this.currentClusterOrdersIsConsistent && ! this.currentClusterOrdersHaveDiffCompensations ) {
            if ( this.patternContainsClusterFoundInVariant() ) {
                logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] pattern contains cluster!");                
                int containingReward = cube(this.currentClusterLength);
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] -%s : ^ ", containingReward);
                this.positionsWeight = this.positionsWeight - containingReward;
            } else {
                logAnalyze(POSITIONS_CLUSTERS, "            [C-stat] pattern DOES NOT contain cluster!");
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : ^ ", this.currentClusterLength);
                this.positionsWeight = this.positionsWeight + this.currentClusterLength;
            }
        }
        
        this.previousClusterLastPosition = this.currentPosition;
        this.previousClusterEndsWithSeparator = this.clusterEndsWithSeparator;
        this.previousClusterLength = this.currentClusterLength;
        this.previousClusterOrdersIsConsistent = this.currentClusterOrdersIsConsistent;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        this.clusterEndsWithSeparator = false;
    }
    
    private boolean patternContainsClusterFoundInVariant() {
        char first = this.data.variantText.charAt(this.currentClusterFirstPosition);
        char patternChar;
        char clusteredChar;
        
        int clusteredCharPos;
        int patternChatPos;
        
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
                    patternChatPos = i + j;
                    
                    if ( clusteredCharPos >= variantLength || patternChatPos >= patternLength ) {
                        found = false;
                        break patternIterating;
                    }
                    
                    clusteredChar = this.data.variantText.charAt(clusteredCharPos);
                    patternChar = this.data.patternChars[patternChatPos];
                    
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
//        if ( orderDiff != 0 ) {
            this.currentClusterOrderDiffs.add(orderDiff);
//        }
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
        Cluster cluster = calculateCluster(this.currentClusterOrderDiffs, this.currentClusterLength);

        this.currentClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();
        this.currentClusterOrdersHaveDiffCompensations = cluster.haveOrdersDiffCompensations();

        if ( cluster.hasOrdersDiff() ) {
            int incosistency = inconsistencyOf(cluster, this.currentClusterLength);
            logAnalyze(POSITIONS_CLUSTERS, "               [weight] +%s : for inconsistency", incosistency);
            this.positionsWeight = this.positionsWeight + incosistency;
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
        
        this.previousClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();
        
        this.currentClusterOrderDiffs.clear();  
        this.clusters.add(cluster);
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
        this.nonClusteredImportance = nonClusteredImportanceDependingOn(
                this.nonClustered, this.missed, this.data.patternChars.length);
        this.missedImportance = missedImportanceDependingOn(
                this.missed, 
                this.clustersImportance,
                this.data.patternChars.length,
                this.data.variantText.length());
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
    
    void clearPositionsAnalyze() {
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
        this.separatorsBetweenClusters = 0;
        this.currentPosition = POS_UNINITIALIZED;
        this.currentPositionIndex = POS_UNINITIALIZED;
        this.nextPosition = POS_UNINITIALIZED;
        this.prevCharIsSeparator = false;
        this.nextCharIsSeparator = false;
        this.currentPatternCharPositionInVariant = POS_UNINITIALIZED;
        this.missedImportance = 0;
        this.clustersImportance = 0;
        this.nonClusteredImportance = 0;
        this.positionsWeight = 0;
        this.distanceBetweenClusters = 0;
        this.previousClusterLastPosition = POS_UNINITIALIZED;
        this.currentClusterFirstPosition = POS_UNINITIALIZED;
        this.badReason = NO_REASON;
        this.currentChar = ' ';
        this.patternInVariantLength = 0;
        this.skipNextPatternChar = false;
        this.positionUnsortedOrders.clear();
        this.positionPatternIndexes.clear();
        this.positionCandidate.clear();
        this.nearestPositionInVariant = POS_UNINITIALIZED;
        this.currentClusterOrderDiffs.clear();
        if ( nonEmpty(this.clusters) ) {
            giveBackAllToPool(this.clusters);
        }
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
