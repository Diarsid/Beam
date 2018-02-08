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
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.UNINITIALIZED;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isVariantTextLengthTooBad;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedTooMuch;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.MathUtil.ratio;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
class AnalyzeData {
    
    final AnalyzePositionsData forwardAnalyze = new AnalyzePositionsData(this, FORWARD);
    final AnalyzePositionsData reverseAnalyze = new AnalyzePositionsData(this, REVERSE);
    
    AnalyzePositionsData best;
            
    WeightedVariant newVariant;
    WeightedVariant prevVariant;
    
    String variantText;
    
    double variantWeight;
    char[] patternChars;

    AnalyzeData() {
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
        logAnalyze("  weight before calculation: %s", this.variantWeight);
        double lengthDelta = ( this.variantText.length() - this.best.clustered ) * 0.4 ;
        this.variantWeight = this.variantWeight + (
                ( this.best.nonClustered * 2.3 )
                - ( this.best.clustered * 1.6 )
                - ( this.best.clustersImportance )
                + ( ratio(this.best.distanceBetweenClusters, this.variantText.length()) )
                + ( this.best.missedImportance )
                + ( lengthDelta )
                + ( this.best.unsortedImportance ) );
        int a = 5;
    }

    void calculateClustersImportance() {
        this.forwardAnalyze.calculateImportance();
        this.reverseAnalyze.calculateImportance();
    }
    
    AnalyzePositionsData bestPositions() {
        return this.forwardAnalyze.clustersImportance + ( this.forwardAnalyze.positionsWeight * -1.0d ) - this.forwardAnalyze.unsorted
                >= this.reverseAnalyze.clustersImportance + ( this.reverseAnalyze.positionsWeight * -1.0d ) - this.reverseAnalyze.unsorted ? 
                this.forwardAnalyze : this.reverseAnalyze;
    }

    boolean isVariantTooBad() {
        return isVariantTextLengthTooBad(this.variantWeight, this.variantText.length());
    }

    void isFirstCharMatchInVariantAndPattern(String pattern) {
        if ( pattern.charAt(0) == this.variantText.charAt(0) ) {            
            this.variantWeight = this.variantWeight - 3.4;            
        }
    }

    void logState() {
        AnalyzePositionsData positions = this.bestPositions();
        logAnalyze("  variant       : %s", this.variantText);
                
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
        logAnalyze("  pattern chars : %s", patternCharsString);
        logAnalyze("  positions     : %s", positionsString);
        logAnalyze("    %-25s %s", "direction", positions.direction);
        logAnalyze("    %-25s %s", "clusters", positions.clustersQty);
        logAnalyze("    %-25s %s", "clustered", positions.clustered);
        logAnalyze("    %-25s %s", "distance between clusters", positions.distanceBetweenClusters);
        logAnalyze("    %-25s %s", "separators between clusters", positions.separatorsBetweenClusters);
        logAnalyze("    %-25s %s", "nonClustered", positions.nonClustered);
        logAnalyze("    %-25s %s", "clustersImportance", positions.clustersImportance);
        logAnalyze("    %-25s %s", "missed", positions.missed);
        logAnalyze("    %-25s %s", "missedImportance", positions.missedImportance);
        logAnalyze("    %-25s %s", "unsorted", positions.unsorted);
        logAnalyze("    %-25s %s", "unsortedImportance", positions.unsortedImportance);
        logAnalyze("    %-25s %s", "total weight", this.variantWeight);
    }

    boolean areTooMuchPositionsMissed() {
        boolean tooMuchMissed = missedTooMuch(this.forwardAnalyze.missed, this.variantText.length());
        if ( tooMuchMissed ) {
            System.out.println(this.variantText + ", missed: " + this.forwardAnalyze.missed + " to much, skip variant!");
        }
        return tooMuchMissed;
    }

    void sortPositions() {
        this.forwardAnalyze.sortPositions();
        this.reverseAnalyze.sortPositions();
    }

    void countUnsortedPositions() {
        this.forwardAnalyze.countUnsortedPositions();
        this.reverseAnalyze.countUnsortedPositions();
    }

    void setVariantText(Variant variant) {
        this.variantText = lower(variant.text());
    }
    
    void findPositionsClusters() {
        this.forwardAnalyze.analyzePositionsClusters();
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
    }

    void logUnsortedPositions() {
        this.logUnsortedPositionsOf(this.forwardAnalyze);
        this.logUnsortedPositionsOf(this.reverseAnalyze);
    }

    private void logUnsortedPositionsOf(AnalyzePositionsData data) {
        String positionsS = stream(data.positions)
                .mapToObj(position -> String.valueOf(position))
                .collect(joining(" "));
        logAnalyze("  %s positions before sorting: %s", data.direction, positionsS);
    }
    
    void clearAnalyze() {
        this.forwardAnalyze.clearPositionsAnalyze();
        this.reverseAnalyze.clearPositionsAnalyze();
        this.best = null;
        this.variantText = "";
        this.variantWeight = 0;
        this.newVariant = null;
        this.prevVariant = null;
    }
    
    void strangeConditionOnUnsorted() {
        this.forwardAnalyze.strangeConditionOnUnsorted();
        this.reverseAnalyze.strangeConditionOnUnsorted();
    }
}
