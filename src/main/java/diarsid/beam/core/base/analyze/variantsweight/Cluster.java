/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import diarsid.beam.core.base.objects.CachedReusable;


/**
 *
 * @author Diarsid
 */
class Cluster extends CachedReusable {
    
    static {
        CachedReusable.createCacheFor(Cluster.class, () -> new Cluster());
    }
    
    private int length;
    private int ordersDiffMean;
    private int ordersDiffSum;
    private int ordersDiffCount;
    private int ordersDiffShifts;
    private boolean ordersDiffHaveCompensation;

    private Cluster() {
        super();
        this.length = 0;
        this.ordersDiffMean = 0;
        this.ordersDiffSum = 0;
        this.ordersDiffCount = 0;
        this.ordersDiffShifts = 0;
        this.ordersDiffHaveCompensation = false;
    }
    
    Cluster set(
            int length, 
            int mean, 
            int diffSum, 
            int diffCount, 
            int shifts, 
            boolean haveCompensation) {
        this.length = length;
        this.ordersDiffMean = mean;
        this.ordersDiffSum = diffSum;
        this.ordersDiffCount = diffCount;
        this.ordersDiffShifts = shifts;
        this.ordersDiffHaveCompensation = haveCompensation;
        return this;
    }
    
    int length() {
        return this.length;
    }
    
    int ordersDiffMean() {
        return this.ordersDiffMean;
    }

    int ordersDiffSum() {
        return this.ordersDiffSum;
    }
    
    int ordersDiffCount() {
        return this.ordersDiffCount;
    }

    int ordersDiffShifts() {
        return this.ordersDiffShifts;
    }
    
    boolean hasOrdersDiff() {
        return this.ordersDiffSum > 0;
    }
    
    boolean hasOrdersDiffShifts() {
        return this.ordersDiffShifts > 0;
    }
    
    boolean haveOrdersDiffCompensations() {
        return this.ordersDiffHaveCompensation;
    }

    @Override
    public void clearForReuse() {
        this.length = 0;
        this.ordersDiffMean = 0;
        this.ordersDiffSum = 0;
        this.ordersDiffCount = 0;
        this.ordersDiffShifts = 0;
        this.ordersDiffHaveCompensation = false;
    }
}
