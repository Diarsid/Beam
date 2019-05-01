/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import diarsid.support.objects.Possible;

import static java.lang.Integer.min;

import static diarsid.beam.core.base.analyze.variantsweight.ClusterComparison.LEFT_IS_BETTER;
import static diarsid.beam.core.base.analyze.variantsweight.ClusterComparison.LEFT_IS_WORSE;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.MathUtil.zeroIfNegative;
import static diarsid.support.objects.Possibles.possibleButEmpty;
import static diarsid.support.strings.StringUtils.countCharMatchesIn;
import static diarsid.support.strings.StringUtils.isWordsSeparator;
import static diarsid.support.strings.StringUtils.joinAll;

/**
 *
 * @author Diarsid
 */
class PositionsSearchStepOneCluster {
    
    private static final int UNINITIALIZED = -5;
    private static final int BEFORE_START = -1;
    private static final int TYPO_RANGE = 3;
    
    private static class StepOneClusterPositionView implements PositionView {
        
        private final PositionsSearchStepOneCluster cluster;

        public StepOneClusterPositionView(PositionsSearchStepOneCluster cluster) {
            this.cluster = cluster;
        }

        @Override
        public int patternPosition() {
            return this.cluster.lastAddedPatternPosition;
        }

        @Override
        public int variantPosition() {
            return this.cluster.lastAddedVariantPosition;
        }
        
    }
    
    private static class StepOneClusterPositionIterableView implements PositionIterableView {
        
        private final PositionsSearchStepOneCluster cluster;
        private int i;

        private StepOneClusterPositionIterableView(PositionsSearchStepOneCluster cluster) {
            this.cluster = cluster;
            this.i = BEFORE_START;
        }
        
        @Override
        public boolean hasNext() {
            return this.i < this.cluster.allVariantPositions.size() - 1;
        }
        
        
        @Override
        public void goToNext() {
            this.i++;
        }        
        
        @Override
        public int patternPosition() {
            return this.cluster.allPatternPositions.get(this.i);
        }
        
        
        @Override
        public int variantPosition() {
            return this.cluster.allVariantPositions.get(this.i);
        }
        
        @Override
        public boolean isFilled() {
            return false;
        }
        
        @Override
        public boolean isNotFilled() {
            return true;
        }
        
    }
    
    static class PatternCluster {
        
        private final PositionsSearchStepOneCluster cluster;

        PatternCluster(PositionsSearchStepOneCluster cluster) {
            this.cluster = cluster;
        }
        
        boolean isAtPatternStart() {
            return this.firstPosition() == 0;
        }
        
        boolean isAtPatternEnd() {
            return this.cluster.pattern.orThrow().length() - 1 == this.lastPosition();
        }
        
        int firstPosition() {
            return this.cluster.allPatternPositions.get(0);
        }
        
        int lastPosition() {
            return last(this.cluster.allPatternPositions);
        }
        
        int length() {
            return this.cluster.allPatternPositions.size();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.firstPosition();
            hash = 97 * hash + this.lastPosition();
            hash = 97 * hash + this.length();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null ) {
                return false;
            }
            if ( getClass() != obj.getClass() ) {
                return false;
            }
            final PatternCluster other = ( PatternCluster ) obj;
            
