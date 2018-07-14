/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data.util;

/**
 *
 * @author Diarsid
 */
class ModifiablePatternLikeness {
    
    private final static int DEFAULT_LIKENESS_PERCENT = 90;
    private final static int LIKENESS_PERCENT_CHANGE = 10;
    
    private int patternLength;
    private int matchesChange;
    private int matchesMinLimit;
    private int matches;
    private boolean patternEven;

    ModifiablePatternLikeness() {
        this.toInitialState();
    }

    private static int defineMatchesChangeDependingOn(
            int patternLength, int likenessPercentChange) {
        int singleCharPercent = 100 / patternLength;
        if ( singleCharPercent > likenessPercentChange ) {
            return 1;
        } else {
            int whole = likenessPercentChange / singleCharPercent;
            if ( likenessPercentChange % singleCharPercent > 0 ) {
                whole++;
            }
            return whole;
        }
    }
    
    static int requiredMatchesDependingOn(int patternLength, int likenessPercent) {
        if ( patternLength == 2 ) {
            return 2;
        } else {
            int requiredMatches = ( patternLength * likenessPercent ) / 100;
            if ( requiredMatches < 2 ) {
                requiredMatches = 2;
            }
            return requiredMatches;
        }        
    }
    
    private void toInitialState() {
        this.matchesMinLimit = 0;
        this.patternLength = 0;
        this.matchesChange = 0;
        this.matches = 0;
        this.patternEven = false;
    }
    
    void clear() {
        this.toInitialState();
    }
    
    void setPatternLength(int patternLength) {
        this.patternLength = patternLength;
        this.matchesMinLimit = this.patternLength / 2 + this.patternLength % 2;
        this.matchesChange = defineMatchesChangeDependingOn(
                this.patternLength, LIKENESS_PERCENT_CHANGE);
        if ( this.matchesChange == 1 ) {
            this.patternEven = patternLength % 2 == 0;
        }
        this.matches = requiredMatchesDependingOn(this.patternLength, DEFAULT_LIKENESS_PERCENT);
    }
    
    int requiredMatches() {
        return this.matches;
    }
    
    boolean isNextDecreaseMeaningfull() {
        int oldMatches = this.matches;
        int newMatches = oldMatches - this.matchesChange;
        return newMatches > this.matchesMinLimit;
    }
    
    void decrease() {
        int oldMatches = this.matches;
        int newMatches = oldMatches - this.matchesChange;
        
        if ( this.matchesChange == 1 ) {
            if ( this.patternEven ) {
                if ( newMatches <= this.matchesMinLimit ) {
                    newMatches = oldMatches;
                }
            } else {
                if ( newMatches < this.matchesMinLimit ) {
                    newMatches = oldMatches;
                }
            }             
        } else {
            if ( newMatches <= this.matchesMinLimit ) {
                newMatches = oldMatches;
            }            
        }
        
        this.matches = newMatches;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.patternLength;
        hash = 67 * hash + this.matchesChange;
        hash = 67 * hash + this.matchesMinLimit;
        hash = 67 * hash + this.matches;
        return hash;
    }
    
    public boolean notEquals(ModifiablePatternLikeness other) {
        return ! this.equals(other);
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
        final ModifiablePatternLikeness other = ( ModifiablePatternLikeness ) obj;
        if ( this.patternLength != other.patternLength ) {
            return false;
        }
        if ( this.matchesChange != other.matchesChange ) {
            return false;
        }
        if ( this.matchesMinLimit != other.matchesMinLimit ) {
            return false;
        }
        if ( this.matches != other.matches ) {
            return false;
        }
        return true;
    }
    
}
