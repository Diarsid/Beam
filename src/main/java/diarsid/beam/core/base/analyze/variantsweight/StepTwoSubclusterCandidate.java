/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_SEARCH;

/**
 *
 * @author Diarsid
 */
class StepTwoSubclusterCandidate {
    
    private static final int UNINITIALIZED = -9;
    
    static class PositionView {

        public PositionView(StepTwoSubclusterCandidate subcluster) {
            this.subcluster = subcluster;
        }
        
        private final StepTwoSubclusterCandidate subcluster;
        private int i;
        private char character;
        private int patternPosition;
        private int variantPosition;
        private boolean included;
        private MatchType matchType;
        private boolean filledFromSubcluster;
        
        private PositionView fill(
                char c, 
                int patternPosition, 
                int variantPosition, 
                boolean included, 
                MatchType matchType) {
            this.character = c;
            this.included = included;
            this.patternPosition = patternPosition;
            this.variantPosition = variantPosition;
            this.matchType = matchType;
            
            this.i = UNINITIALIZED;
            this.filledFromSubcluster = false;
            
            return this;
        }
        
        private boolean isBetterThan(PositionView other) {
            if ( this.included && (! other.included) ) {
                return true;
            } else if ( (! this.included) && other.included ) {
                return false;
            } else {
                if ( this.matchType.strength() > other.matchType.strength() ) {
                    return true;
                } else if ( this.matchType.strength() < other.matchType.strength() ) {
                    return false;
                } else {
                    throw new IllegalArgumentException("Unexpected matching!");
                }
            }
        }
        
        private void mergeInSubclusterInsteadOf(PositionView other) {
            this.i = other.i;
            
            this.subcluster
                    .chars
                    .set(this.i, this.character);
            this.subcluster
                    .inclusions
                    .set(this.i, this.included);
            this.subcluster
                    .patternPositions
                    .set(this.i, this.patternPosition);
            this.subcluster
                    .variantPositions
                    .set(this.i, this.variantPosition);
            this.subcluster
                    .matches
                    .set(this.i, this.matchType);
            
            this.subcluster.matchStrength = this.subcluster.matchStrength + this.matchType.strength();            
        }

        private PositionView fillFromSubcluster(int i) {
            this.character = this.subcluster.chars.get(i);
            this.included = this.subcluster.inclusions.get(i);
            this.patternPosition = this.subcluster.patternPositions.get(i);
            this.variantPosition = this.subcluster.variantPositions.get(i);
            this.matchType = this.subcluster.matches.get(i);
            
            this.i = i;
            this.filledFromSubcluster = true;
            
            return this;
        }
        
        boolean goToNext() {
            boolean hasNext = this.i < this.subcluster.variantPositions.size() - 1;
            
            if ( hasNext ) {
                this.i++;
                this.fillFromSubcluster(this.i);
            }        
            
            return hasNext;
        }
        
        char character() {
            return this.character;
        }
        
        int patternPosition() {
            return this.patternPosition;
        }
        
        int variantPosition() {
            return this.variantPosition;
        }
        
        boolean included() {
            return this.included;
        }
        
        boolean notIncluded() {
            return ! this.included;
        }
        
        MatchType matchType() {
            return this.matchType;
        }
    }
    
    private final List<Character> chars;
    private final List<Integer> patternPositions;
    private final List<Integer> variantPositions;
    private final List<MatchType> matches;
    private final List<Boolean> inclusions;
    
    private final PositionView existingPositionView;
    private final PositionView possiblePositionView;
    
    private char assessedChar;
    private int assessedCharPatternPosition;
    private int assessedCharVariantPosition;    
    private int includedQty;
    private int matchStrength;
    private int mergedDuplicates;

    public StepTwoSubclusterCandidate() {
        this.chars = new ArrayList<>();
        this.patternPositions = new ArrayList<>();
        this.variantPositions = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.inclusions = new ArrayList<>();
        this.includedQty = 0;
        this.matchStrength = 0;
        this.mergedDuplicates = 0;
        this.assessedChar = ' ';
        this.assessedCharPatternPosition = UNINITIALIZED;
        this.assessedCharVariantPosition = UNINITIALIZED;
        
        this.existingPositionView = new PositionView(this);
        this.possiblePositionView = new PositionView(this);
    }
    
