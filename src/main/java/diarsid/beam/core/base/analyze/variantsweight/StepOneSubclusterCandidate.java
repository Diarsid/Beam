/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 *
 * @author Diarsid
 */
class StepOneSubclusterCandidate {
    
    private static final int UNINITIALIZED = -5;
    
    private final List<Integer> all;
    private final List<Integer> allIndexes;
    private final List<Integer> prevs;
    private final List<Integer> prevsIndexes;
    private final List<Integer> nexts;
    private final List<Integer> nextsIndexes;
    private int prev;
    private int main;
    private int next;
    private int prevIndex;
    private int mainIndex;
    private int nextIndex;
    private boolean hasPrevs;
    private boolean hasNexts;
    private int skip;

    public StepOneSubclusterCandidate() {
        this.all = new ArrayList<>();
        this.allIndexes = new ArrayList<>();
        this.prevs = new ArrayList<>();
        this.prevsIndexes = new ArrayList<>();
        this.nexts = new ArrayList<>();
        this.nextsIndexes = new ArrayList<>();
        this.prev = UNINITIALIZED;
        this.main = UNINITIALIZED;
        this.next = UNINITIALIZED;
        this.prevIndex = UNINITIALIZED;
        this.mainIndex = UNINITIALIZED;
        this.nextIndex = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
        this.skip = 0;
    }
    
    void incrementSkip() {
        this.skip++;
    }
    
    int skip() {
        return this.skip;
    }
    
    List<Integer> found() {
        for (int i = this.prevs.size() - 1; i > -1; i--) {
            this.all.add(this.prevs.get(i));
        }
        if ( this.prev > -1 ) {
            this.all.add(this.prev);
        }
        if ( this.main > -1 ) {
            this.all.add(this.main);
        }
        if ( this.next > -1 ) {
            this.all.add(this.next);
        }
        this.all.addAll(this.nexts);
        return this.all;
    }
    
    List<Integer> foundIndexes() {
        for (int i = this.prevsIndexes.size() - 1; i > -1; i--) {
            this.allIndexes.add(this.prevsIndexes.get(i));
        }
        if ( this.prevIndex > -1 ) {
            this.allIndexes.add(this.prevIndex);
        }
        if ( this.mainIndex > -1 ) {
            this.allIndexes.add(this.mainIndex);
        }
        if ( this.nextIndex > -1 ) {
            this.allIndexes.add(this.nextIndex);
        }
        this.allIndexes.addAll(this.nextsIndexes);
        return this.allIndexes;
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
        this.nexts.add(nextOne);
        this.nextsIndexes.add(this.nextIndex + this.nexts.size());
    }
    
    void addPrev(int prevOne) {
        if ( this.prev < 0 ) {
            throw new IllegalStateException();
        }
        this.hasPrevs = true;
        this.prevs.add(prevOne);
        this.prevsIndexes.add(this.prevIndex - this.prevs.size());
    }
    
    int length() {
        return this.prevs.size() + 3 + this.nexts.size();
    }
    
    boolean isBetterThan(StepOneSubclusterCandidate other) {
        return this.length() >= other.length();
    }
    
    void clear() {
        this.all.clear();
        this.prevs.clear();
        this.nexts.clear();
        this.allIndexes.clear();
        this.prevsIndexes.clear();
        this.nextsIndexes.clear();
        this.prev = UNINITIALIZED;
        this.main = UNINITIALIZED;
        this.next = UNINITIALIZED;
        this.prevIndex = UNINITIALIZED;
        this.mainIndex = UNINITIALIZED;
        this.nextIndex = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
        this.skip = 0;
    }
    
    @Override
    public String toString() {
        return 
                this.prevs.toString() + format("%s,%s,%s", this.prev, this.main, this.next) + this.nexts.toString() + ", indexes: " + 
                this.prevsIndexes.toString() + format("%s,%s,%s", this.prevIndex, this.mainIndex, this.nextIndex) + this.nextsIndexes.toString();
    }
    
}
