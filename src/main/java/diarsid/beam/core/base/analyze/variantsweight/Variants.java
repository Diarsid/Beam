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

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Answers.answerOfVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.variantsDontContainSatisfiableAnswer;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.MathUtil.absDiff;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.startsIgnoreCase;
import static diarsid.support.strings.StringUtils.lower;


/**
 *
 * @author Diarsid
 */
public class Variants implements Serializable {
    
    private static final int FEED_ALGORITHM_VERSION = 2;
    
    private final List<Variant> variants;
    private final double bestWeight;
    private final double worstWeight;
    private final double weightDifference;
    private final double weightStep;
    private List<Variant> currentSimilarVariants;
    private int currentVariantIndex;

    Variants(List<Variant> variants) {
        sort(variants);
        this.variants = unmodifiableList(variants);
        if ( nonEmpty(this.variants) ) {
            this.bestWeight = variants.get(0).weight();
            this.worstWeight = last(variants).weight();
            this.weightDifference = absDiff(this.bestWeight, this.worstWeight);
            this.weightStep = this.weightDifference / this.variants.size();
        } else {
            this.bestWeight = 0;
            this.worstWeight = 0;
            this.weightDifference = 0;
            this.weightStep = 0;
        }
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
                .map(variant -> lower(variant.value()))
                .collect(joining(";"));
    }
    
    public void removeHavingSameStartAs(Variant variant) {
        Variant current;
        
        for (int i = 0; i < this.variants.size(); i++) {
            current = this.variants.get(i);
            if ( current.index() <= variant.index()) {
                continue;
            }
            
            if ( startsIgnoreCase(current.nameOrValue(), variant.nameOrValue()) ) {
                this.variants.remove(i);
                i--;
            }
        }
    }
    
    public Variants removeWorseThan(String variantValue) {
        List<Variant> moidifiableVariants = new ArrayList<>(this.variants);
        boolean needToRemove = false;
        for (int i = 0; i < moidifiableVariants.size(); i++) {
            if ( needToRemove ) {
                moidifiableVariants.remove(i);
                i--;
            } else {
                if ( moidifiableVariants.get(i).value().equalsIgnoreCase(variantValue) ) {
                    needToRemove = true;
                }
            }
        }
        
        return new Variants(moidifiableVariants);
    }
    
    public String getVariantAt(int i) {
        return this.variants.get(i).value();
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
                        return containsIgnoreCase(variant.value(), possibleFragment);
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
            if ( this.currentVariantIndex < 0 ) {
                throw new IllegalStateException(
                        "Unexpected behavior: call .next() before accessing variants!");
            }
            
            double currentWeight = this.current().weight();
            double nextWeight = this.variants.get(this.currentVariantIndex + 1).weight();
            
            switch ( FEED_ALGORITHM_VERSION ) {
                case 1 : {
                    if ( this.currentVariantIndex == 0 ) {
                        return absDiff(currentWeight, nextWeight) >= 1.0;
                    } else if ( this.currentVariantIndex < 4 ) {
                        return absDiff(currentWeight, nextWeight) >= 2.0;
                    } else {
                        return ( currentWeight * 0.8 < nextWeight );
                    }
                }
                case 2 : {
                    if ( this.currentVariantIndex == 0 ) {
                        return absDiff(currentWeight, nextWeight) >= this.weightStep;
                    } else if ( this.currentVariantIndex < 4 ) {
                        return absDiff(currentWeight, nextWeight) >= this.weightDifference / 4 ;
                    } else {
                        return absDiff(currentWeight, nextWeight) >= this.weightDifference / 2 ;
                    }
                }
                default : {
                    throw new IllegalStateException(format(
                            "There is not feed algorithm with version %s", FEED_ALGORITHM_VERSION));
                }
            }
            
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
