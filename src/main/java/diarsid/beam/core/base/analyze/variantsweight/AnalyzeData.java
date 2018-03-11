/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.Map;

import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.BASE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.UNINITIALIZED;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.arePositionsEquals;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.lengthImportanceRatio;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedTooMuch;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.BAD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimate;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.MathUtil.ratio;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
class AnalyzeData {
    
    final AnalyzePositionsData forwardAnalyze;
    final AnalyzePositionsData reverseAnalyze;
    private boolean forwardAndReverseEqual;
    
    AnalyzePositionsData best;
            
    WeightedVariant newVariant;
    WeightedVariant prevVariant;
    
    String variantText;
    
    double variantWeight;
    double lengthDelta;
    double distanceBetweenClustersImportance;
    
    char[] patternChars;

    AnalyzeData() {
        this.forwardAnalyze = new AnalyzePositionsData(this, FORWARD);
        this.reverseAnalyze = new AnalyzePositionsData(this, REVERSE);
        this.forwardAndReverseEqual = false;
    }

    boolean isNewVariantBetterThanPrevious() {
        return this.newVariant.betterThan(this.prevVariant);
    }

    void setPreviousVariantWithSameDisplayText(Map<String, WeightedVariant> variantsByDisplay) {
        this.prevVariant = variantsByDisplay.get(lower(this.newVariant.displayText()));
    }

    void setNewVariant(Variant variant) {
        // weight calculation ends
        this.newVariant = new WeightedVariant(variant, this.variantWeight);
    }

    void calculateWeight() {        
        this.best = this.bestPositions();
        this.variantWeight = this.variantWeight + this.best.positionsWeight;
        logAnalyze(BASE, "  weight on step 1: %s", this.variantWeight);
        if ( this.variantWeight > 0 ) {
            this.best.badReason = "preliminary position calculation is too bad";
            return;
        }
        if ( ratio(this.best.nonClustered, this.patternChars.length) > 0.4 ) {
            this.best.badReason = "Too much unclustered positions";
            return;
        }
        
        double lengthImportance = lengthImportanceRatio(this.variantText.length());
        this.distanceBetweenClustersImportance = ratio(this.best.distanceBetweenClusters, this.variantText.length()) * 15 * lengthImportance;
        this.lengthDelta = ( this.variantText.length() - this.best.clustered ) * 0.4 * lengthImportance;
        
        this.variantWeight = this.variantWeight + (
                ( this.best.nonClusteredImportance )
                - ( this.best.clustersImportance )
                + ( this.distanceBetweenClustersImportance )
                + ( this.best.missedImportance )
                + ( this.lengthDelta ) 
        );
        logAnalyze(BASE, "  weight on step 2: %s", this.variantWeight);
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

    boolean isVariantTooBad() {
        return nonEmpty(this.best.badReason) || estimate(this.variantWeight).equals(BAD);
    }

    void isFirstCharMatchInVariantAndPattern(String pattern) {
        if ( pattern.charAt(0) == this.variantText.charAt(0) ) {            
            this.variantWeight = this.variantWeight - 3.4;            
        }
    }

    void logState() {
        AnalyzePositionsData positions = this.bestPositions();
        logAnalyze(BASE, "  variant       : %s", this.variantText);
                
        String patternCharsString = stream(positions.positions)
                .mapToObj(position -> {
                    if ( position < 0 ) {
                        return "*";
                    } else {
                        return String.valueOf(this.variantText.charAt(position));
                    }                    
                })
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        String positionsString =  stream(positions.positions)
                .mapToObj(position -> String.valueOf(position))
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        logAnalyze(BASE, "  pattern chars : %s", patternCharsString);
        logAnalyze(BASE, "  positions     : %s", positionsString);
        logAnalyze(BASE, "    %-25s %s", "direction", positions.direction);
        logAnalyze(BASE, "    %-25s %s", "clusters", positions.clustersQty);
        logAnalyze(BASE, "    %-25s %s", "clustered", positions.clustered);
        logAnalyze(BASE, "    %-25s %s", "length delta", this.lengthDelta);
        logAnalyze(BASE, "    %-25s %s", "distance between clusters", positions.distanceBetweenClusters);
        logAnalyze(BASE, "    %-25s %s", "distance between clusters importance", this.distanceBetweenClustersImportance);
        logAnalyze(BASE, "    %-25s %s", "separators between clusters", positions.separatorsBetweenClusters);
        logAnalyze(BASE, "    %-25s %s", "nonClustered", positions.nonClustered);
        logAnalyze(BASE, "    %-25s %s", "nonClusteredImportance", positions.nonClusteredImportance);
        logAnalyze(BASE, "    %-25s %s", "clustersImportance", positions.clustersImportance);
        logAnalyze(BASE, "    %-25s %s", "missed", positions.missed);
        logAnalyze(BASE, "    %-25s %s", "missedImportance", positions.missedImportance);
        logAnalyze(BASE, "    %-25s %s", "total weight", this.variantWeight);
        if ( nonEmpty(this.best.badReason) ) {
            logAnalyze(BASE, "    %-25s %s", "bad reason", this.best.badReason);
        }
    }

    boolean areTooMuchPositionsMissed() {
        boolean tooMuchMissed = missedTooMuch(this.forwardAnalyze.missed, this.patternChars.length);
        if ( tooMuchMissed ) {
            System.out.println(this.variantText + ", missed: " + this.forwardAnalyze.missed + " to much, skip variant!");
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

    void setVariantText(Variant variant) {
        this.variantText = lower(variant.text());
    }
    
    void findPositionsClusters() {
        this.forwardAnalyze.analyzePositionsClusters();
        if ( this.forwardAndReverseEqual ) {
            return;
        }
        this.reverseAnalyze.analyzePositionsClusters();
    }

    void checkIfVariantTextContainsPatternDirectly(String pattern) {
        if ( containsIgnoreCase(this.variantText, pattern) ) {
            debug("variant contains pattern!");
            this.variantWeight = this.variantWeight - patternLengthRatio(pattern);
        }
    }

    void setPatternCharsAndPositions(String pattern) {
        this.patternChars = pattern.toCharArray();
        this.forwardAnalyze.positions = new int[this.patternChars.length];
        fill(this.forwardAnalyze.positions, UNINITIALIZED);
        this.reverseAnalyze.positions = new int[this.patternChars.length];
        fill(this.reverseAnalyze.positions, UNINITIALIZED);
    }
    
    private static double patternLengthRatio(String pattern) {
        return pattern.length() * 5.5;
    }

    void analyzePatternCharsPositions() {
        this.forwardAnalyze.findPatternCharsPositions();
        this.reverseAnalyze.findPatternCharsPositions();
        this.forwardAndReverseEqual = arePositionsEquals(this.forwardAnalyze, this.reverseAnalyze);
        if ( this.forwardAndReverseEqual ) {
            logAnalyze(BASE, "  FORWARD == REVERSE");
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
    
    void clearAnalyze() {
        this.forwardAnalyze.clearPositionsAnalyze();
        this.reverseAnalyze.clearPositionsAnalyze();
        this.best = null;
        this.variantText = "";
        this.variantWeight = 0;
        this.lengthDelta = 0;
        this.distanceBetweenClustersImportance = 0;
        this.newVariant = null;
        this.prevVariant = null;
        this.forwardAndReverseEqual = false;
    }
    
}
