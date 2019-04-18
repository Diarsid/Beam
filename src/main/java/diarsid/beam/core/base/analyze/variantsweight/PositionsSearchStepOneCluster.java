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
import static java.lang.String.format;

import static diarsid.beam.core.base.analyze.variantsweight.ClusterComparison.LEFT_IS_BETTER;
import static diarsid.beam.core.base.analyze.variantsweight.ClusterComparison.LEFT_IS_WORSE;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.MathUtil.zeroIfNegative;
import static diarsid.support.objects.Possibles.possibleButEmpty;
import static diarsid.support.strings.StringUtils.countCharMatchesIn;
import static diarsid.support.strings.StringUtils.isWordsSeparator;

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
        private int i;

        private StepOneClusterPositionView(PositionsSearchStepOneCluster cluster) {
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
    
    private int prev;
    private int main;
    private int next;
    private int prevIndex;
    private int mainIndex;
    private int nextIndex;
    private boolean hasPrevs;
    private boolean hasNexts;
    
    private boolean finished;
    
    private int skip;

    public PositionsSearchStepOneCluster() {
        this.variant = possibleButEmpty();
        this.pattern = possibleButEmpty();
        this.positionView = new StepOneClusterPositionView(this);
        this.patternCluster = new PatternCluster(this);
        this.allVariantPositions = new ArrayList<>();
        this.allPatternPositions = new ArrayList<>();
        this.prevsVariantPositions = new ArrayList<>();
        this.prevsPatternPositions = new ArrayList<>();
        this.nextsVariantPositions = new ArrayList<>();
        this.nextsPatternPositions = new ArrayList<>();
        this.prev = UNINITIALIZED;
        this.main = UNINITIALIZED;
        this.next = UNINITIALIZED;
        this.prevIndex = UNINITIALIZED;
        this.mainIndex = UNINITIALIZED;
        this.nextIndex = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
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
    
    StepOneClusterPositionView positionView() {
        this.finishIfNot();
        this.positionView.i = BEFORE_START;
        return this.positionView;
    }
    
    PatternCluster patternCluster() {
        return this.patternCluster;
    }
    
    int possibleTypoMatches() {
        return this.possibleTypoMatchesBefore + this.possibleTypoMatchesAfter;
    }
    
    boolean doesHaveMorePossibleTypoMatchesThan(PositionsSearchStepOneCluster other) {
        return this.possibleTypoMatches() > other.possibleTypoMatches();
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
    
    private List<Integer> composeVariantPositions() {
        for (int i = this.prevsVariantPositions.size() - 1; i > -1; i--) {
            this.allVariantPositions.add(this.prevsVariantPositions.get(i));
        }
        if ( this.prev > -1 ) {
            this.allVariantPositions.add(this.prev);
        }
        if ( this.main > -1 ) {
            this.allVariantPositions.add(this.main);
        }
        if ( this.next > -1 ) {
            this.allVariantPositions.add(this.next);
        }
        this.allVariantPositions.addAll(this.nextsVariantPositions);
        return this.allVariantPositions;
    }
    
    private List<Integer> composePatternIndexes() {
        for (int i = this.prevsPatternPositions.size() - 1; i > -1; i--) {
            this.allPatternPositions.add(this.prevsPatternPositions.get(i));
        }
        if ( this.prevIndex > -1 ) {
            this.allPatternPositions.add(this.prevIndex);
        }
        if ( this.mainIndex > -1 ) {
            this.allPatternPositions.add(this.mainIndex);
        }
        if ( this.nextIndex > -1 ) {
            this.allPatternPositions.add(this.nextIndex);
        }
        this.allPatternPositions.addAll(this.nextsPatternPositions);
        return this.allPatternPositions;
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
        return this.main > -1;
    }
    
    boolean isNotSet() {
        return this.main < 0;
    }
    
    void setMain(int index, int main) {
        this.main = main;
        this.mainIndex = index;
    }

    void setPrev(int prev) {
        this.prev = prev;
        this.prevIndex = this.mainIndex - 1;
    }

    void setNext(int next) {
        this.next = next;
        this.nextIndex = this.mainIndex + 1;
    }
    
    void addNext(int nextOne) {
        if ( this.next < 0 ) {
            throw new IllegalStateException();
        }
        this.hasNexts = true;
        this.nextsVariantPositions.add(nextOne);
        this.nextsPatternPositions.add(this.nextIndex + this.nextsVariantPositions.size());
    }
    
    void addPrev(int prevOne) {
        if ( this.prev < 0 ) {
            throw new IllegalStateException();
        }
        this.hasPrevs = true;
        this.prevsVariantPositions.add(prevOne);
        this.prevsPatternPositions.add(this.prevIndex - this.prevsVariantPositions.size());
    }
    
    int length() {
        return this.prevsVariantPositions.size() + 3 + this.nextsVariantPositions.size();
    }
    
    boolean isLongerThan(PositionsSearchStepOneCluster other) {
        return this.length() > other.length();
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
        this.positionView.i = BEFORE_START;
        this.allVariantPositions.clear();
        this.prevsVariantPositions.clear();
        this.nextsVariantPositions.clear();
        this.allPatternPositions.clear();
        this.prevsPatternPositions.clear();
        this.nextsPatternPositions.clear();
        this.prev = UNINITIALIZED;
        this.main = UNINITIALIZED;
        this.next = UNINITIALIZED;
        this.prevIndex = UNINITIALIZED;
        this.mainIndex = UNINITIALIZED;
        this.nextIndex = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
        this.finished = true;
        this.skip = 0;
        this.possibleTypoMatchesBefore = 0;
        this.possibleTypoMatchesAfter = 0;
        this.variantPositionsAtStart = null;
        this.startsAfterSeparator = null;
        this.variantPositionsAtEnd = null;
        this.endsBeforeSeparator = null;
    }
    
    @Override
    public String toString() {
        return 
                this.prevsVariantPositions.toString() + format("%s,%s,%s", this.prev, this.main, this.next) + this.nextsVariantPositions.toString() + ", indexes: " + 
                this.prevsPatternPositions.toString() + format("%s,%s,%s", this.prevIndex, this.mainIndex, this.nextIndex) + this.nextsPatternPositions.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.positionView);
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
        hash = 89 * hash + this.prev;
        hash = 89 * hash + this.main;
        hash = 89 * hash + this.next;
        hash = 89 * hash + this.prevIndex;
        hash = 89 * hash + this.mainIndex;
        hash = 89 * hash + this.nextIndex;
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
        if ( this.prev != other.prev ) {
            return false;
        }
        if ( this.main != other.main ) {
            return false;
        }
        if ( this.next != other.next ) {
            return false;
        }
        if ( this.prevIndex != other.prevIndex ) {
            return false;
        }
        if ( this.mainIndex != other.mainIndex ) {
            return false;
        }
        if ( this.nextIndex != other.nextIndex ) {
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
        if ( !Objects.equals(this.positionView, other.positionView) ) {
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
