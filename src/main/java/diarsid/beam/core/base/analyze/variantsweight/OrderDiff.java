/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import diarsid.beam.core.base.objects.Clearable;
import diarsid.beam.core.base.objects.SyncCache;

/**
 *
 * @author Diarsid
 */
class OrderDiff implements Clearable {
    
    private final static SyncCache<OrderDiff> CACHE;
    
    static {
        CACHE = new SyncCache<>(() -> new OrderDiff());
    }
    
    private int diffSum;
    private int shifts;
    private boolean haveCompensation;

    private OrderDiff() {
        this.diffSum = 0;
        this.shifts = 0;
        this.haveCompensation = false;
    }
    
    static OrderDiff orderDiffResultOf(int diffSum, int shifts, boolean haveCompensation) {
        return CACHE.get().set(diffSum, shifts, haveCompensation);
    }
    
    static void back(OrderDiff orderDiffResult) {
        CACHE.back(orderDiffResult);
    }
    
    OrderDiff set(int diffSum, int shifts, boolean haveCompensation) {
        this.diffSum = diffSum;
        this.shifts = shifts;
        this.haveCompensation = haveCompensation;
        return this;
    }

    int diffSum() {
        return diffSum;
    }

    int shifts() {
        return shifts;
    }
    
    boolean hasDiff() {
        return this.diffSum > 0;
    }
    
    boolean hasShifts() {
        return this.shifts > 0;
    }
    
    boolean haveCompensations() {
        return this.haveCompensation;
    }

    @Override
    public void clear() {
        this.diffSum = 0;
        this.shifts = 0;
        this.haveCompensation = false;
    }
}
