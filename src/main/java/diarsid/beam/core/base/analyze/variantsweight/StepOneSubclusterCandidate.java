/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diarsid
 */
class StepOneSubclusterCandidate {
    
    private static final int UNINITIALIZED = -5;
    
    private final List<Integer> prevs;
    private final List<Integer> nexts;
    private int prev;
    private int main;
    private int next;
    private boolean hasPrevs;
    private boolean hasNexts;

    public StepOneSubclusterCandidate() {
        this.prevs = new ArrayList<>();
        this.nexts = new ArrayList<>();
        this.prev = UNINITIALIZED;
        this.main = UNINITIALIZED;
        this.next = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
    }
    
    void set(int main) {
        this.prev = main - 1;
        this.main = main;
        this.next = main + 1;
    }
    
    void addNext(int nextOne) {
        this.hasNexts = true;
        this.nexts.add(nextOne);
    }
    
    void addPrev(int prevOne) {
        this.hasPrevs = true;
        this.prevs.add(prevOne);
    }
    
    int length() {
        return this.prevs.size() + 3 + this.nexts.size();
    }
    
    boolean isBetterThan(StepOneSubclusterCandidate other) {
        return this.length() >= other.length();
    }
    
    void clear() {
        this.prevs.clear();
        this.nexts.clear();
        this.prev = UNINITIALIZED;
        this.main = UNINITIALIZED;
        this.next = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
    }
    
}
