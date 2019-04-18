/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.TreeSet;
import java.util.function.IntFunction;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.support.objects.Pool;
import diarsid.support.objects.PooledReusable;

import static java.lang.String.format;
import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.BASE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.beam.core.base.analyze.variantsweight.PositionsAnalyze.POS_NOT_FOUND;
import static diarsid.beam.core.base.analyze.variantsweight.PositionsAnalyze.POS_UNINITIALIZED;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.lengthImportanceRatio;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedTooMuch;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.BAD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimate;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimatePreliminarily;
import static diarsid.beam.core.base.util.CollectionsUtils.isNotEmpty;
import static diarsid.beam.core.base.util.MathUtil.percentAsInt;
import static diarsid.beam.core.base.util.MathUtil.ratio;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.indexOfIgnoreCase;
import static diarsid.support.strings.StringUtils.isPathSeparator;
import static diarsid.support.strings.StringUtils.isTextSeparator;
import static diarsid.support.strings.StringUtils.lower;
import static diarsid.support.strings.StringUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
class AnalyzeData extends PooledReusable {
    
    private static final IntFunction<String> POSITION_INT_TO_STRING;
    
    static {
        POSITION_INT_TO_STRING = (position) -> {
            if ( position == POS_NOT_FOUND ) {
                return "x";
            } else {
                return String.valueOf(position);
            }                    
        };
    }
    
    final PositionsAnalyze positionsAnalyze;
            
    WeightedVariant weightedVariant;
    
    private Variant variant;
    TreeSet<Integer> variantSeparators;
    TreeSet<Integer> variantPathSeparators;
    TreeSet<Integer> variantTextSeparators;
    String variantText;
    boolean variantEqualsToPattern;
    boolean variantContainsPattern;
    int patternInVariantIndex;
    
    double variantWeight;
    double lengthDelta;
    boolean calculatedAsUsualClusters;
    
    char[] patternChars;
    String pattern;
    
    int missedPercent;
        
    AnalyzeData(Pool<Cluster> clusterPool) {
        super();
        this.positionsAnalyze = new PositionsAnalyze(
                this, 
                new Clusters(this, clusterPool), 
                new PositionCandidate(this));
        this.variantSeparators = new TreeSet<>();
        this.variantPathSeparators = new TreeSet<>();
        this.variantTextSeparators = new TreeSet<>();
    }

    void set(String pattern, Variant variant) {
        this.variant = variant;
        this.variantText = lower(variant.text());
        this.pattern = pattern;
        this.checkIfVariantEqualsToPatternAndAssignWeight();
    }
    
    void set(String pattern, String variantText) {
        this.variant = null;
        this.variantText = lower(variantText);
        this.pattern = pattern;
        this.checkIfVariantEqualsToPatternAndAssignWeight();
    }

    private void checkIfVariantEqualsToPatternAndAssignWeight() {
        this.variantEqualsToPattern = this.pattern.equalsIgnoreCase(this.variantText);
        if ( this.variantEqualsToPattern ) {
            this.variantWeight = - this.variantText.length() * 1024;
            logAnalyze(BASE, "  variant is equal to pattern: weight %s", this.variantWeight);            
        }
    }
    
    @Override
    public void clearForReuse() {
        this.pattern = null;
        this.positionsAnalyze.clearPositionsAnalyze();
        this.variantSeparators.clear();
        this.variantPathSeparators.clear();
        this.variantTextSeparators.clear();
        this.variant = null;
        this.variantText = "";
        this.variantEqualsToPattern = false;
        this.variantContainsPattern = false;
        this.patternInVariantIndex = -1;
        this.variantWeight = 0;
        this.lengthDelta = 0;
        this.weightedVariant = null;
        this.calculatedAsUsualClusters = true;
        this.missedPercent = 0;
    }

    void complete() {
        // weight calculation ends
        if ( nonNull(this.variant) ) {
            this.weightedVariant = new WeightedVariant(
                    this.variant, this.variantEqualsToPattern, this.variantWeight);
        }        
    }

