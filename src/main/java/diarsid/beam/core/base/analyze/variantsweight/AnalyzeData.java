/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.TreeSet;

import diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection;
import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.support.objects.PooledReusable;

import static java.lang.Math.pow;
import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.BASE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.POS_UNINITIALIZED;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.arePositionsEquals;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.lengthImportanceRatio;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedTooMuch;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.BAD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimate;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimatePreliminarily;
import static diarsid.beam.core.base.util.MathUtil.ratio;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.isPathSeparator;
import static diarsid.beam.core.base.util.StringUtils.isTextSeparator;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
class AnalyzeData extends PooledReusable {
    
    static {
        createPoolFor(AnalyzeData.class, () -> new AnalyzeData());
    }
    
    final AnalyzePositionsData forwardAnalyze;
    final AnalyzePositionsData reverseAnalyze;
    private boolean forwardAndReverseEqual;
    
    AnalyzePositionsData best;
            
    WeightedVariant weightedVariant;
    
    private Variant variant;
    TreeSet<Integer> variantPathSeparators;
    TreeSet<Integer> variantTextSeparators;
    String variantText;
    boolean variantEqualsToPattern;
    
    double variantWeight;
    double lengthDelta;
    boolean calculatedAsUsualClusters;
    
    char[] patternChars;
    String pattern;
        
    private AnalyzeData() {
        super();
        this.forwardAnalyze = new AnalyzePositionsData(this, FORWARD);
        this.reverseAnalyze = new AnalyzePositionsData(this, REVERSE);
        this.variantPathSeparators = new TreeSet<>();
        this.variantTextSeparators = new TreeSet<>();
        this.forwardAndReverseEqual = false;
    }

    void set(String pattern, Variant variant) {
        this.variant = variant;
        this.variantText = lower(variant.text());
        this.pattern = pattern;
        this.variantEqualsToPattern = this.pattern.equalsIgnoreCase(this.variantText);
        if ( this.variantEqualsToPattern ) {
            this.variantWeight = -pow(this.variantText.length(), 5);
            logAnalyze(BASE, "  variant is equal to pattern: weight %s", this.variantWeight);            
        }
    }
    
    void set(String pattern, String variantText) {
        this.variant = null;
        this.variantText = lower(variantText);
        this.pattern = pattern;
        this.variantEqualsToPattern = this.pattern.equalsIgnoreCase(this.variantText);
        if ( this.variantEqualsToPattern ) {
            this.variantWeight = -pow(this.variantText.length(), 5);
            logAnalyze(BASE, "  variant is equal to pattern: weight %s", this.variantWeight);            
        }
    }
    
    @Override
    public void clearForReuse() {
        this.pattern = null;
        this.forwardAnalyze.clearPositionsAnalyze();
        this.reverseAnalyze.clearPositionsAnalyze();
        this.variantPathSeparators.clear();
        this.variantTextSeparators.clear();
        this.best = null;
        this.variant = null;
        this.variantText = "";
        this.variantEqualsToPattern = false;
        this.variantWeight = 0;
        this.lengthDelta = 0;
        this.weightedVariant = null;
        this.forwardAndReverseEqual = false;
        this.calculatedAsUsualClusters = true;
    }

    void complete() {
        // weight calculation ends
        if ( nonNull(this.variant) ) {
            this.weightedVariant = new WeightedVariant(
                    this.variant, this.variantEqualsToPattern, this.variantWeight);
        }        
    }

    void calculateWeight() {        
        this.best = this.bestPositions();
        this.variantWeight = this.variantWeight + this.best.positionsWeight;
        logAnalyze(BASE, "  weight on step 1: %s (positions: %s) ", this.variantWeight, this.best.positionsWeight);
        
        if ( this.variantWeight > 0 ) {
            this.best.badReason = "preliminary position calculation is too bad";
            return;
        }
        if ( this.best.clustered > 0 ) {
            if ( ratio(this.best.nonClustered, this.patternChars.length) > 0.4 ) {
                this.best.badReason = "Too much unclustered positions";
                return;
            } else {
                this.calculateAsUsualClusters();
                this.calculatedAsUsualClusters = true;
            }
        } else {
            if ( this.best.unsortedPositions == 0 && this.best.missed == 0 ) {
                this.calculateAsSeparatedCharsWithoutClusters();
                this.calculatedAsUsualClusters = false;
            } else {
                this.best.badReason = "There are no clusters, positions are unsorted";
                return;
            }
        }
        
        logAnalyze(BASE, "  weight on step 2: %s", this.variantWeight);
    }
    
