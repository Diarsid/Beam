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

import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.UNINITIALIZED;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.isVariantTextLengthTooBad;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.missedTooMuch;
import static diarsid.beam.core.base.util.Logs.debug;
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
        System.out.println("weight before calculation: " + this.variantWeight);
        double lengthDelta = ( this.variantText.length() - this.best.clustered ) * 0.8 ;
        this.variantWeight = this.variantWeight + (
                ( this.best.nonClustered * 5.3 )
                - ( this.best.clustered * 4.6 )
                - ( this.best.clustersImportance )
                + ( this.best.missedImportance )
                + ( lengthDelta )
                + ( this.best.unsortedImportance ) );
    }

    void calculateClustersImportance() {
        this.forwardAnalyze.calculateImportance();
        this.reverseAnalyze.calculateImportance();
    }
    
    AnalyzePositionsData bestPositions() {
        return this.forwardAnalyze.clustersImportance + ( this.forwardAnalyze.positionsWeight * -1.0d ) - this.forwardAnalyze.unsorted
                > this.reverseAnalyze.clustersImportance + ( this.reverseAnalyze.positionsWeight * -1.0d ) - this.reverseAnalyze.unsorted ? 
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
        System.out.println(this.variantText + ", positions: " + stream(positions.positions).mapToObj(position -> String.valueOf(position)).collect(joining(" ")));
        System.out.println(String.format("   %-20s %s", "direction", positions.direction));
        System.out.println(String.format("   %-20s %s", "clusters", positions.clustersQty));
        System.out.println(String.format("   %-20s %s", "clustered", positions.clustered));
        System.out.println(String.format("   %-20s %s", "nonClustered", positions.nonClustered));
        System.out.println(String.format("   %-20s %s", "clustersImportance", positions.clustersImportance));
        System.out.println(String.format("   %-20s %s", "missed", positions.missed));
        System.out.println(String.format("   %-20s %s", "missedImportance", positions.missedImportance));
        System.out.println(String.format("   %-20s %s", "unsorted", positions.unsorted));
        System.out.println(String.format("   %-20s %s", "unsortedImportance", positions.unsortedImportance));
        System.out.println(String.format("   %-20s %s", "total weight", this.variantWeight));
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
        this.forwardAnalyze.findPositionsClusters();
        this.reverseAnalyze.findPositionsClusters();
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

    void findPatternCharsPositions() {
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
        System.out.println(data.direction + " positions before sorting: " + positionsS);
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
