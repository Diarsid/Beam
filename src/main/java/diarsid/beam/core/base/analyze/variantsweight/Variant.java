/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.analyze.variantsweight;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.estimateWeightOf;
import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class Variant implements Serializable, Comparable<Variant> {
    
    private final String text;
    private final String name;
    private final int index;
    
    private Boolean equalsToPattern;
    private double weight;
    
    public Variant(String text, int variantIndex) {
        this.text = text;
        this.name = "";
        this.index = variantIndex;
    }
    
    public Variant(String text, String displayText, int variantIndex) {
        this.text = text;
        this.name = displayText;
        this.index = variantIndex;
    }
    
    protected Variant(Variant other) {
        this.text = other.text;
        this.name = other.name;
        this.index = other.index;
    }
    
    public boolean doesHaveName() {
        return ! this.name.isEmpty();
    }

    public String bestText() {
        return this.doesHaveName() ? this.name : this.text;
    }
    
    public String text() {
        return this.text;
    }

    public String name() {
        return this.name;
    }
    
    public Variant set(double weight, boolean equalsToPattern) {
        if ( nonNull(this.equalsToPattern) ) {
            throw new IllegalStateException();
        }
        
        this.weight = weight;
        this.equalsToPattern = equalsToPattern;
        
        return this;
    }
    
    public boolean hasEqualOrBetterWeightThan(WeightEstimate otherEstimate) {
        return estimateWeightOf(this).isEqualOrBetterThan(otherEstimate);
    }
    
    public boolean isBetterThan(Variant other) {
        return this.weight < other.weight;
    }
    
    public boolean isEqualToPattern() {
        return this.equalsToPattern;
    }

    public double weight() {
        return this.weight;
    }
    
    public boolean equalsByLowerText(Variant variant) {
        return lower(this.text).equals(lower(variant.text));
    }
    
    public boolean equalsByLowerName(Variant variant) {
        return this.doesHaveName() && 
                lower(this.name).equals(lower(variant.name));
    }
    
    public int index() {
        return this.index;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.text);
        hash = 89 * hash + Objects.hashCode(this.name);
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
        final Variant other = ( Variant ) obj;
        if ( !Objects.equals(this.text, other.text) ) {
            return false;
        }
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Variant other) {
        if ( this.weight > other.weight) {
            return 1;
        } else if ( this.weight < other.weight ) {
            return -1;
        } else {
            if ( this.index >  other.index ) {
                return 1;
            } else if ( this.index < other.index ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
