/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.patternsanalyze;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.util.CollectionsUtils;

import static java.util.Collections.sort;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Answer.answerOfVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Answer.noAnswerFromVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;


/**
 *
 * @author Diarsid
 */
public class WeightedVariantsQuestion implements Serializable {
    
    private final boolean isDiversityAcceptable;
    private final List<WeightedVariant> variants;
    private List<WeightedVariant> currentSimilarVariants;
    private int currentVariantIndex;

    WeightedVariantsQuestion(List<WeightedVariant> variants, boolean isDiversityAcceptable) {
        this.variants = variants;
        sort(this.variants);
        this.currentVariantIndex = 0;
        this.isDiversityAcceptable = isDiversityAcceptable;
        this.currentSimilarVariants = null;
    }
    
    public int size() {
        return this.variants.size();
    }
    
    public WeightedVariant current() {
        if ( this.currentIndexIsInBounds() ) {
            return this.variants.get(this.currentVariantIndex);            
        } else {
            return this.last();
        }
    }
    
    public boolean isChoiceInSimilarVariantsNaturalRange(int number) {
        if ( nonNull(this.currentSimilarVariants) ) {
            return ( 0 < number ) && ( number < (this.currentSimilarVariants.size() + 1) );
        } else {
            return false;
        }
    }
    
    public Answer answerWith(int choiceNumber) {
        if ( this.isChoiceInSimilarVariantsNaturalRange(choiceNumber) ) {
            return answerOfVariant(this.currentSimilarVariants.get(choiceNumber - 1));
        } else {
            return noAnswerFromVariants();
        }
    }
    
    public Answer ifPartOfAnySimilarVariant(String possibleFragment) {
        List<Answer> matches = this.currentSimilarVariants
                .stream()
                .filter(variant -> { 
                    if ( variant.hasDisplayText() ) {
                        return containsIgnoreCase(variant.displayText(), possibleFragment);
                    } else {
                        return containsIgnoreCase(variant.text(), possibleFragment);
                    }
                })
                .map(variant -> answerOfVariant(variant))
                .collect(toList());
        if ( CollectionsUtils.hasOne(matches) ) {
            return CollectionsUtils.getOne(matches);
        } else {
            return noAnswerFromVariants();
        }
    }

    private WeightedVariant last() {
        return this.variants.get(this.variants.size() - 1);
    }
    
    private WeightedVariant next() {
        if ( this.hasNext() ) {
            return this.variants.get(this.currentVariantIndex + 1);
        } else {
            return this.last();
        }
    }
    
    public boolean hasAcceptableDiversity() {
        return this.isDiversityAcceptable;
    }
    
    public void toNext() {
        this.currentVariantIndex++;
        this.currentSimilarVariants = null;
    }
    
    public boolean hasNext() {
        return ( this.currentVariantIndex > -1 ) && 
                ( this.currentVariantIndex < this.variants.size() - 1 );
    }

    private boolean currentIndexIsInBounds() {
        return ( this.currentVariantIndex > -1 ) && 
                ( this.currentVariantIndex < this.variants.size() );
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
    
    public boolean hasOne() {
        return ( this.variants.size() == 1 );
    }
    
    public Answer singleAnswer() {
        return answerOfVariant(getOne(this.variants));
    }
    
    boolean hasMany() {
        return ( this.variants.size() > 1 );
    }
    
    public List<WeightedVariant> allNextSimilar() {
        List<WeightedVariant> similarVariants = new ArrayList();
        boolean currentIsSimilarToNext = this.isCurrentSimilarToNext();
        while ( currentIsSimilarToNext || this.currentVariantIndex == this.variants.size() - 1 ) {
            currentIsSimilarToNext = this.isCurrentSimilarToNext();
            similarVariants.add(this.current());
            this.toNext();
        }
        this.currentSimilarVariants = similarVariants;
        return similarVariants;
    }    
}
