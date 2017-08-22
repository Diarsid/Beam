/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.patternsanalyze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.patternsanalyze.AnalyzePositionsData.AnalyzePositionsDirection.FORWARD;
import static diarsid.beam.core.base.patternsanalyze.AnalyzePositionsData.AnalyzePositionsDirection.REVERSE;
import static diarsid.beam.core.base.patternsanalyze.AnalyzePositionsData.UNINITIALIZED;
import static diarsid.beam.core.base.patternsanalyze.AnalyzeUtil.isVariantTextLengthTooBad;
import static diarsid.beam.core.base.patternsanalyze.AnalyzeUtil.missedTooMuch;

/**
 *
 * @author Diarsid
 */
class AnalyzeData {
    
    final Map<Character, Integer> reusableVisitedChars = new HashMap<>();    
    final Set<Character> skippedChars = new HashSet<>();
    final AnalyzePositionsData forward = new AnalyzePositionsData(this, FORWARD);
    final AnalyzePositionsData reverse = new AnalyzePositionsData(this, REVERSE);
    
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
        this.forward.calculateImportance();
        this.reverse.calculateImportance();
    }
    
    AnalyzePositionsData bestPositions() {
        return this.forward.clustersImportance + ( this.forward.positionsWeight * -1.0d ) - this.forward.unsorted
                > this.reverse.clustersImportance + ( this.reverse.positionsWeight * -1.0d ) - this.reverse.unsorted ? 
                this.forward : this.reverse;
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
        boolean tooMuchMissed = missedTooMuch(this.forward.missed, this.variantText.length());
        if ( tooMuchMissed ) {
            System.out.println(this.variantText + ", missed: " + this.forward.missed + " to much, skip variant!");
        }
        return tooMuchMissed;
    }

    void sortPositions() {
        this.forward.sortPositions();
        this.reverse.sortPositions();
    }

    void countUnsortedPositions() {
        this.forward.countUnsortedPositions();
        this.reverse.countUnsortedPositions();
    }

    void setVariantText(Variant variant) {
        this.variantText = lower(variant.text());
    }
    
    void findPositionsClusters() {
        this.forward.findPositionsClusters();
        this.reverse.findPositionsClusters();
    }

    void checkIfVariantTextContainsPatternDirectly(String pattern) {
        if ( containsIgnoreCase(this.variantText, pattern) ) {
            debug("variant contains pattern!");
            this.variantWeight = this.variantWeight - patternLengthRatio(pattern);
        }
    }

    void setPatternCharsAndPositions(String pattern) {
        this.patternChars = pattern.toCharArray();
        this.forward.positions = new int[this.patternChars.length];
        fill(this.forward.positions, UNINITIALIZED);
        this.reverse.positions = new int[this.patternChars.length];
        fill(this.reverse.positions, UNINITIALIZED);
    }
    
    private static double patternLengthRatio(String pattern) {
        return pattern.length() * 5.5;
    }

    void findPatternCharsPositions() {
        this.forward.findPatternCharsPositions();
        this.reverse.findPatternCharsPositions();
    }

    void logUnsortedPositions() {
        this.logUnsortedPositionsOf(this.forward);
        this.logUnsortedPositionsOf(this.reverse);
    }

    private void logUnsortedPositionsOf(AnalyzePositionsData data) {
        String positionsS = stream(data.positions)
                .mapToObj(position -> String.valueOf(position))
                .collect(joining(" "));
        System.out.println(data.direction + " positions before sorting: " + positionsS);
    }
    
    void clearAnalyze() {
        this.forward.clearPositionsAnalyze();
        this.reverse.clearPositionsAnalyze();
        this.best = null;
        this.variantText = "";
        this.variantWeight = 0;
        this.newVariant = null;
        this.prevVariant = null;
    }
    
    void strangeConditionOnUnsorted() {
        this.forward.strangeConditionOnUnsorted();
        this.reverse.strangeConditionOnUnsorted();
    }
}
