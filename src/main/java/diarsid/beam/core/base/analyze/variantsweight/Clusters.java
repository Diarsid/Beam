/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.AnalyzePositionsDirection;
import diarsid.support.objects.Possible;
import diarsid.support.objects.StatefulClearable;

import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.beam.core.base.util.CollectionsUtils.lastFrom;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.MathUtil.mean;
import static diarsid.beam.core.base.util.MathUtil.percentAsFloat;
import static diarsid.beam.core.base.util.MathUtil.percentAsFloatOf;
import static diarsid.beam.core.base.util.MathUtil.percentAsInt;
import static diarsid.beam.core.base.util.MathUtil.percentAsIntOf;
import static diarsid.support.objects.Pools.giveBackAllToPoolAndClear;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
class Clusters implements StatefulClearable {
    
    private static final int UNKNOWN_VALUE;
    private static final int MAX_PLACING_BONUS;
    
    static {
        UNKNOWN_VALUE = -9;
        MAX_PLACING_BONUS = 20;
    }
    
    /* Clusters variables block */
    private final List<Cluster> clusters;
    private final Possible<Cluster> lastAdded;
    private final AnalyzeData data;
    private final AnalyzePositionsDirection direction;
    private boolean arranged;
    private int clustersTotalLength;
    private int distanceBetweenClusters;
    private boolean allClustersSeparatedByOneChar;

    /* Placing variables block */
    final Possible<String> placingCase;
    final Possible<String> placingBonusNotApplicableReason;
    int clustersPercentInVariant;        
    int meanPosition = UNKNOWN_VALUE;
    int adjustedVariantLength = UNKNOWN_VALUE;
    float placingPercent = UNKNOWN_VALUE;
    float clustersPlacingImportance = UNKNOWN_VALUE;
    float distanceBetweenClustersImportance = UNKNOWN_VALUE;
    float placingBonus;
    float placingBonusLimit;

    Clusters(AnalyzeData analyzeData, AnalyzePositionsDirection direction) {
        this.data = analyzeData;
        this.direction = direction;
        this.clusters = new ArrayList<>();
        this.lastAdded = possibleButEmpty();
        this.arranged = false;
        this.clustersTotalLength = 0;
        this.distanceBetweenClusters = 0;
        this.allClustersSeparatedByOneChar = false;        
        
        this.clustersPercentInVariant = UNKNOWN_VALUE;        
        this.meanPosition = UNKNOWN_VALUE;
        this.adjustedVariantLength = UNKNOWN_VALUE;
        this.placingPercent = UNKNOWN_VALUE;
        this.clustersPlacingImportance = UNKNOWN_VALUE;
        this.distanceBetweenClustersImportance = UNKNOWN_VALUE;
        this.placingBonus = UNKNOWN_VALUE;
        this.placingBonusLimit = UNKNOWN_VALUE;
        this.placingCase = possibleButEmpty();
        this.placingBonusNotApplicableReason = possibleButEmpty();
    }
    
    void add(Cluster cluster) {
        if ( this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to add next cluster after arrengment!");
        }
        this.lastAdded.resetTo(cluster);
        this.clusters.add(cluster);
    }
    
    List<Cluster> all() {
        return this.clusters;
    }
    
