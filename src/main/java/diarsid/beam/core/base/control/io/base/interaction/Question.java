/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Answer.answerOfVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Answer.noAnswerFromVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class Question implements Serializable {
    
    private final String question;
    private final List<Variant> variants;
    
    private Question(String question) {
        this.question = question;
        this.variants = new ArrayList<>();
    }
    
    private Question(String question, List<Variant> variants) {
        this.question = question;
        this.variants = new ArrayList<>();
    }
    
    public static Question question(String question) {
        return new Question(question);
    }
    
    public Question withAnswerVariant(Variant variant) {
        this.variants.add(variant);
        return this;
    }
    
    public Question withAnswerString(String variant) {
        this.variants.add(new Variant(variant, this.variants.size()));
        return this;
    }
    
    public Question withAnswerEntity(ConvertableToVariant convertable) {
        this.variants.add(convertable.toVariant(this.variants.size()));
        return this;
    }
    
    public Question withAnswerStrings(List<String> variants) {
        AtomicInteger indexer = new AtomicInteger(0);
        this.variants.addAll(variants
                        .stream()
                        .map(variantString -> 
                                new Variant(
                                        variantString, 
                                        indexer.getAndIncrement()))
                        .collect(toList())
        );
        return this;
    }
    
    public Question withAnswerStrings(String... variants) {
        AtomicInteger indexer = new AtomicInteger(0);
        this.variants.addAll(stream(variants)
                        .map(variantString -> 
                                new Variant(
                                        variantString, 
                                        indexer.getAndIncrement()))
                        .collect(toList()));
        return this;
    }
    
    public Question withAnswerVariants(List<Variant> variants) {
        this.variants.addAll(variants);
        return this;
    }
    
    public Question withAnswerEntities(List<? extends ConvertableToVariant> variants) {
        AtomicInteger indexer = new AtomicInteger(0);
        this.variants.addAll(variants
                        .stream()
                        .map(convertable -> convertable.toVariant(indexer.getAndIncrement()))
                        .collect(toList())
        );
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
                        return containsIgnoreCase(variant.text(), possibleFragment);
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