    void calculateWeight() {        
        this.variantWeight = this.variantWeight + this.positionsAnalyze.weight.sum();
        logAnalyze(BASE, "  weight on step 1: %s (positions: %s) ", this.variantWeight, this.positionsAnalyze.weight);
        
        
        if (POSITIONS_CLUSTERS.isEnabled()) {
            this.positionsAnalyze.weight.observeAll((i, weight, element) -> {
                logAnalyze(POSITIONS_CLUSTERS, format("               [weight] %1$s) %2$-+7.1f : %3$s", i, weight, element.description()));
            });
        }
        
        if ( this.variantWeight > 0 ) {
            this.positionsAnalyze.badReason = "preliminary position calculation is too bad";
            return;
        }
        if ( this.positionsAnalyze.clustersQty > 0 ) {
            switch ( this.pattern.length() ) {
                case 0 : 
                case 1 : {
                    throw new IllegalStateException(
                            "This analyze is not intended to process 0 or 1 length patterns!");
                }
                case 2 : {
                    this.calculateAsUsualClusters();
                    this.calculatedAsUsualClusters = true;
                    break;
                }
                case 3 :
                case 4 : {
                    if ( this.positionsAnalyze.missed == 0 && this.positionsAnalyze.nonClustered > 2 ) {
                        if ( this.areAllPositionsPresentSortedAndNotPathSeparatorsBetween() ) {
                            this.calculateAsSeparatedCharsWithoutClusters();
                            this.calculatedAsUsualClusters = false;
                        } else {
                            this.positionsAnalyze.badReason = "Too much unclustered positions: " + this.positionsAnalyze.nonClustered;
                            return; 
                        }
                    } else if ( this.positionsAnalyze.missed == 1 && this.positionsAnalyze.nonClustered > 1 ) {
                        this.positionsAnalyze.badReason = "Too much unclustered and missed positions: " + (this.positionsAnalyze.missed + this.positionsAnalyze.nonClustered);
                        return;
                    } else if ( this.positionsAnalyze.missed == 0 && this.positionsAnalyze.nonClustered > 1 ) {
                        if ( this.positionsAnalyze.clustersFacingStartEdges > 0 ) {
                            this.calculateAsUsualClusters();
                            this.calculatedAsUsualClusters = true;
                        } else if ( this.areAllPositionsPresentSortedAndNotPathSeparatorsBetween() ) {
                            this.calculateAsSeparatedCharsWithoutClusters();
                            this.calculatedAsUsualClusters = false;
                        } else {
                            this.positionsAnalyze.badReason = "Too much unclustered positions";
                            return;
                        }                        
                    } else {
                        this.calculateAsUsualClusters();
                        this.calculatedAsUsualClusters = true;
                    }
                    break;
                }
                default: {
                    float tresholdRatio;
                    
                    if ( this.positionsAnalyze.clustersQty == 1 ) {
                        tresholdRatio = 0.5f;
                    } else {
                        tresholdRatio = 0.4f;
                    }
                    
                    if ( ratio(this.positionsAnalyze.nonClustered, this.patternChars.length) > tresholdRatio ) {
                        if ( this.areAllPositionsPresentSortedAndNotPathSeparatorsBetween() ) {
                            this.calculateAsSeparatedCharsWithoutClusters();
                            this.calculatedAsUsualClusters = false;
                        } else {
                            this.positionsAnalyze.badReason = "Too much unclustered positions";
                            return;
                        }                        
                    } else {
                        this.calculateAsUsualClusters();
                        this.calculatedAsUsualClusters = true;
                    }
                }
            }            
        } else {
            if ( this.areAllPositionsPresentSortedAndNotPathSeparatorsBetween() ) {
                this.calculateAsSeparatedCharsWithoutClusters();
                this.calculatedAsUsualClusters = false;
            } else {
                this.positionsAnalyze.badReason = "There are no clusters, positions are unsorted";
                return;
            }
        }
        
        logAnalyze(BASE, "  weight on step 2: %s", this.variantWeight);
    }

    private boolean areAllPositionsPresentSortedAndNotPathSeparatorsBetween() {
        if (this.positionsAnalyze.unsortedPositions == 0 && this.positionsAnalyze.missed == 0 ) {
            if ( this.variantPathSeparators.isEmpty() ) {
                return true;
            } else {
                int first = this.positionsAnalyze.findFirstPosition();
                int last = this.positionsAnalyze.findLastPosition();
                
                if ( first < 0 || last < 0 ) {
                    return false;
                }
                
                Integer possibleSeparator = this.variantPathSeparators.higher(first);
                if ( nonNull(possibleSeparator) ) {
                    return last < possibleSeparator;
                } 
                
                return true;
            }
        } else {
            return false;
        }
    }
    
