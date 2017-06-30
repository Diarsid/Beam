/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.patternsanalyze;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.clusterWeightRatioDependingOn;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.countUsorted;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.firstCharMatchRatio;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.isVariantTextLengthTooBad;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.isWordsSeparator;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.missedImportanceDependingOn;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.missedTooMuch;
import static diarsid.beam.core.domain.patternsanalyze.AnalyzeUtil.sortingStepsImportanceDependingOn;

/**
 *
 * @author Diarsid
 */
class AnalyzeData {
    
    Map<Character, Integer> reusableVisitedChars = new HashMap<>();
    WeightedVariant newVariant;
    WeightedVariant prevVariant;
    String variantText;
    
    int[] positions;
    double variantWeight;
    char[] patternChars;
    int currentCharPosition;
    int betterCurrentCharPosition;
    char currentChar;

    int unsorted;
    int missed;
    int clustersQty;
    int clustered;
    int nonClustered;
    int clustersWeight;
    double clustersImportance;
    double missedImportance;
    double sortingStepsImportance;
    int currentClusterLength;
    int currentPosition;
    int nextPosition;
    boolean clusterContinuation;
    boolean containsFirstChar;
    boolean firstCharsMatchInVariantAndPattern;

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
        this.nonClustered = this.nonClustered + this.missed;
        this.clustersImportance = clustersImportanceDependingOn(this.clustersQty, this.clustered, this.nonClustered);
        this.missedImportance = missedImportanceDependingOn(this.missed, this.clustersImportance);
        this.sortingStepsImportance = sortingStepsImportanceDependingOn(this.unsorted, this.clustersImportance);
        this.variantWeight = this.variantWeight + (
                ( this.nonClustered * 5.3 )
                - ( this.clustered * 4.6 )
                - ( firstCharMatchRatio(this.containsFirstChar) )
                - ( this.clustersImportance )
                + ( this.clustersWeight * clusterWeightRatioDependingOn(
                        this.containsFirstChar,
                        this.firstCharsMatchInVariantAndPattern) )
                - ( this.clustersQty * 5.4 )
                + ( this.missedImportance )
                + (( this.variantText.length() - this.clustered ) * 0.8 )
                + ( this.sortingStepsImportance ) );
    }

    boolean isVariantTooBad() {
        return isVariantTextLengthTooBad(this.variantWeight, this.variantText.length());
    }

    void isFirstCharMatchInVariantAndPattern(String pattern) {
        this.firstCharsMatchInVariantAndPattern = ( pattern.charAt(0) == this.variantText.charAt(0) );
    }

    void logState() {
        System.out.println(this.variantText + ", positions: " + stream(this.positions).mapToObj(position -> String.valueOf(position)).collect(joining(" ")));
        System.out.println(String.format("   %-15s %s", "clusters", this.clustersQty));
        System.out.println(String.format("   %-15s %s", "clustered", this.clustered));
        System.out.println(String.format("   %-15s %s", "clusters weight", this.clustersWeight));
        System.out.println(String.format("   %-15s %s", "clusters importance", this.clustersImportance));
        System.out.println(String.format("   %-15s %s", "non clustered", this.nonClustered));
        System.out.println(String.format("   %-15s %s", "missed", this.missed));
        System.out.println(String.format("   %-15s %s", "missedImportance", this.missedImportance));
        System.out.println(String.format("   %-15s %s", "sortSteps", this.unsorted));
        System.out.println(String.format("   %-15s %s", "sortStepsImportance", this.sortingStepsImportance));
        System.out.println(String.format("   %-15s %s", "total weight", this.variantWeight));
    }

    void strangeConditionOnUnsorted() {
        if ( ( this.clustered < 2 ) && ( this.unsorted > 0 ) ) {
            this.variantWeight = this.variantWeight * 1.8;
        }
    }

    boolean areTooMuchPositionsMissed() {
        return missedTooMuch(this.missed, this.variantText.length());
    }

    boolean isCurrentPositionNotMissed() {
        return this.currentPosition > 0;
    }

    void clusterEnds() {
        this.clustered++;
        this.clusterContinuation = false;
    }

    void newClusterStarts() {
        this.clustered++;
        this.clustersQty++;
        this.clusterContinuation = true;
        this.currentClusterLength = 1;
        this.clustersWeight = this.clustersWeight + ( this.currentPosition / 2 );
        if ( this.isCurrentPositionNotMissed() ) {
            if ( this.isPreviousCharWordSeparator() ) {
                this.variantWeight = this.variantWeight - 4;
            }
        }
    }

    boolean isPreviousCharWordSeparator() {
        return isWordsSeparator(this.variantText.charAt(this.currentPosition - 1));
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

    boolean isCurrentPositionAtPatternStart() {
        return this.currentPosition == 0;
    }

    boolean isCurrentPositionMissed() {
        return this.currentPosition < 0;
    }

    void setCurrentPosition(int i) {
        this.currentPosition = this.positions[i];
    }

    void clearClustersInfo() {
        this.missed = 0;
        this.variantWeight = 0;
        this.clustersQty = 0;
        this.clustered = 0;
        this.nonClustered = 0;
        this.clustersWeight = 0;
        this.currentClusterLength = 0;
        this.clusterContinuation = false;
        this.containsFirstChar = false;
    }

    void sortPositions() {
        Arrays.sort(this.positions);
    }

    void countUnsortedPositions() {
        this.unsorted = countUsorted(this.positions);
    }

    void saveCurrentCharFinalPosition(int currentCharIndex) {
        this.positions[currentCharIndex] = this.currentCharPosition;
    }

    void findBetterCurrentCharPositionFromPreviousCharPosition(int currentCharIndex) {
        this.betterCurrentCharPosition = this.variantText.indexOf(this.currentChar, this.positions[currentCharIndex - 1] + 1);
    }

    boolean isBetterCharPositionFoundAndInCluster(int currentCharIndex) {
        return isBetterCharPositionFound() && isCurrentCharBetterPostionInCluster(currentCharIndex);
    }

    void replaceCurrentPositionWithBetterPosition() {
        System.out.println(format("assign position of '%s' in '%s' as %s instead of %s", this.currentChar, this.variantText, this.betterCurrentCharPosition, this.currentCharPosition));
        this.currentCharPosition = this.betterCurrentCharPosition;
    }

    boolean isCurrentCharBetterPostionInCluster(int currentCharIndex) {
        return this.betterCurrentCharPosition == this.positions[currentCharIndex - 1] + 1;
    }

    boolean isBetterCharPositionFound() {
        return this.betterCurrentCharPosition > -1;
    }

    void findBetterCurrentCharPosition() {
        this.betterCurrentCharPosition =
                this.variantText.indexOf(this.currentChar, this.currentCharPosition + 1);
    }

    boolean currentCharIndexInRange(int currentCharIndex) {
        return ( currentCharIndex > 0 ) && ( this.currentCharPosition < this.positions[currentCharIndex - 1] );
    }

    boolean currentCharFound() {
        return this.currentCharPosition > -1;
    }

    void currentCharIs(int currentCharIndex) {
        this.currentChar = this.patternChars[currentCharIndex];
    }
    
    boolean isCurrentCharAlreadyVisited() {
        return this.reusableVisitedChars.containsKey(this.currentChar);
    }

    void setCurrentCharPosition() {
        this.currentCharPosition = this.variantText.indexOf(this.currentChar);
    }

    void setCurrentCharPositionNextFoundAfterLastVisitedOne() {
        this.currentCharPosition = this.variantText.indexOf(
                this.currentChar,
                reusableVisitedChars.get(this.currentChar) + 1);
    }

    void addCurrentCharToVisited() {
        this.reusableVisitedChars.put(this.currentChar, this.currentCharPosition);
    }
    
    void findClusters() {
        clustersCounting: for (int i = 0; i < this.positions.length; i++) {
            this.setCurrentPosition(i);
            if ( this.isCurrentPositionMissed() ) {
                this.missed++;
                continue clustersCounting;
            }
            if ( this.isCurrentPositionAtPatternStart() ) {
                this.containsFirstChar = true;
            }
            if ( this.hasNextPosition(i)) {
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
                        this.nonClustered++;
                    }
                }
            } else {
                if ( this.isCurrentPositionNotMissed() ) {
                    if ( this.isPreviousCharWordSeparator() ) {
                        this.variantWeight = this.variantWeight - 3;
                    }
                }
                if ( this.clusterContinuation ) {
                    this.clustered++;
                } else {
                    this.nonClustered++;
                }
            }
        }
    }
}
