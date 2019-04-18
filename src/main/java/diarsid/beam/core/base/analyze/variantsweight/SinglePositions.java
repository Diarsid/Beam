/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;

import static diarsid.beam.core.base.util.CollectionsUtils.lastFrom;

/**
 *
 * @author Diarsid
 */
class SinglePositions {
    
    private static enum Event {
        ADDED,
        MISSED,
        UNINIT
    }
    
    private final static Integer MISS = -3;
    
    private final List<Integer> positions;
    private final List<Integer> uninterruptedPositions;
    private int added;
    private int missed;
    private Event lastEvent;

    SinglePositions() {
        this.positions = new ArrayList<>();
        this.uninterruptedPositions = new ArrayList<>();
        this.added = 0;
        this.missed = 0;
        this.lastEvent = SinglePositions.Event.UNINIT;
    }
    
    void clear() {
        this.positions.clear();
        this.uninterruptedPositions.clear();
        this.added = 0;
        this.missed = 0;
        this.lastEvent = SinglePositions.Event.UNINIT;
    }
    
    void add(int position) {
        if ( this.lastEvent.equals(SinglePositions.Event.ADDED) ) {
            if ( this.uninterruptedPositions.isEmpty() ) {
                this.uninterruptedPositions.add(lastFrom(this.positions));
            }
            this.uninterruptedPositions.add(position);
        }
        this.positions.add(position);
        this.added++;
        this.lastEvent = SinglePositions.Event.ADDED;        
    }
    
    void miss() {
        this.positions.add(MISS);
        this.missed++;
        this.lastEvent = SinglePositions.Event.MISSED;
    }
    
    void end() {
        
    }
    
    boolean doHaveUninterruptedRow() {
        return ! this.uninterruptedPositions.isEmpty();
    }
    
    List<Integer> uninterruptedRow() {
        return this.uninterruptedPositions;
    }

//    private void tryToProcessUninterruptedPositions() {
//        if ( this.lastEvent.equals(SinglePositions.Event.ADDED) ) {
//            if ( nonEmpty(this.uninterruptedPositions) ) {
//                this.uninterruptedPositionsProcessor.accept(this.uninterruptedPositions);
//                this.uninterruptedPositions.clear();
//            }
//        }
//    }
    
    int quantity() {
        return this.added;
    }
}