    private void calculateAsUsualClusters() {
        if ( this.best.nonClustered == 0 && 
             this.best.missed == 0 &&
             this.variantText.length() == this.best.clustered + 
                                          this.variantPathSeparators.size() + 
                                          this.variantTextSeparators.size() ) {
            this.variantWeight = this.variantWeight - this.best.clustersImportance - this.best.clustered;
        } else {
            double lengthImportance = lengthImportanceRatio(this.variantText.length());
            this.lengthDelta = ( this.variantText.length() - this.best.clustered ) * 0.3 * lengthImportance;

            this.variantWeight = this.variantWeight + (
                    this.best.nonClusteredImportance 
                    - this.best.clustersImportance
                    + this.best.missedImportance
                    + this.lengthDelta  
                    + this.variantPathSeparators.size() 
                    + this.variantTextSeparators.size()
            );
        }        
    }
    
    private void calculateAsSeparatedCharsWithoutClusters() {
        double bonus = this.best.positions.length * 5.1;
        this.variantWeight = this.variantWeight - bonus;
        logAnalyze(BASE, "               [weight] -%s : no clusters, all positions are sorted, none missed", bonus);
    }

    void calculateClustersImportance() {
        this.forwardAnalyze.calculateImportance();
        if ( this.forwardAndReverseEqual ) {
            return;
        }
        this.reverseAnalyze.calculateImportance();
    }
    
    AnalyzePositionsData bestPositions() {
        if ( this.forwardAndReverseEqual ) {
            return this.forwardAnalyze;
        }
        return this.forwardAnalyze.clustersImportance + ( this.forwardAnalyze.positionsWeight * -1.0d ) 
                >= this.reverseAnalyze.clustersImportance + ( this.reverseAnalyze.positionsWeight * -1.0d )  ? 
                this.forwardAnalyze : this.reverseAnalyze;
    }
    
    AnalyzePositionsData positionsOf(AnalyzePositionsDirection direction) {
        if ( direction.equals(FORWARD) ) {
            return this.forwardAnalyze;
        } else {
            return this.reverseAnalyze;
        }
    }
    
    boolean ifClustersPresentButWeightTooBad() {
        return ( this.forwardAnalyze.clustersQty > 0 && estimatePreliminarily(this.forwardAnalyze.positionsWeight).equals(BAD) ) 
               && 
               ( this.reverseAnalyze.clustersQty > 0 && estimatePreliminarily(this.reverseAnalyze.positionsWeight).equals(BAD));
    }

    boolean isVariantTooBad() {
        return nonEmpty(this.best.badReason) || estimate(this.variantWeight).equals(BAD);
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
        
        String patternCharsString = stream(this.best.positions)
                .mapToObj(position -> {
                    if ( position < 0 ) {
                        return "*";
                    } else {
                        return String.valueOf(this.variantText.charAt(position));
                    }                    
                })
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        String positionsString =  stream(this.best.positions)
                .mapToObj(position -> String.valueOf(position))
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        logAnalyze(BASE, "  pattern chars : %s", patternCharsString);
        logAnalyze(BASE, "  positions     : %s", positionsString);
        logAnalyze(BASE, "    %-25s %s", "direction", this.best.direction);
                
        if ( nonEmpty(this.best.badReason) ) {
            logAnalyze(BASE, "    %-25s %s", "bad reason", this.best.badReason);
            return;
        }
        
        if ( this.calculatedAsUsualClusters ) {
            this.logClustersState();
        } else {
            logAnalyze(BASE, "  calculated as separated characters");
        }
        logAnalyze(BASE, "    %-25s %s", "total weight", this.variantWeight); 
    }
    