            return 
                    this.firstPosition() == other.firstPosition() && 
                    this.lastPosition() == other.lastPosition();
        }
        
    }
    
    private final Possible<String> variant;
    private final Possible<String> pattern;
    
    private final StepOneClusterPositionIterableView positionIterableView;
    private final StepOneClusterPositionView positionView;
    private final PatternCluster patternCluster;
    
    private final List<Integer> allVariantPositions;
    private final List<Integer> allPatternPositions;
    private final List<Integer> prevsVariantPositions;
    private final List<Integer> prevsPatternPositions;
    private final List<Integer> nextsVariantPositions;
    private final List<Integer> nextsPatternPositions;
    
    private Boolean startsAfterSeparator;
    private Boolean endsBeforeSeparator;
    private Boolean variantPositionsAtStart;
    private Boolean variantPositionsAtEnd;
    
    private int possibleTypoMatchesBefore;
    private int possibleTypoMatchesAfter;
    private int possibleTypoMatches;
    
    private int prevVariantPosition;
    private int mainVariantPosition;
    private int nextVariantPosition;
    private int prevPatternPosition;
    private int mainPatternPosition;
    private int nextPatternPosition;
    private boolean hasPrevs;
    private boolean hasNexts;
    
    private int lastAddedVariantPosition;
    private int lastAddedPatternPosition;
    
    private boolean finished;
    
    private int skip;

    public PositionsSearchStepOneCluster() {
        this.variant = possibleButEmpty();
        this.pattern = possibleButEmpty();
        this.positionIterableView = new StepOneClusterPositionIterableView(this);
        this.positionView = new StepOneClusterPositionView(this);
        this.patternCluster = new PatternCluster(this);
        this.allVariantPositions = new ArrayList<>();
        this.allPatternPositions = new ArrayList<>();
        this.prevsVariantPositions = new ArrayList<>();
        this.prevsPatternPositions = new ArrayList<>();
        this.nextsVariantPositions = new ArrayList<>();
        this.nextsPatternPositions = new ArrayList<>();
        this.prevVariantPosition = UNINITIALIZED;
        this.mainVariantPosition = UNINITIALIZED;
        this.nextVariantPosition = UNINITIALIZED;
        this.prevPatternPosition = UNINITIALIZED;
        this.mainPatternPosition = UNINITIALIZED;
        this.nextPatternPosition = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
        this.lastAddedVariantPosition = UNINITIALIZED;
        this.lastAddedPatternPosition = UNINITIALIZED;
        this.finished = true;
        this.skip = 0;
        this.possibleTypoMatchesBefore = 0;
        this.possibleTypoMatchesAfter = 0;
        this.variantPositionsAtStart = null;
        this.startsAfterSeparator = null;
        this.variantPositionsAtEnd = null;
        this.endsBeforeSeparator = null;
    }
    
    void incrementSkip() {
        this.skip++;
    }
    
    int skip() {
        return this.skip;
    }
    
    PositionIterableView positionIterableView() {
        this.finishIfNot();
        this.positionIterableView.i = BEFORE_START;
        return this.positionIterableView;
    }
    
    PositionView lastAdded() {
        return this.positionView;
    }
    
    PatternCluster patternCluster() {
        return this.patternCluster;
    }
    
    int possibleTypoMatches() {
        return this.possibleTypoMatches;
    }
    
    boolean doesHaveMorePossibleTypoMatchesThan(PositionsSearchStepOneCluster other) {
        return this.possibleTypoMatches > other.possibleTypoMatches;
    }
    
    boolean isAtStart() {
        return this.variantPositionsAtStart;
    }
    
    boolean isAtEnd() {
        return this.variantPositionsAtEnd;
    }
    
    boolean doesStartAfterSeparator() {
        return this.startsAfterSeparator;
    }
    
    boolean doesEndBeforeSeparator() {
        return this.endsBeforeSeparator;
    }
    
    private void finishIfNot() {
        if ( this.finished ) {
            this.composeVariantPositions();
            this.composePatternIndexes();
            this.finished = false;
        }        
    }
    
    private void composeVariantPositions() {
        for (int i = this.prevsVariantPositions.size() - 1; i > -1; i--) {
            this.allVariantPositions.add(this.prevsVariantPositions.get(i));
        }
        if ( this.prevVariantPosition > -1 ) {
            this.allVariantPositions.add(this.prevVariantPosition);
        }
        if ( this.mainVariantPosition > -1 ) {
            this.allVariantPositions.add(this.mainVariantPosition);
        }
        if ( this.nextVariantPosition > -1 ) {
            this.allVariantPositions.add(this.nextVariantPosition);
        }
        this.allVariantPositions.addAll(this.nextsVariantPositions);
    }
    
    private void composePatternIndexes() {
        for (int i = this.prevsPatternPositions.size() - 1; i > -1; i--) {
            this.allPatternPositions.add(this.prevsPatternPositions.get(i));
        }
        if ( this.prevPatternPosition > -1 ) {
            this.allPatternPositions.add(this.prevPatternPosition);
        }
        if ( this.mainPatternPosition > -1 ) {
            this.allPatternPositions.add(this.mainPatternPosition);
        }
        if ( this.nextPatternPosition > -1 ) {
            this.allPatternPositions.add(this.nextPatternPosition);
        }
        this.allPatternPositions.addAll(this.nextsPatternPositions);
    }
    
    void finish(String variant, String pattern) {   
        this.variant.resetTo(variant);
        this.pattern.resetTo(pattern);
        this.finishIfNot();
        
        if ( this.patternCluster.isAtPatternStart() ) {
            
        } else {
            int variantClusterFirstPosition = this.firstVariantPosition();
            if ( variantClusterFirstPosition > 0 ) {
                int patternClusterFirstPosition = this.patternCluster.firstPosition();
                int patternToExcl = patternClusterFirstPosition;
                int patternFromIncl = zeroIfNegative(patternClusterFirstPosition - TYPO_RANGE);
                
                int variantToExcl = variantClusterFirstPosition;
                int variantFromIncl = zeroIfNegative(variantToExcl - TYPO_RANGE);
                
                this.possibleTypoMatchesBefore = countCharMatchesIn(
                        variant, variantFromIncl, variantToExcl, 
                        pattern, patternFromIncl, patternToExcl);
                this.possibleTypoMatches = this.possibleTypoMatches + this.possibleTypoMatchesBefore;
            }
        }
        
        if ( this.patternCluster.isAtPatternEnd() ) {
            
        } else {
            int variantClusterLastPosition = this.lastVariantPosition();
            if ( variantClusterLastPosition < variant.length() - 1 ) {
                int patternClusterLastPosition = this.patternCluster.lastPosition();
                int patternFromIncl = patternClusterLastPosition + 1;
                int patternToExcl = patternFromIncl + TYPO_RANGE;
                patternToExcl = min(patternToExcl, pattern.length());
                
                int variantFromIncl = variantClusterLastPosition + 1;
                int variantToExcl = variantFromIncl + TYPO_RANGE;
                variantToExcl = min(variantToExcl, variant.length());
                
                this.possibleTypoMatchesAfter = countCharMatchesIn(
                        variant, variantFromIncl, variantToExcl, 
                        pattern, patternFromIncl, patternToExcl);
                this.possibleTypoMatches = this.possibleTypoMatches + this.possibleTypoMatchesAfter;
            }
        }
        
        this.variantPositionsAtStart = this.firstVariantPosition() == 0;
        this.variantPositionsAtEnd = this.lastVariantPosition() == variant.length() - 1;
        
        if ( this.variantPositionsAtStart ) {
            this.startsAfterSeparator = true;
        } else {
            this.startsAfterSeparator = isWordsSeparator(variant.charAt(this.firstVariantPosition() - 1));
        }
        
        if ( this.variantPositionsAtEnd ) {
            this.endsBeforeSeparator = true;
        } else {
            this.endsBeforeSeparator = isWordsSeparator(variant.charAt(this.lastVariantPosition() + 1));
        }
    }
    
    static ClusterComparison calculateAdditionalPossibleTypoMatches(
            PositionsSearchStepOneCluster one, PositionsSearchStepOneCluster two) {
        String pattern = one.pattern.orThrow();
        String variant = one.variant.orThrow();
        PatternCluster patternCluster = one.patternCluster;
        
        int oneAdditionalMatches = 0;
        int twoAdditionalMatches = 0;
        
        boolean additionalMatchesEqual = true;
        
        int patternBackPointerFromIncl = patternCluster.firstPosition() - TYPO_RANGE - 1;
        if ( patternBackPointerFromIncl >= 0 ) { 
        
            int clusterOnePointerFromIncl = one.firstVariantPosition() - TYPO_RANGE - 1;
            int clusterTwoPointerFromIncl = two.firstVariantPosition() - TYPO_RANGE - 1;
            
            if ( clusterOnePointerFromIncl >= 0 || clusterTwoPointerFromIncl >= 0 ) {
                int patternOnePointer = patternBackPointerFromIncl;
                int patternTwoPointer = patternBackPointerFromIncl;
                int clusterOnePointer = clusterOnePointerFromIncl;
                int clusterTwoPointer = clusterTwoPointerFromIncl;
                char patternOneChar;
                char patternTwoChar;
                char variantOneChar;
                char variantTwoChar;
                boolean oneCanMove = clusterOnePointerFromIncl >= 0;
                boolean twoCanMove = clusterTwoPointerFromIncl >= 0;
                int variantOneLimit;
                int variantTwoLimit;
                if ( one.isBefore(two) ) {
                    variantOneLimit = 0;
                    variantTwoLimit = one.lastVariantPosition() + 1;
                } else {
                    variantOneLimit = two.lastVariantPosition() + 1;
                    variantTwoLimit = 0;
                }

                while ( additionalMatchesEqual && ( oneCanMove || twoCanMove ) ) {
                    
                    if ( oneCanMove ) {
                        patternOneChar = pattern.charAt(patternOnePointer);
                        variantOneChar = variant.charAt(clusterOnePointer);
                        if ( patternOneChar == variantOneChar ) {
                            oneAdditionalMatches++;
                            patternOnePointer--;
                        }
                        clusterOnePointer--;
                    }
                    
                    if ( twoCanMove ) {
                        patternTwoChar = pattern.charAt(patternTwoPointer);
                        variantTwoChar = variant.charAt(clusterTwoPointer);
                        if ( patternTwoChar == variantTwoChar ) {
                            twoAdditionalMatches++;
                            patternTwoPointer--;
                        }
                        clusterTwoPointer--;                        
                    }

                    additionalMatchesEqual = oneAdditionalMatches == twoAdditionalMatches;

                    oneCanMove = clusterOnePointer >= variantOneLimit && patternOnePointer >= 0;
                    twoCanMove = clusterTwoPointer >= variantTwoLimit && patternTwoPointer >= 0;
                }

                if ( ! additionalMatchesEqual ) {
                    if ( oneAdditionalMatches > twoAdditionalMatches ) {
                        return LEFT_IS_BETTER;
                    } else if ( oneAdditionalMatches < twoAdditionalMatches ) {
                        return LEFT_IS_WORSE;
                    }
                }
            }            
        }
                
        int patternForwPointerFromIncl = patternCluster.lastPosition() + 1 + TYPO_RANGE;
        int patternLength = pattern.length();
        if ( patternForwPointerFromIncl < patternLength ) {
            
            int variantLength = variant.length();
            int clusterOnePointerFromIncl = one.lastVariantPosition() + 1 + TYPO_RANGE;
            int clusterTwoPointerFromIncl = two.lastVariantPosition() + 1 + TYPO_RANGE;
            boolean clusterOneHasSpaceAfter = clusterOnePointerFromIncl < variantLength;
            boolean clusterTwoHasSpaceAfter = clusterTwoPointerFromIncl < variantLength;
            
            if ( clusterOneHasSpaceAfter || clusterTwoHasSpaceAfter ) {
                int patternOnePointer = patternForwPointerFromIncl;
                int patternTwoPointer = patternForwPointerFromIncl;
                int clusterOnePointer = clusterOnePointerFromIncl;
                int clusterTwoPointer = clusterTwoPointerFromIncl;
                char patternOneChar;
                char patternTwoChar;
                char variantOneChar;
                char variantTwoChar;
                boolean oneCanMove = clusterOneHasSpaceAfter;
                boolean twoCanMove = clusterTwoHasSpaceAfter;
                int variantOneLimit;
                int variantTwoLimit;
                if ( one.isBefore(two) ) {
                    variantOneLimit = two.firstVariantPosition();
                    variantTwoLimit = variantLength;
                } else {
                    variantOneLimit = variantLength;
                    variantTwoLimit = one.firstVariantPosition();                    
                }
                
                while ( additionalMatchesEqual && ( oneCanMove || twoCanMove ) ) {
                    
                    if ( oneCanMove ) {
                        patternOneChar = pattern.charAt(patternOnePointer);
                        variantOneChar = variant.charAt(clusterOnePointer);
                        if ( patternOneChar == variantOneChar ) {
                            oneAdditionalMatches++;
                            patternOnePointer++;
                        }
                        clusterOnePointer++;
                    }
                    
                    if ( twoCanMove ) {
                        patternTwoChar = pattern.charAt(patternTwoPointer);
                        variantTwoChar = variant.charAt(clusterTwoPointer);
                        if ( patternTwoChar == variantTwoChar ) {
                            twoAdditionalMatches++;
                            patternTwoPointer++;
                        }
                        clusterTwoPointer++;
                    }
                    
                    additionalMatchesEqual = oneAdditionalMatches == twoAdditionalMatches;

                    oneCanMove = clusterOnePointer < variantOneLimit && patternOnePointer < patternLength;
                    twoCanMove = clusterTwoPointer < variantTwoLimit && patternTwoPointer < patternLength;
                }
            }
        }
        
        if ( oneAdditionalMatches > twoAdditionalMatches ) {
            return LEFT_IS_BETTER;
        } else if ( oneAdditionalMatches < twoAdditionalMatches ) {
            return LEFT_IS_WORSE;
        } else {
            return null;
        }
    }
    
    boolean isSet() {
        return this.mainVariantPosition > -1;
    }
    
    boolean isNotSet() {
        return this.mainVariantPosition < 0;
    }
    
    void setMain(int patternP, int variantP) {
        this.lastAddedVariantPosition = variantP;
        this.lastAddedPatternPosition = patternP;
        
        this.mainVariantPosition = variantP;
        this.mainPatternPosition = patternP;
    }

    void setPrev(int prev) {
        this.lastAddedVariantPosition = prev;
        this.lastAddedPatternPosition = this.mainPatternPosition - 1;
        
        this.prevVariantPosition = this.lastAddedVariantPosition;
        this.prevPatternPosition = this.lastAddedPatternPosition;
    }

    void setNext(int next) {
        this.lastAddedVariantPosition = next;
        this.lastAddedPatternPosition = this.mainPatternPosition + 1;
        
        this.nextVariantPosition = this.lastAddedVariantPosition;
        this.nextPatternPosition = this.lastAddedPatternPosition;
    }
    
    void addNext(int nextOne) {
        if ( this.nextVariantPosition < 0 ) {
            throw new IllegalStateException();
        }
        this.hasNexts = true;
        
        this.lastAddedVariantPosition = nextOne;
        this.lastAddedPatternPosition = this.nextPatternPosition + this.nextsVariantPositions.size() + 1;
        
        this.nextsVariantPositions.add(this.lastAddedVariantPosition);
        this.nextsPatternPositions.add(this.lastAddedPatternPosition);
    }
    
    void addPrev(int prevOne) {
        if ( this.prevVariantPosition < 0 ) {
            throw new IllegalStateException();
        }
        this.hasPrevs = true;
        
        this.lastAddedVariantPosition = prevOne;
        this.lastAddedPatternPosition = this.prevPatternPosition - this.prevsVariantPositions.size() + 1;
        
        this.prevsVariantPositions.add(this.lastAddedVariantPosition);
        this.prevsPatternPositions.add(this.lastAddedPatternPosition);
    }
    
    int length() {
        return this.prevsVariantPositions.size() + 3 + this.nextsVariantPositions.size();
    }
    
    boolean isLongerThan(PositionsSearchStepOneCluster other) {
        return this.length() + this.possibleTypoMatches() > 
               other.length() + other.possibleTypoMatches();
    }
    
    private int firstVariantPosition() {
        return this.allVariantPositions.get(0);        
    }
    
    private int lastVariantPosition() {
        return last(this.allVariantPositions);
    }
    
    boolean isBefore(PositionsSearchStepOneCluster other) {
        return this.firstVariantPosition() < other.firstVariantPosition();
    }
    
    void clear() {
        this.variant.nullify();
        this.pattern.nullify();
        this.positionIterableView.i = BEFORE_START;
        this.allVariantPositions.clear();
        this.prevsVariantPositions.clear();
        this.nextsVariantPositions.clear();
        this.allPatternPositions.clear();
        this.prevsPatternPositions.clear();
        this.nextsPatternPositions.clear();
        this.prevVariantPosition = UNINITIALIZED;
        this.mainVariantPosition = UNINITIALIZED;
        this.nextVariantPosition = UNINITIALIZED;
        this.prevPatternPosition = UNINITIALIZED;
        this.mainPatternPosition = UNINITIALIZED;
        this.nextPatternPosition = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
        this.lastAddedVariantPosition = UNINITIALIZED;
        this.lastAddedPatternPosition = UNINITIALIZED;
        this.finished = true;
        this.skip = 0;
        this.possibleTypoMatchesBefore = 0;
        this.possibleTypoMatchesAfter = 0;
        this.possibleTypoMatches = 0;
        this.variantPositionsAtStart = null;
        this.startsAfterSeparator = null;
        this.variantPositionsAtEnd = null;
        this.endsBeforeSeparator = null;
    }
    
    @Override
    public String toString() {
        return 
                "[positions: " +
                joinAll(",", this.prevsVariantPositions, this.prevVariantPosition, this.mainVariantPosition, this.nextVariantPosition, this.nextsVariantPositions) + ", indexes: " + 
                joinAll(",", this.prevsPatternPositions, this.prevPatternPosition, this.mainPatternPosition, this.nextPatternPosition, this.nextsPatternPositions) +
                this.plusTyposString() + "]";
    }
    
    private String plusTyposString() {
        if ( this.possibleTypoMatches > 0 ) {
            return ", poss.typos: " + this.possibleTypoMatches;
        } else {
            return "";
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.positionIterableView);
        hash = 89 * hash + Objects.hashCode(this.allVariantPositions);
        hash = 89 * hash + Objects.hashCode(this.allPatternPositions);
        hash = 89 * hash + Objects.hashCode(this.prevsVariantPositions);
        hash = 89 * hash + Objects.hashCode(this.prevsPatternPositions);
        hash = 89 * hash + Objects.hashCode(this.nextsVariantPositions);
        hash = 89 * hash + Objects.hashCode(this.nextsPatternPositions);
        hash = 89 * hash + Objects.hashCode(this.startsAfterSeparator);
        hash = 89 * hash + Objects.hashCode(this.endsBeforeSeparator);
        hash = 89 * hash + Objects.hashCode(this.variantPositionsAtStart);
        hash = 89 * hash + Objects.hashCode(this.variantPositionsAtEnd);
        hash = 89 * hash + this.possibleTypoMatchesBefore;
        hash = 89 * hash + this.possibleTypoMatchesAfter;
        hash = 89 * hash + this.prevVariantPosition;
        hash = 89 * hash + this.mainVariantPosition;
        hash = 89 * hash + this.nextVariantPosition;
        hash = 89 * hash + this.prevPatternPosition;
        hash = 89 * hash + this.mainPatternPosition;
        hash = 89 * hash + this.nextPatternPosition;
        hash = 89 * hash + (this.hasPrevs ? 1 : 0);
        hash = 89 * hash + (this.hasNexts ? 1 : 0);
        hash = 89 * hash + (this.finished ? 1 : 0);
        hash = 89 * hash + this.skip;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final PositionsSearchStepOneCluster other = ( PositionsSearchStepOneCluster ) obj;
        if ( this.possibleTypoMatchesBefore != other.possibleTypoMatchesBefore ) {
            return false;
        }
        if ( this.possibleTypoMatchesAfter != other.possibleTypoMatchesAfter ) {
            return false;
        }
        if ( this.prevVariantPosition != other.prevVariantPosition ) {
            return false;
        }
        if ( this.mainVariantPosition != other.mainVariantPosition ) {
            return false;
        }
        if ( this.nextVariantPosition != other.nextVariantPosition ) {
            return false;
        }
        if ( this.prevPatternPosition != other.prevPatternPosition ) {
            return false;
        }
        if ( this.mainPatternPosition != other.mainPatternPosition ) {
            return false;
        }
        if ( this.nextPatternPosition != other.nextPatternPosition ) {
            return false;
        }
        if ( this.hasPrevs != other.hasPrevs ) {
            return false;
        }
        if ( this.hasNexts != other.hasNexts ) {
            return false;
        }
        if ( this.finished != other.finished ) {
            return false;
        }
        if ( this.skip != other.skip ) {
            return false;
        }
        if ( !Objects.equals(this.positionIterableView, other.positionIterableView) ) {
            return false;
        }
        if ( !Objects.equals(this.allVariantPositions, other.allVariantPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.allPatternPositions, other.allPatternPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.prevsVariantPositions, other.prevsVariantPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.prevsPatternPositions, other.prevsPatternPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.nextsVariantPositions, other.nextsVariantPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.nextsPatternPositions, other.nextsPatternPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.startsAfterSeparator, other.startsAfterSeparator) ) {
            return false;
        }
        if ( !Objects.equals(this.endsBeforeSeparator, other.endsBeforeSeparator) ) {
            return false;
        }
        if ( !Objects.equals(this.variantPositionsAtStart, other.variantPositionsAtStart) ) {
            return false;
        }
        if ( !Objects.equals(this.variantPositionsAtEnd, other.variantPositionsAtEnd) ) {
            return false;
        }
        return true;
    }
    
}