    private void calculateAsUsualClusters() {
        if ( this.positionsAnalyze.nonClustered == 0 && 
             this.positionsAnalyze.missed == 0 &&
             this.variantText.length() == this.positionsAnalyze.clustered + 
                                          this.variantPathSeparators.size() + 
                                          this.variantTextSeparators.size() ) {
            this.variantWeight = this.variantWeight - this.positionsAnalyze.clustersImportance - this.positionsAnalyze.clustered;
        } else {
            double lengthImportance = lengthImportanceRatio(this.variantText.length());
            this.lengthDelta = ( this.variantText.length() - this.positionsAnalyze.clustered ) * 0.3 * lengthImportance;

            this.variantWeight = this.variantWeight + (
                    this.positionsAnalyze.nonClusteredImportance 
                    - this.positionsAnalyze.clustersImportance
/* EXP BREAKING */ //                     + this.positionsAnalyze.missedImportance
                    + this.lengthDelta  
                    + this.variantPathSeparators.size() 
                    + this.variantTextSeparators.size()
            );
            
            if ( this.positionsAnalyze.missed > 0 ) {
                this.missedPercent = 100 - percentAsInt(this.positionsAnalyze.missed, this.pattern.length());
                this.variantWeight = this.variantWeight * this.missedPercent / 100;
            }
        }        
    }
    
    private void calculateAsSeparatedCharsWithoutClusters() {
        double bonus = this.positionsAnalyze.positions.length * 5.1;
        this.variantWeight = this.variantWeight - bonus;
        logAnalyze(BASE, "               [weight] -%s : no clusters, all positions are sorted, none missed", bonus);
    }

    void calculateClustersImportance() {
        this.positionsAnalyze.calculateImportance();
    }
    
    PositionsAnalyze positions() {
        return this.positionsAnalyze;
    }
    
    boolean ifClustersPresentButWeightTooBad() {
        return this.positionsAnalyze.clustersQty > 0 && estimatePreliminarily(this.positionsAnalyze.weight.sum()).equals(BAD);
    }

    boolean isVariantTooBad() {
        return nonEmpty(this.positionsAnalyze.badReason) || estimate(this.variantWeight).equals(BAD);
    }

    void isFirstCharMatchInVariantAndPattern(String pattern) {
        // TODO if there are no path separators if variant is quite short and if clusters are 0-2
//        if ( pattern.charAt(0) == this.variantText.charAt(0) ) {  
//            logAnalyze(BASE, "               [weight] -3.4 : first char match in variant and pattern ");
//            this.variantWeight = this.variantWeight - 3.4;            
//        }
    }

    void logState() {
        logAnalyze(BASE, "  variant       : %s", this.variantText);
        
        String patternCharsString = stream(this.positionsAnalyze.positions)
                .mapToObj(position -> {
                    if ( position < 0 ) {
                        return "*";
                    } else {
                        return String.valueOf(this.variantText.charAt(position));
                    }                    
                })
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        String positionsString =  stream(this.positionsAnalyze.positions)
                .mapToObj(POSITION_INT_TO_STRING)
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        logAnalyze(BASE, "  pattern chars : %s", patternCharsString);
        logAnalyze(BASE, "  positions     : %s", positionsString);
                
        if ( nonEmpty(this.positionsAnalyze.badReason) ) {
            logAnalyze(BASE, "    %1$-25s %2$s", "bad reason", this.positionsAnalyze.badReason);
            return;
        }
        
        if ( this.calculatedAsUsualClusters ) {
            this.logClustersState();
        } else {
            logAnalyze(BASE, "  calculated as separated characters");
        }
        logAnalyze(BASE, "    %1$-25s %s", "total weight", this.variantWeight); 
    }
    