    int distanceBetweenClusters() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to get distance between clusters before arrangement!");
        }
        
        if ( this.isEmpty() ) {
            return 0;
        } 
        
        return this.distanceBetweenClusters;
    }
    
    int totalLength() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to get total length before arrangement!");
        }    
        
        return this.clustersTotalLength;
    }
    
    private int findMeanPosition() {
        if ( this.isEmpty() ) {
            return 0;
        }
        
        if ( this.quantity() == 1 ) {
            return this.lastAdded.orThrow().positionsMean();
        }
        
        int middlePositionsSum = this.clusters
                .stream()
                .mapToInt(cluster -> cluster.positionsMean())
                .sum();
        
        return middlePositionsSum / this.clusters.size();
    }
    
    private int findMeanFirstPosition() {
        if ( this.isEmpty() ) {
            return 0;
        }
        
        if ( this.quantity() == 1 ) {
            return this.lastAdded.orThrow().firstPosition();
        }
        
        int middlePositionsSum = this.clusters
                .stream()
                .mapToInt(cluster -> cluster.firstPosition())
                .sum();
        
        return middlePositionsSum / this.clusters.size();
    }
    
    private int findMeanLastPosition() {
        if ( this.isEmpty() ) {
            return 0;
        }
        
        if ( this.quantity() == 1 ) {
            return this.lastAdded.orThrow().lastPosition();
        }
        
        int middlePositionsSum = this.clusters
                .stream()
                .mapToInt(cluster -> cluster.lastPosition())
                .sum();
        
        return middlePositionsSum / this.clusters.size();
    }
    
    Cluster lastAddedCluster() {
        return this.lastAdded.orThrow();
    }
    
    Cluster lastCluster() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to get last cluster before arrangement!");
        }
        
        return lastFrom(this.clusters);
    }
    
    boolean isEmpty() {
        return this.clusters.isEmpty();
    }
    
    boolean nonEmpty() {
        return ! this.isEmpty();
    }
    
    int quantity() {
        return this.clusters.size();
    }
    
    void arrange() {
        if ( this.arranged ) {
            return;
        }
        
        Collections.sort(this.clusters);
        this.arranged = true;
        
        this.loopThroughClustersAndCollectData();
    }
    
    private void loopThroughClustersAndCollectData() {
        if ( this.quantity() < 2 ) {
            this.distanceBetweenClusters = 0;
            this.allClustersSeparatedByOneChar = false;
            if ( this.nonEmpty() ) {
                this.clustersTotalLength = this.lastAdded.orThrow().length();
            }           
            return;
        }
        
        this.allClustersSeparatedByOneChar = true;
        
        Cluster currentCluster = this.clusters.get(0);
        int prevClusterEnd = currentCluster.lastPosition();
        int totalLength = currentCluster.length();
        int nextClusterStart;
        int distanceBetweenTwoClusters;        
        
        for (int i = 1; i < this.clusters.size(); i++) {
            currentCluster = this.clusters.get(i);    
            totalLength += currentCluster.length();
            nextClusterStart = currentCluster.firstPosition();
            distanceBetweenTwoClusters = absDiff(prevClusterEnd, nextClusterStart) - 1;
            
            if ( distanceBetweenTwoClusters != 1 ) {
                this.allClustersSeparatedByOneChar = false;
            }
            this.distanceBetweenClusters += distanceBetweenTwoClusters;
            
            prevClusterEnd = currentCluster.lastPosition();
        }
        
        this.clustersTotalLength = totalLength;
    }
    
    boolean areAllClustersSeparatedByOneChar() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to query if all clusters " +
                    "separated by one char before arrangement!");
        }
        
        return this.allClustersSeparatedByOneChar;
    }
    
    float calculatePlacingBonus() {        
        this.clustersPercentInVariant = percentAsInt(this.totalLength(), this.data.variantText.length()); 
        
        if ( this.data.variantPathSeparators.isEmpty() ) {
            if ( this.quantity() == 1 ) {
                if ( this.isFirstClusterAtVariantStart() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.bestPlacing();
                    } else {
                        this.bestPlacingPercent(97, "single cluster at variant start, no separators");
                    }
                } else {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.singleMajorClosierToStartIsBetter();
                    } else {
                        this.singleClosierToStartIsBetter();
                    }                    
                }
            } else {
                if ( this.areAllClustersSeparatedByOneChar() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.bestPlacingPercent(90, "all clusters-as-one at variant start, forming majority, no separators");
                        } else {
                            this.manyAsOneMajorClosierToStartAreBetter();
                        }
                    } else {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.bestPlacingPercent(85, "all clusters-as-one at variant start, no separators");
                        } else {
                            this.manyAsOneClosierToStartAreBetter();
                        }
                    }                    
                } else {
                    if ( this.isClusteredPartFormingMajority() ) {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.manyMajorClosierToStartAreBetterFirstAtStart();
                        } else {
                            this.manyMajorClosierToStartAreBetter();
                        }
                    } else {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.manyClosierToStartAreBetterFirstAtStart();
                        } else {
                            this.manyClosierToStartAreBetter();
                        }
                    }                    
                }                
            }
        } else {
            /* variant contains path separators */
            if ( this.quantity() == 1 ) {
                if ( this.isLastClusterAtVariantEnd() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.bestPlacingPercent(95, "single cluster at variant start, forming majority, with separators");
                    } else {
                        this.bestPlacingPercent(90, "single cluster at variant start, with separators");
                    }
                } else if ( this.isSingleClusterAfterLastPathSeparator() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.singleMajorClosierToEndIsBetterAfterLastSeparator();
                    } else {
                        this.singleClosierToEndIsBetterAfterLastSeparator();
                    }
                } else if ( this.areAllClustersBeforeFirstPathSeparator() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.singleMajorClosierToStartIsBetterBeforeFirstPathSeparator();
                    } else {
                        this.singleClosierToStartIsBetterBeforeFirstPathSeparator();
                    }
                } else {
                    this.placingBonusNoMoreThanPercent(80);
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.singleMajorClosierToEndIsBetter();
                    } else {
                        this.singleClosierToEndIsBetter();
                    }
                }
            } else {
                /* many clusters */
                if ( this.areAllClustersSeparatedByOneChar() ) {
                    if ( this.isLastClusterAtVariantEnd() ) {
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.bestPlacingPercent(90, "all clusters-as-one at variant end, forming majority, with separators");
                        } else {
                            this.bestPlacingPercent(85, "all clusters-as-one at variant end, with separators");
                        }
                    } else if ( this.areAllClustersAfterLastPathSeparator() ) {
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyAsOneMajorClosierToEndAreBetterAfterLastPathSeparator();
                        } else {
                            this.manyAsOneClosierToEndAreBetterAfterLastPathSeparator();
                        }
                    } else if ( this.areAllClustersBeforeFirstPathSeparator() ) {
                        this.placingBonusNoMoreThanPercent(60);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyAsOneMajorClosierToStartAreBetterBeforeFirstPathSeparator();
                        } else {
                            this.manyAsOneClosierToStartAreBetterBeforeFirstPathSeparator();
                        }
                    } else {
                        this.placingBonusNoMoreThanPercent(75);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyAsOneMajorClosierToEndAreBetter();
                        } else {
                            this.manyAsOneClosierToEndAreBetter();
                        }
                    }
                } else {
                    /* no many-as-one clusters */
                    if ( this.isLastClusterAtVariantEnd() ) {
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyMajorClosierToEndAreBetterLastAtEnd();
                        } else {
                            this.manyClosierToEndAreBetterLastAtEnd();
                        }
                    } else if ( this.areAllClustersAfterLastPathSeparator() ) {
                        this.placingBonusNoMoreThanPercent(70);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyMajorClosierToEndAreBetterAfterLastPathSeparator();
                        } else {
                            this.placingBonusNoMoreThanPercent(65);
                            this.manyClosierToEndAreBetterAfterLastPathSeparator();
                        }
                    } else if ( this.areAllClustersBeforeFirstPathSeparator() ) {
                        this.placingBonusNoMoreThanPercent(50);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyMajorClosierToStartAreBetterBeforeFirstPathSeparator();
                        } else {
                            this.manyClosierToStartAreBetterBeforeFirstPathSeparator();
                        }
                    } else {
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.placingBonusNoMoreThanPercent(65);
                            this.manyMajorClosierToEndAreBetter();
                        } else {
                            this.placingBonusNoMoreThanPercent(50);
                            this.manyClosierToEndAreBetter();
                        }
                    }
                }
            } 
        } 
        
        this.placingBonusMustBePresent();
        this.applyBonusHigherLimitationIfSpecified();
        this.subtractMissedPositionsFromBonus();
        this.subtractSeparatorsBetweenClusters();
        this.logState();        
        
        return this.placingBonus;
    }

    private void placingBonusMustBePresent() throws IllegalStateException {
        if ( this.placingBonus == UNKNOWN_VALUE ) {
            throw new IllegalStateException("Placing bonus have not been calculated!");
        }
    }
    
    private void applyBonusHigherLimitationIfSpecified() {
        if ( this.placingBonusLimit != UNKNOWN_VALUE ) {
            if ( this.placingBonus > this.placingBonusLimit ) {
                logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] WARN!!! placing bonus limit applied : placing bonus is %s but limit is %s", this.placingBonus, this.placingBonusLimit);     
                this.placingBonus = this.placingBonusLimit;
            }
        }
    }
    
    private void subtractMissedPositionsFromBonus() {
        AnalyzePositionsData positionsData = this.data.positionsOf(this.direction);
        if ( positionsData.missed == 0 ) {
            return;
        }
        
        float presentPercent = percentAsFloat(this.data.patternChars.length - positionsData.missed, this.data.patternChars.length);
        
        logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] missed position placing penalty : *%s%%", presentPercent);     
        this.placingBonus = percentAsFloatOf(this.placingBonus, presentPercent);
        
        if ( this.placingBonus < 0 ) {
            this.placingBonus = 0;
        }
    }
    
    private void subtractSeparatorsBetweenClusters() {
        if ( this.isEmpty() ) {
            return;
        }
        
        if ( this.quantity() == 1 ) {
            return;
        }
        
        int separatorsBetweenClustersPenalty = this.data.positionsOf(direction).separatorsBetweenClusters - (this.quantity() - 1);
        
        if ( separatorsBetweenClustersPenalty <= 0 ) {
            return;
        }
        
        this.placingBonus = this.placingBonus - separatorsBetweenClustersPenalty;
        
        if ( this.placingBonus < 0 ) {
            this.placingBonus = 0;
        }
    }
    
    private void placingBonusNoMoreThanPercent(int percent) {
        this.placingBonusLimit = (MAX_PLACING_BONUS * (float) percent) / 100;
        logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] placing bonus limit : %s (%s%%)", this.placingBonusLimit, percent);        
    }

    private void placingPercentNoHigherThan(int limit) {
        if ( this.placingPercent > limit ) {
            this.placingPercent = limit;
        }
    }
    
    private void clustersPlacingImportancePercentNoHigherThan(int limit) {
        if ( this.clustersPlacingImportance > limit ) {
            this.clustersPlacingImportance = limit;
        }
    }
    
    private void clustersPlacingImportancePercentNoLowerThan(int limit) {
        if ( this.clustersPlacingImportance < limit ) {
            this.clustersPlacingImportance = limit;
        }
    }
    
    private boolean isClusteredPartFormingMajority() {
        return this.clustersPercentInVariant >= 66;
    }
    
    private boolean isFirstClusterAtVariantStart() {
        return this.firstCluster().firstPosition() == 0;
    }
    
    private boolean isSingleClusterAfterLastPathSeparator() {
        return this.quantity() == 1 && 
               this.firstCluster().firstPosition() > this.data.variantPathSeparators.last();
    }

    private Cluster firstCluster() {
        return this.clusters.get(0);
    }
    
    private boolean areAllClustersAfterLastPathSeparator() {
        return this.firstCluster().firstPosition() > this.data.variantPathSeparators.last();
    }
    
    private boolean areAllClustersBeforeFirstPathSeparator() {
        return this.lastCluster().lastPosition() < this.data.variantPathSeparators.first();
    }
    
    private void bestPlacing() {
        this.placingCase.resetTo("single cluster at variant start, forming majority, no separators");
        this.placingBonus = MAX_PLACING_BONUS;
    }
    
    private void bestPlacingPercent(int percent, String description) {
        this.placingCase.resetTo(description);
        this.placingBonus = ( MAX_PLACING_BONUS * (float) percent ) / 100f;
    }
    
    private void singleClosierToStartIsBetter() {
        this.placingCase.resetTo("single cluster, no separators");
        this.meanPosition = this.findMeanPosition();
        this.placingPercent = 100f - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(90);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void singleMajorClosierToStartIsBetter() {
        this.placingCase.resetTo("single cluster, forming majority, no separators");
        this.meanPosition = this.firstCluster().firstPosition();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(93);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void singleClosierToEndIsBetter() {
        this.placingCase.resetTo("single cluster, with separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        
        this.clustersPlacingImportance = 100f - this.clustersPercentInVariant;
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(80);
        this.clustersPlacingImportancePercentNoHigherThan(75);
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(this.placingPercent, this.clustersPlacingImportance )
                / 100f;
    }
    
    private void singleMajorClosierToEndIsBetter() {
        this.placingCase.resetTo("single cluster, forming majority, with separators");
        this.meanPosition = this.lastCluster().lastPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);        
        this.applyKeyCharsBonusToPlacingPercent();
        
        this.clustersPlacingImportance = this.clustersPercentInVariant;
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(85);
        this.clustersPlacingImportancePercentNoHigherThan(80);
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(this.placingPercent, this.clustersPlacingImportance )
                / 100f;
    }
    
    private void singleClosierToEndIsBetterAfterLastSeparator() {
        this.placingCase.resetTo("single cluster, with separators, after last separator");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        this.placingPercentNoHigherThan(80);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void singleMajorClosierToEndIsBetterAfterLastSeparator() {
        this.placingCase.resetTo("single cluster, forming majority, with separators, after last separator");
        this.meanPosition = this.lastCluster().lastPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);        
        this.placingPercentNoHigherThan(85);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void manyClosierToStartAreBetterFirstAtStart() {
        this.placingCase.resetTo("many clusters, no separators, first at start");
        this.meanPosition = this.findMeanPosition() / 2;
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(87);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyMajorClosierToStartAreBetterFirstAtStart() {
        this.placingCase.resetTo("many clusters, forming majority, no separators");
        this.meanPosition = this.findMeanFirstPosition() / 2;
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(95);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyClosierToStartAreBetter() {
        this.placingCase.resetTo("many clusters, no separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        this.placingPercentNoHigherThan(75);
        
        this.clustersPlacingImportance = 100 - this.clustersPercentInVariant;
        this.clustersPlacingImportancePercentNoLowerThan(30);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent, 
                        this.clustersPlacingImportance, 
                        this.distanceBetweenClustersImportance )
                / 100;        
    }
    
    private void manyMajorClosierToStartAreBetter() {
        this.placingCase.resetTo("many clusters, forming majority, no separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        this.placingPercentNoHigherThan(80);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(this.placingPercent, this.distanceBetweenClustersImportance)
                / 100;
    }
    
    private void manyAsOneClosierToStartAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, no separators");
        this.meanPosition = this.findMeanPosition();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(85);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneMajorClosierToStartAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, forming majority, no separators");
        this.meanPosition = this.findMeanFirstPosition();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(93);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneClosierToEndAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, with separators");
        this.meanPosition = this.findMeanLastPosition();
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(85);
        this.applyPathComplexityPenalty();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneMajorClosierToEndAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, forming majority, with separators");
        this.meanPosition = this.lastCluster().lastPosition();
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(93);
        this.applyPathComplexityPenalty();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many-as-one clusters, with separators, after last separator");
        this.meanPosition = this.findMeanLastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        this.placingPercentNoHigherThan(87);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneMajorClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many-as-one clusters, with separators, after last separator");
        this.meanPosition = this.lastCluster().lastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        this.placingPercentNoHigherThan(93);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void applyAfterLastPathSeparatorPlacingPercentBonus() {
        int separatorsBonus = 10 + ( this.data.variantPathSeparators.size() * 10 );
        this.placingPercent = this.placingPercent + separatorsBonus;
    }
    
    private void applyPathComplexityPenalty() {
        int pathComplexity = this.data.variantPathSeparators.size() 
                             - this.data.positionsOf(direction).keyChars.size();
        
        if ( pathComplexity <= 0 ) {
            return;
        }
        
        logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] path complexity placing penalty : -%s%%", pathComplexity * 10);   
        
        this.placingPercent = this.placingPercent - (pathComplexity * 10);
        if ( this.placingPercent <= 0 ) {
            this.placingPercent = 0;
        }
    }
    
    private void applyCasePenalty(int penaltyPercent) {
        this.placingPercent = percentAsFloatOf(this.placingPercent, penaltyPercent);
        logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] case-dependent placing penalty : *%s%%", penaltyPercent); 
    }
    
    private void adjustMeanPositionAfterLastPathSeparator() {
        this.meanPosition = this.meanPosition - this.data.variantPathSeparators.last();
        this.adjustedVariantLength = this.data.variantText.length() - 1 - this.data.variantPathSeparators.last();
    }
    
    private void manyClosierToEndAreBetterLastAtEnd() {
        this.placingCase.resetTo("many clusters, no separators");
        this.meanPosition = this.findMeanPosition() / 2;
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(87);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyMajorClosierToEndAreBetterLastAtEnd() {
        this.placingCase.resetTo("many clusters, forming majority, with separators");
        this.meanPosition = this.findMeanLastPosition() / 2;
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        
        this.placingPercentNoHigherThan(95);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many clusters, with separators, after last separator");
        this.meanPosition = this.findMeanLastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        this.placingPercentNoHigherThan(70);
        
        this.clustersPlacingImportance = 100 - this.clustersPercentInVariant;
        this.clustersPlacingImportancePercentNoLowerThan(30);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.clustersPlacingImportance,
                        this.distanceBetweenClustersImportance)
                / 100;   
    }
    
    private void manyMajorClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many clusters, forming majority, with separators");
        this.meanPosition = this.findMeanLastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        this.placingPercentNoHigherThan(75);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.distanceBetweenClustersImportance)
                / 100;
    }
    
    private void manyClosierToEndAreBetter() {
        this.placingCase.resetTo("many clusters, with separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.placingPercentNoHigherThan(70);
        this.applyPathComplexityPenalty();
        
        this.clustersPlacingImportance = 100 - this.clustersPercentInVariant;
        this.applyKeyCharsBonusToPlacingImportance();
        this.clustersPlacingImportancePercentNoLowerThan(30);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.clustersPlacingImportance)
                / 100;   
        
        this.placingBonus = percentAsFloatOf(this.placingBonus, this.distanceBetweenClustersImportance);
    }

    private void calculateDistanceBetweenClustersImportance() {
        float distanceToVariantLengthPercent = 100 - percentAsFloat(this.distanceBetweenClusters, this.data.variantText.length() - this.totalLength());
        int distanceToTotalLengthPercent = percentAsInt(this.distanceBetweenClusters, this.totalLength());
        this.distanceBetweenClustersImportance = distanceToVariantLengthPercent;
    }
    
    private void manyMajorClosierToEndAreBetter() {
        this.placingCase.resetTo("many clusters, forming majority, with separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variantText.length() - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.placingPercentNoHigherThan(75);
        
        this.calculateDistanceBetweenClustersImportance();
        this.applyPathComplexityPenalty();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.distanceBetweenClustersImportance)
                / 100;
    }
    
    private void applyKeyCharsBonusToPlacingPercent() {
        if ( this.data.positionsOf(direction).keyChars.size() > 0 ) {
            logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] key chars bonus to placing : +20% ");
            this.placingPercent = this.placingPercent + 20;
            
            if ( this.placingPercent > 100 ) {
                this.placingPercent = 100;
            }
        }
    }
    
    private void applyKeyCharsBonusToPlacingImportance() {
        if ( this.clustersPlacingImportance == UNKNOWN_VALUE ) {
            return;
        }
        
        if ( this.data.positionsOf(direction).keyChars.size() > 0 ) {
            logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] key chars bonus to placing importance : +10% ");
            this.clustersPlacingImportance = this.clustersPlacingImportance + 10;
            
            if ( this.clustersPlacingImportance > 100 ) {
                this.clustersPlacingImportance = 100;
            }
        }
    }
    
    private void manyAsOneMajorClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.singleMajorClosierToStartIsBetterBeforeFirstPathSeparator();
        this.placingCase.resetTo("many-as-one majority clusters, with separators, before first separator");
    }
    
    private void manyAsOneClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.manyMajorClosierToStartAreBetterBeforeFirstPathSeparator();
        this.placingCase.resetTo("many-as-one clusters, with separators, before first separator");
    }
    
    private void manyMajorClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("many clusters, with separators, before first separator");
        this.meanPosition = this.findMeanFirstPosition();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, firstPathSeparator - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenalty();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variantText.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsIntOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(65);
        this.clustersPlacingImportancePercentNoHigherThan(65);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }
    
    private void manyClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("many clusters, with separators, before first separator");
        this.meanPosition = this.findMeanPosition();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, firstPathSeparator - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenalty();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variantText.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsIntOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(65);
        this.clustersPlacingImportancePercentNoHigherThan(65);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }    
    
    private void singleMajorClosierToStartIsBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("single cluster, with separators, before first separator");
        this.meanPosition = this.firstCluster().firstPosition();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, firstPathSeparator - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenalty();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variantText.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsFloatOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(65);
        this.clustersPlacingImportancePercentNoHigherThan(65);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }
    
    private void singleClosierToStartIsBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("single cluster, with separators, before first separator");
        this.meanPosition = this.firstCluster().firstPosition();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, firstPathSeparator - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenalty();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variantText.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsFloatOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(50);
        this.clustersPlacingImportancePercentNoHigherThan(50);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }

    private boolean isLastClusterAtVariantEnd() {
        return this.lastCluster().lastPosition() == this.data.variantText.length() - 1;
    }

    void logState() {
        logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] %s", this.toString());
        if ( placingBonusNotApplicableReason.isNotPresent() ) {
            logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] placing case       : %s ", placingCase.orThrow());
            if ( meanPosition != UNKNOWN_VALUE ) {
                if ( this.adjustedVariantLength != UNKNOWN_VALUE ) {
                    logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] mean position      : %s/%s (%s) ", meanPosition, this.adjustedVariantLength, this.data.variantText.length() - 1);
                } else {
                    logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] mean position      : %s/%s ", meanPosition, this.data.variantText.length() - 1);
                } 
            }
            if ( placingPercent != UNKNOWN_VALUE ) {
                logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] placing percent    : %s%% ", placingPercent);
            }
            if ( clustersPlacingImportance != UNKNOWN_VALUE ) {
                logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] placing importance : %s%% ", clustersPlacingImportance);
            }
            if ( distanceBetweenClustersImportance != UNKNOWN_VALUE ) {
                logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] cluster distance importance : %s%% ", distanceBetweenClustersImportance);
            }
        } else {
            logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] placing not applicable : %s ", placingBonusNotApplicableReason.orThrow());
        }

        logAnalyze(POSITIONS_CLUSTERS, "    [C-placing] placing bonus      : %s ", placingBonus);
    }
    
    @Override
    public void clear() {   
        this.lastAdded.nullify();
        giveBackAllToPoolAndClear(this.clusters);
        this.arranged = false;
        this.clustersTotalLength = 0;
        this.distanceBetweenClusters = 0;
        this.allClustersSeparatedByOneChar = false;
        
        this.clustersPercentInVariant = UNKNOWN_VALUE;        
        this.meanPosition = UNKNOWN_VALUE;
        this.adjustedVariantLength = UNKNOWN_VALUE;
        this.placingPercent = UNKNOWN_VALUE;
        this.clustersPlacingImportance = UNKNOWN_VALUE;
        this.distanceBetweenClustersImportance = UNKNOWN_VALUE;
        this.placingBonus = UNKNOWN_VALUE;
        this.placingBonusLimit = UNKNOWN_VALUE;
        this.placingCase.nullify();
        this.placingBonusNotApplicableReason.nullify();
    }
    
    @Override
    public String toString() {
        if ( this.isEmpty() ) {
            return "Clusters[ empty ]";
        }
        
        return this.clusters
                .stream()
                .map(cluster -> cluster.toString())
                .collect(joining(" ", "Clusters[ ", " ]"));
    }
    
}
