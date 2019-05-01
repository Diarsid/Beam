/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.analyze.variantsweight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.util.CollectionsUtils;

import static java.util.Collections.sort;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Answers.answerOfVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.variantsDontContainSatisfiableAnswer;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.support.strings.StringUtils.lower;


/**
 *
 * @author Diarsid
 */
public class Variants implements Serializable {
    
    private final List<Variant> variants;
    private List<Variant> currentSimilarVariants;
    private int currentVariantIndex;

    Variants(List<Variant> variants) {
        this.variants = new ArrayList<>(variants);
        sort(this.variants);
        this.currentVariantIndex = -1;
        this.currentSimilarVariants = null;
    }
    
    public static Variants unite(List<Variant> variants) {
        return new Variants(variants);
    }
    
    public static Optional<Variant> findVariantEqualToPattern(
            List<Variant> variants) {
        return variants
                .stream()
                .filter(variant -> variant.isEqualToPattern())
                .findFirst();
    }
    
    public IntStream indexes() {
        return this.variants.stream().mapToInt(variant -> variant.index());
    }
    
    public void resetTraversing() {
        this.currentVariantIndex = -1;
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
    }
    
    public void setTraversingToPositionBefore(int variantIndex) {
        if ( variantIndex > -1 ) {
            variantIndex--; // adjust for subsequent .next() call
        }
        this.currentVariantIndex = variantIndex;
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
    } 
    
    public int currentTraverseIndex() {
        if ( nonEmpty(this.currentSimilarVariants) ) {
            // +1 is used in orded to balance subsequent index-- when .next() will be called.
            return this.currentVariantIndex - this.currentSimilarVariants.size() + 1;
        } else {
            return this.currentVariantIndex;
        }        
    }
    
    public boolean isEmpty() {
        return this.variants.isEmpty();
    }
    
    public boolean isNotEmpty() {
        return ! this.variants.isEmpty();
    }
    
    public Variant best() {
        return this.variants.get(0);
    }
    
    public String stamp() {
        return this.variants
                .stream()
                .map(variant -> lower(variant.text()))
                .collect(joining(";"));
    }
    
    public void removeWorseThan(String variantValue) {
        boolean needToRemove = false;
        for (int i = 0; i < this.variants.size(); i++) {
            if ( needToRemove ) {
                this.variants.remove(i);
                i--;
            } else {
                if ( this.variants.get(i).text().equalsIgnoreCase(variantValue) ) {
                    needToRemove = true;
                }
            }
        }
    }
    
    public String getVariantAt(int i) {
        return this.variants.get(i).text();
    }
    
    public int size() {
        return this.variants.size();
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
            return variantsDontContainSatisfiableAnswer();
        }
    }
    
    public Answer ifPartOfAnySimilarVariant(String possibleFragment) {
        List<Answer> matches = this.currentSimilarVariants
                .stream()
                .filter(variant -> { 
                    if ( variant.doesHaveName() ) {
                        return containsIgnoreCase(variant.name(), possibleFragment);
                    } else {
                        return containsIgnoreCase(variant.text(), possibleFragment);
                    }
                })
                .map(variant -> answerOfVariant(variant))
                .collect(toList());
        if ( CollectionsUtils.hasOne(matches) ) {
            return CollectionsUtils.getOne(matches);
        } else {
            return variantsDontContainSatisfiableAnswer();
        }
    }
    
    public boolean hasOne() {
        return ( this.variants.size() == 1 );
    }
    
    public boolean hasMany() {
        return ( this.variants.size() > 1 );
    }
    
    public Answer singleAnswer() {
        return answerOfVariant(getOne(this.variants));
    }
    
    public boolean next() {
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
        if ( this.currentVariantIndex < this.variants.size() - 1 ) {
            this.currentVariantIndex++;
            return true;
        } else {
            return false;
        }
    }
    
    public boolean currentIsMuchBetterThanNext() {
        if ( this.currentVariantIndex < this.variants.size() - 1 ) {
            double currentWeight = this.current().weight();
            double nextWeight = this.variants.get(this.currentVariantIndex + 1).weight();
            
            if ( this.currentVariantIndex < 0 ) {
                throw new IllegalStateException(
                        "Unexpected behavior: call .next() before accessing variants!");
            } else if ( this.currentVariantIndex == 0 ) {
                return absDiff(currentWeight, nextWeight) >= 1.0;
            } else if ( this.currentVariantIndex < 4 ) {
                return absDiff(currentWeight, nextWeight) >= 2.0;
            } else {
                return ( currentWeight * 0.8 < nextWeight );
            }
            
//            return ( absDiff(currentWeight, nextWeight) < abs(currentWeight * 0.2) );
//            if ( currentWeight < 5.0 ) {
//                return ( nextWeight - currentWeight ) >= 1.0;
//            } else {
//                double currentDouble = currentWeight * 1.0;
//                double nextDouble = nextWeight * 0.75;
//                return currentDouble < nextDouble;
//            }
        } else if ( this.currentVariantIndex == this.variants.size() - 1 ) {
            return true;
        } else {
            throw new IllegalStateException("Unexpected behavior.");
        }
    }
    
    public Variant current() {
        return this.variants.get(this.currentVariantIndex);
    }
    
    public List<Variant> nextSimilarVariants() {
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        } else {
            this.currentSimilarVariants = new ArrayList<>();
        }        
        boolean proceed = true;
        while ( this.currentVariantIndex < this.variants.size() && proceed ) {
            if ( ! this.currentIsMuchBetterThanNext() ) {
                this.currentSimilarVariants.add(this.current());
                this.currentVariantIndex++;
            } else {
                this.currentSimilarVariants.add(this.current());
                proceed = false;
            }            
        }
        return this.currentSimilarVariants;
    }
}