    void setAssessed(char c, int patternPosition, int variantPosition) {
        this.assessedChar = c;
        this.assessedCharPatternPosition = patternPosition;
        this.assessedCharVariantPosition = variantPosition;
    }
    
    int charPatternPosition() {
        return this.assessedCharPatternPosition;
    }
    
    int charVariantPosition() {
        return this.assessedCharVariantPosition;
    }
    
    PositionView positionView() {
        this.existingPositionView.fillFromSubcluster(0);
        return this.existingPositionView;
    }
    
    private PositionView positionViewAt(int i) {
        this.existingPositionView.fillFromSubcluster(i);
        return this.existingPositionView;
    }
    
    boolean isSet() {
        return this.chars.size() > 0;
    }
    
    void add(
            char c, 
            int patternPosition, 
            int variantPosition, 
            boolean included, 
            MatchType matchType) {
        int alreadyExisted = this.patternPositions.indexOf(patternPosition);
        if ( alreadyExisted > -1 ) {
            PositionView existingPosition = this.positionViewAt(alreadyExisted);
            PositionView possiblePosition = this.possiblePositionView.fill(c, patternPosition, variantPosition, included, matchType);
            logAnalyze(
                    POSITIONS_SEARCH, 
                    "          [info] positions-in-cluster duplicate: new '%s' pattern:%s, variant:%s -vs- existed '%s' pattern:%s, variant:%s",
                    possiblePosition.character, possiblePosition.patternPosition, possiblePosition.variantPosition,
                    existingPosition.character, existingPosition.patternPosition, existingPosition.variantPosition);
            
            if ( possiblePosition.isBetterThan(existingPosition) ) {
                possiblePosition.mergeInSubclusterInsteadOf(existingPosition);
                logAnalyze(
                        POSITIONS_SEARCH, 
                        "          [info] positions-in-cluster duplicate: new position accepted");
            } else {
                logAnalyze(
                        POSITIONS_SEARCH, 
                        "          [info] positions-in-cluster duplicate: new position rejected");
            }
            
            this.mergedDuplicates++;
        } else {
            this.chars.add(c);
            this.patternPositions.add(patternPosition);
            this.variantPositions.add(variantPosition);
            this.matches.add(matchType);
            this.inclusions.add(included);
            if ( included ) {
                this.includedQty++;
            } 
            this.matchStrength = this.matchStrength + matchType.strength();
            logAnalyze(
                    POSITIONS_SEARCH, 
                    "          [info] positions-in-cluster '%s' pattern:%s, variant:%s, included: %s, %s", 
                    c, patternPosition, variantPosition, included, matchType.name());
        }        
    }
    
    boolean isBetterThan(StepTwoSubclusterCandidate other) {
        if ( this.includedQty == 0 && other.includedQty == 0 ) {
            /* comparison of found subclusters, both are new */
            /* prefer subcluster that have more matches to fill more chars */
            if ( this.matched() > other.matched() ) {
                return true;
            } else if ( this.matched() < other.matched() ) {
                return false;
            } else {
                return this.matchStrength >= other.matchStrength;
            }
        } else {
            /* comparison of found subclusters, some subclusters have ties with already found chars */
            /* prefer subcluster that have more ties with found chars to increase consistency */
            if ( this.includedQty > other.includedQty ) {
                return true;
            } else if ( this.includedQty < other.includedQty ) {
                return false;
            } else {
                if ( this.matched() > other.matched() ) {
                    return true;
                } else if ( this.matched() < other.matched() ) {
                    return false;
                } else {
                    return this.matchStrength >= other.matchStrength;
                }
            }
        }        
    }
    
    int matched() {
        return this.chars.size() + this.mergedDuplicates;
    }
    
    void clear() {
        this.chars.clear();
        this.patternPositions.clear();
        this.variantPositions.clear();
        this.matches.clear();
        this.inclusions.clear();
        this.includedQty = 0;
        this.matchStrength = 0;
        this.mergedDuplicates = 0;
        this.assessedChar = ' ';
        this.assessedCharPatternPosition = UNINITIALIZED;
        this.assessedCharVariantPosition = UNINITIALIZED;
    }
    
    @Override
    public String toString() {
        return format(
                "PositionCandidate['%s' variant:%s, clusters - %s, pattern:%s, variant:%s, included:%s, matches:%s]", 
                this.assessedChar, this.assessedCharVariantPosition, this.chars, this.patternPositions, this.variantPositions, this.inclusions, this.matches);
    }
}
