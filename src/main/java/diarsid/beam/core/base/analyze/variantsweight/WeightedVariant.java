/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.analyze.variantsweight;

import java.io.Serializable;

import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimateWeightOf;

/**
 *
 * @author Diarsid
 */
public class WeightedVariant 
        extends 
                Variant 
        implements 
                Comparable<Variant>, 
                Serializable {
    
    private double weight;

    WeightedVariant(Variant parent, double weight) {
        super(parent);
        this.weight = weight;
    }
    
    public boolean hasEqualOrBetterWeightThan(WeightEstimate estimate) {
        WeightEstimate thisEstimate = estimateWeightOf(this);
        boolean hasEqualOrBetter = thisEstimate.isEqualOrBetterThan(estimate);
        return hasEqualOrBetter;
    }
    
    public boolean betterThan(WeightedVariant other) {
        return this.weight < other.weight;
    }

    public double weight() {
        return this.weight;
    }
    
    void adjustWeight(double delta) {
        this.weight = this.weight - delta;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + 
                (int) (Double.doubleToLongBits(this.weight) ^ 
                (Double.doubleToLongBits(this.weight) >>> 32));
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
        final WeightedVariant other = ( WeightedVariant ) obj;
        if ( Double.doubleToLongBits(this.weight) != Double.doubleToLongBits(other.weight) ) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Variant other) {
        if ( other instanceof WeightedVariant ) {
            if ( this.weight > ((WeightedVariant) other).weight) {
                return 1;
            } else if ( this.weight < ((WeightedVariant) other).weight ) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return super.compareTo(other);
        }        
    }    
}
