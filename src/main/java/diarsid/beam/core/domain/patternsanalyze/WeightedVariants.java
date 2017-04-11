/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.patternsanalyze;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.util.CollectionsUtils;

import static java.util.Collections.sort;


/**
 *
 * @author Diarsid
 */
public class WeightedVariants {
    
    private final List<WeightedVariant> variants;
    private int currentCandidateIndex;

    WeightedVariants(List<WeightedVariant> candidates) {
        this.variants = candidates;
        sort(this.variants);
        this.currentCandidateIndex = 0;
    }
    
    public WeightedVariant current() {
        if ( this.currentIndexIsInBounds() ) {
            return this.variants.get(this.currentCandidateIndex);            
        } else {
            return this.last();
        }
    }

    private WeightedVariant last() {
        return this.variants.get(this.variants.size() - 1);
    }
    
    WeightedVariant next() {
        if ( this.hasNext() ) {
            return this.variants.get(this.currentCandidateIndex + 1);
        } else {
            return this.last();
        }
    }
    
    public boolean hasAcceptableDiversity() {
        
    }
    
    public void toNext() {
        this.currentCandidateIndex++;
    }
    
    public boolean hasNext() {
        return ( this.currentCandidateIndex > -1 ) && 
                ( this.currentCandidateIndex < this.variants.size() - 1 );
    }

    private boolean currentIndexIsInBounds() {
        return ( this.currentCandidateIndex > -1 ) && 
                ( this.currentCandidateIndex < this.variants.size() );
    }
    
    public boolean isCurrentMuchBetterThanNext() {
        return this.hasNext() && this.currentWeightIsBetterThanNextWeight();
    }
    
    private boolean isCurrentSimilarToNext() {
        return this.hasNext() && ! this.currentWeightIsBetterThanNextWeight();
    }
    
    private boolean currentWeightIsBetterThanNextWeight() {
        double current = this.current().weight();
        double next = this.next().weight();
        if ( current < 5.0 ) {
            return ( next - current ) > 1.0;
        } else {
            double currentDouble = current * 1.0;
            double nextDouble = next * 0.75;
            //System.out.println(format("current-vs-next : %s-vs-%s", currentDouble, nextDouble));
            return currentDouble < nextDouble;
        }        
        //return ( this.current().weight() * 1.0 ) < ( this.next().weight() * 0.6 );
    }
    
    boolean hasOne() {
        return ( this.variants.size() == 1 );
    }
    
    boolean hasMany() {
        return ( this.variants.size() > 1 );
    }
    
    WeightedVariant getOne() {
        return CollectionsUtils.getOne(this.variants);
    }
    
    public List<WeightedVariant> allNextSimilar() {
        List<WeightedVariant> similarVariants = new ArrayList();
        boolean currentIsSimilarToNext = this.isCurrentSimilarToNext();
        while ( currentIsSimilarToNext ) {
            currentIsSimilarToNext = this.isCurrentSimilarToNext();
            similarVariants.add(this.current());
            this.toNext();
        }
        return similarVariants;
    }
    
}
