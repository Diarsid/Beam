/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.control.io.base.Answer.answerOfVariant;
import static diarsid.beam.core.control.io.base.Answer.noAnswerFromVariants;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.util.CollectionsUtils.hasOne;

/**
 *
 * @author Diarsid
 */
public class Question implements Serializable {
    
    private final String question;
    private final List<Variant> variants;
    
    public Question(String question) {
        this.question = question;
        this.variants = new ArrayList<>();
    }
    
    public Question(String question, List<Variant> variants) {
        this.question = question;
        this.variants = new ArrayList<>();
    }
    
    public static Question questionWithEntites(
            String question, List<? extends ConvertableToVariant> variants) {
        AtomicInteger indexer = new AtomicInteger(0);
        return new Question(
                question, 
                variants
                        .stream()
                        .map(convertable -> convertable.convertToVariant(indexer.getAndIncrement()))
                        .collect(toList())
        );
    }
    
    public static Question questionWithStrings(String question, List<String> variants) {
        AtomicInteger indexer = new AtomicInteger(0);
        return new Question(
                question, 
                variants
                        .stream()
                        .map(variantString -> new Variant(variantString, indexer.getAndIncrement()))
                        .collect(toList()));
    }
    
    public Question with(Variant variant) {
        this.variants.add(variant);
        return this;
    }
    
    public Question withVariant(String variant) {
        this.variants.add(new Variant(variant, this.variants.size()));
        return this;
    }

    public String getQuestion() {
        return this.question;
    }

    public List<Variant> getVariants() {
        return this.variants;
    }
    
    public boolean isChoiceInVariantsNaturalRange(int number) {
        // numbers are 1-based, not 0-based.
        return ( 0 < number ) && ( number < (this.variants.size() + 1) );
    }
    
    public Answer ifPartOfAnyVariant(String possibleFragment) {
        List<Answer> matches = this.variants
                .stream()
                .filter(variant -> { 
                    if ( variant.hasDisplayText() ) {
                        return containsIgnoreCase(variant.getDisplayText(), possibleFragment);
                    } else {
                        return containsIgnoreCase(variant.getText(), possibleFragment);
                    }
                })
                .map(variant -> answerOfVariant(variant))
                .collect(toList());
        if ( hasOne(matches) ) {
            return getOne(matches);
        } else {
            return noAnswerFromVariants();
        }
    }
    
    public Answer answerWith(int choiceNumber) {
        if ( this.isChoiceInVariantsNaturalRange(choiceNumber) ) {
            return answerOfVariant(this.variants.get(choiceNumber - 1));
        } else {
            return noAnswerFromVariants();
        }
    }
}