    private void logClustersState() {
        AnalyzePositionsData positions = this.best;
        logAnalyze(BASE, "    %-25s %s", "clusters", positions.clustersQty);
        logAnalyze(BASE, "    %-25s %s", "clustered", positions.clustered);
        logAnalyze(BASE, "    %-25s %s", "length delta", this.lengthDelta);
        logAnalyze(BASE, "    %-25s %s", "distance between clusters", positions.clusters.distanceBetweenClusters());
        logAnalyze(BASE, "    %-25s %s", "separators between clusters", positions.separatorsBetweenClusters);
        logAnalyze(BASE, "    %-25s %s", "variant text separators ", this.variantTextSeparators.size());
        logAnalyze(BASE, "    %-25s %s", "variant path separators ", this.variantPathSeparators.size());
        logAnalyze(BASE, "    %-25s %s", "nonClustered", positions.nonClustered);
        logAnalyze(BASE, "    %-25s %s", "nonClusteredImportance", positions.nonClusteredImportance);
        logAnalyze(BASE, "    %-25s %s", "clustersImportance", positions.clustersImportance);
        logAnalyze(BASE, "    %-25s %s", "missed", positions.missed);
        logAnalyze(BASE, "    %-25s %s", "missedImportance", positions.missedImportance);
    }

    boolean areTooMuchPositionsMissed() {
        boolean tooMuchMissed = missedTooMuch(this.forwardAnalyze.missed, this.patternChars.length);
        if ( tooMuchMissed ) {
            logAnalyze(BASE, "    %s, missed: %s to much, skip variant!", this.variantText, this.forwardAnalyze.missed);
        }
        return tooMuchMissed;
    }

    void sortPositions() {
        this.forwardAnalyze.sortPositions();
        if ( this.forwardAndReverseEqual ) {
            return;
        }
        this.reverseAnalyze.sortPositions();
    }
    
    void findPositionsClusters() {
        this.forwardAnalyze.analyzePositionsClusters();
        if ( this.forwardAndReverseEqual ) {
            return;
        }
        this.reverseAnalyze.analyzePositionsClusters();
    }
    
    boolean isVariantEqualsPattern() {
        return this.variantEqualsToPattern;
    }
    
    boolean isVariantNotEqualsPattern() {
        return ! this.variantEqualsToPattern;
    }

    void checkIfVariantTextContainsPatternDirectly() {
        if ( containsIgnoreCase(this.variantText, this.pattern) ) {
            double lengthRatio = patternLengthRatio(this.pattern);
            logAnalyze(BASE, "  variant contains pattern: weight -%s", lengthRatio);
            this.variantWeight = this.variantWeight - lengthRatio;
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
    }

    void setPatternCharsAndPositions() {
        this.patternChars = this.pattern.toCharArray();
        this.forwardAnalyze.positions = new int[this.patternChars.length];
        fill(this.forwardAnalyze.positions, POS_UNINITIALIZED);
        this.reverseAnalyze.positions = new int[this.patternChars.length];
        fill(this.reverseAnalyze.positions, POS_UNINITIALIZED);
    }
    
    private static double patternLengthRatio(String pattern) {
        return pattern.length() * 5.5;
    }

    void analyzePatternCharsPositions() {
        this.forwardAnalyze.findPatternCharsPositions();
        this.reverseAnalyze.findPatternCharsPositions();
        this.forwardAndReverseEqual = arePositionsEquals(this.forwardAnalyze, this.reverseAnalyze);
        if ( this.forwardAndReverseEqual ) {
            logAnalyze(BASE, "  FORWARD equals to REVERSE");
        }
    }

    void logUnsortedPositions() {
        this.logUnsortedPositionsOf(this.forwardAnalyze);
        if ( this.forwardAndReverseEqual ) {
            return;
        }
        this.logUnsortedPositionsOf(this.reverseAnalyze);
    }

    private void logUnsortedPositionsOf(AnalyzePositionsData data) {
        String positionsS = stream(data.positions)
                .mapToObj(position -> String.valueOf(position))
                .collect(joining(" "));
        logAnalyze(BASE, "  %s positions before sorting: %s", data.direction, positionsS);
    }
}
