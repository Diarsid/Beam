/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;


import diarsid.support.objects.PooledReusable;

import static java.lang.String.format;

import static diarsid.beam.core.base.analyze.variantsweight.AnalyzePositionsData.POS_UNINITIALIZED;


/**
 *
 * @author Diarsid
 */
class Cluster 
        extends 
                PooledReusable 
        implements 
                Comparable<Cluster> {
    
    private int firstPosition;
    private int length;
    private int ordersDiffMean;
    private int ordersDiffSum;
    private int ordersDiffCount;
    private int ordersDiffShifts;
    private boolean ordersDiffHaveCompensation;
    private int compensationSum;

    Cluster() {
        super();
        this.firstPosition = POS_UNINITIALIZED;
        this.length = 0;
        this.ordersDiffMean = 0;
        this.ordersDiffSum = 0;
        this.ordersDiffCount = 0;
        this.ordersDiffShifts = 0;
        this.ordersDiffHaveCompensation = false;
        this.compensationSum = 0;
    }
    
    Cluster set(
            int firstPosition,
            int length, 
            int mean, 
            int diffSum, 
            int diffCount, 
            int shifts, 
            boolean haveCompensation,
            int compensationSum) {
        this.firstPosition = firstPosition;
        this.length = length;
        this.ordersDiffMean = mean;
        this.ordersDiffSum = diffSum;
        this.ordersDiffCount = diffCount;
        this.ordersDiffShifts = shifts;
        this.ordersDiffHaveCompensation = haveCompensation;
        this.compensationSum = compensationSum;
        return this;
    }
    
    int firstPosition() {
        return this.firstPosition;
    }
    
    int lastPosition() {
        return this.firstPosition + this.length - 1;
    }
    
    int length() {
        return this.length;
    }
    
    int positionsMean() {
        return (this.firstPosition * 2 + this.length) / 2;
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
    
    int compensationSum() {
        return this.compensationSum;
    }

    @Override
    public void clearForReuse() {
        this.firstPosition = POS_UNINITIALIZED;
        this.length = 0;
        this.ordersDiffMean = 0;
        this.ordersDiffSum = 0;
        this.ordersDiffCount = 0;
        this.ordersDiffShifts = 0;
        this.ordersDiffHaveCompensation = false;
        this.compensationSum = 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.firstPosition;
        hash = 79 * hash + this.length;
        hash = 79 * hash + this.ordersDiffMean;
        hash = 79 * hash + this.ordersDiffSum;
        hash = 79 * hash + this.ordersDiffCount;
        hash = 79 * hash + this.ordersDiffShifts;
        hash = 79 * hash + (this.ordersDiffHaveCompensation ? 1 : 0);
        hash = 79 * hash + this.compensationSum;
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
        final Cluster other = ( Cluster ) obj;
        if ( this.firstPosition != other.firstPosition ) {
            return false;
        }
        if ( this.length != other.length ) {
            return false;
        }
        if ( this.ordersDiffMean != other.ordersDiffMean ) {
            return false;
        }
        if ( this.ordersDiffSum != other.ordersDiffSum ) {
            return false;
        }
        if ( this.ordersDiffCount != other.ordersDiffCount ) {
            return false;
        }
        if ( this.ordersDiffShifts != other.ordersDiffShifts ) {
            return false;
        }
        if ( this.ordersDiffHaveCompensation != other.ordersDiffHaveCompensation ) {
            return false;
        }
        if ( this.compensationSum != other.compensationSum ) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Cluster other) {
        if ( this.firstPosition < other.firstPosition ) {
            return -1;
        } else if ( this.firstPosition > other.firstPosition ) {
            return 1;
        } else {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        switch ( this.length ) {
            case 0:
            case 1:
                throw new IllegalStateException("Cluster cannot have length lower than 2");
            case 2:
                return format("Cluster[%s,%s]", this.firstPosition, this.lastPosition());
            case 3:
                return format("Cluster[%s,%s,%s]", 
                              this.firstPosition, this.firstPosition + 1, this.lastPosition());
            case 4:
                return format("Cluster[%s,%s,%s,%s]", 
                          this.firstPosition, this.firstPosition + 1, this.firstPosition + 2, this.lastPosition());
            default:
                return format("Cluster[%s...%s]", this.firstPosition, this.lastPosition());
        }
        
    }
    
}