    private void logClustersState() {
        logAnalyze(BASE, "    %1$-25s %2$s", "clusters", positionsAnalyze.clustersQty);
        logAnalyze(BASE, "    %1$-25s %2$s", "clustered", positionsAnalyze.clustered);
        logAnalyze(BASE, "    %1$-25s %2$s", "length delta", this.lengthDelta);
        logAnalyze(BASE, "    %1$-25s %2$s", "distance between clusters", positionsAnalyze.clusters.distanceBetweenClusters());
        logAnalyze(BASE, "    %1$-25s %2$s", "separators between clusters", positionsAnalyze.separatorsBetweenClusters);
        logAnalyze(BASE, "    %1$-25s %2$s", "variant text separators ", this.variantTextSeparators.size());
        logAnalyze(BASE, "    %1$-25s %2$s", "variant path separators ", this.variantPathSeparators.size());
        logAnalyze(BASE, "    %1$-25s %2$s", "nonClustered", positionsAnalyze.nonClustered);
        logAnalyze(BASE, "    %1$-25s %2$s", "nonClusteredImportance", positionsAnalyze.nonClusteredImportance);
        logAnalyze(BASE, "    %1$-25s %2$s", "clustersImportance", positionsAnalyze.clustersImportance);
        logAnalyze(BASE, "    %1$-25s %2$s", "missed", positionsAnalyze.missed);
        logAnalyze(BASE, "    %1$-25s %2$s%%", "missedPercent", this.missedPercent);
    }

    boolean areTooMuchPositionsMissed() {
        boolean tooMuchMissed = missedTooMuch(this.positionsAnalyze.missed, this.patternChars.length);
        if ( tooMuchMissed ) {
            logAnalyze(BASE, "    %s, missed: %s to much, skip variant!", this.variantText, this.positionsAnalyze.missed);
        }
        return tooMuchMissed;
    }

    void sortPositions() {
        this.positionsAnalyze.sortPositions();
    }
    
    void findPositionsClusters() {
        this.positionsAnalyze.analyzePositionsClusters();
    }
    
    boolean isVariantEqualsPattern() {
        return this.variantEqualsToPattern;
    }
    
    boolean isVariantNotEqualsPattern() {
        return ! this.variantEqualsToPattern;
    }

    void checkIfVariantTextContainsPatternDirectly() {
        this.patternInVariantIndex = indexOfIgnoreCase(this.variantText, this.pattern);
        if ( this.patternInVariantIndex >= 0 ) {
            double lengthRatio = patternLengthRatio(this.pattern);
            logAnalyze(BASE, "  variant contains pattern: weight -%s", lengthRatio);
            this.variantWeight = this.variantWeight - lengthRatio;
            this.variantContainsPattern = true;
        }
    }
    
    void findPathAndTextSeparators() {
        for (int i = 0; i < this.variantText.length(); i++) {
            if ( isPathSeparator(this.variantText.charAt(i)) ) {
                this.variantPathSeparators.add(i);
            }
            if ( isTextSeparator(this.variantText.charAt(i)) ) {
                this.variantTextSeparators.add(i);
            }
        }
        if ( isNotEmpty(this.variantPathSeparators) ) {
            this.variantSeparators.addAll(this.variantPathSeparators);
        }
        if ( isNotEmpty(this.variantTextSeparators) ) {
            this.variantSeparators.addAll(this.variantTextSeparators);
        }
    }

    void setPatternCharsAndPositions() {
        this.patternChars = this.pattern.toCharArray();
        this.positionsAnalyze.positions = new int[this.patternChars.length];
        fill(this.positionsAnalyze.positions, POS_UNINITIALIZED);
    }
    
    private static double patternLengthRatio(String pattern) {
        return pattern.length() * 5.5;
    }

    void findPatternCharsPositions() {
        if ( this.variantContainsPattern ) {
            this.positionsAnalyze.fillPositionsFromIndex(this.patternInVariantIndex);
        } else {
            this.positionsAnalyze.findPatternCharsPositions();
        }
    }

    void logUnsortedPositions() {
        this.logUnsortedPositionsOf(this.positionsAnalyze);
    }

    private void logUnsortedPositionsOf(PositionsAnalyze data) {
        String positionsS = stream(data.positions)
                .mapToObj(POSITION_INT_TO_STRING)
                .collect(joining(" "));
        logAnalyze(BASE, "  positions before sorting: %s", positionsS);
    }
}
