/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import diarsid.beam.core.base.analyze.variantsweight.ConvertableToVariant;
import diarsid.beam.core.base.analyze.variantsweight.Variant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Answers.answerOfVariant;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.variantsDontContainSatisfiableAnswer;

/**
 *
 * @author Diarsid
 */
public class VariantsQuestion implements Serializable {
    
    private final String question;
    private final List<Variant> variants;
    
    private VariantsQuestion(String question) {
        this.question = question;
        this.variants = new ArrayList<>();
    }
    
    private VariantsQuestion(String question, List<Variant> variants) {
        this.question = question;
        this.variants = variants;
    }
    
    public static VariantsQuestion question(String question) {
        return new VariantsQuestion(question);
    }
    
    public VariantsQuestion withAnswerVariant(Variant variant) {
        this.variants.add(variant);
        return this;
    }
    
    public VariantsQuestion withAnswerString(String variant) {
        this.variants.add(new Variant(variant, this.variants.size()));
        return this;
    }
    
    public VariantsQuestion withAnswerEntity(ConvertableToVariant convertable) {
        this.variants.add(convertable.toVariant(this.variants.size()));
        return this;
    }
    
    public VariantsQuestion withAnswerStrings(List<String> variants) {
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
    
    public VariantsQuestion withAnswerStrings(String... variants) {
        AtomicInteger indexer = new AtomicInteger(0);
        this.variants.addAll(stream(variants)
                .map(variantString -> 
                        new Variant(
                                variantString, 
                                indexer.getAndIncrement()))
                .collect(toList()));
        return this;
    }
    
    public VariantsQuestion withAnswerVariants(List<Variant> variants) {
        this.variants.addAll(variants);
        return this;
    }
    
    public VariantsQuestion withAnswerEntities(List<? extends ConvertableToVariant> variants) {
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
                    if ( variant.doesHaveName() ) {
                        return containsIgnoreCase(variant.name(), possibleFragment);
                    } else {
                        return containsIgnoreCase(variant.value(), possibleFragment);
                    }
                })
                .map(variant -> answerOfVariant(variant))
                .collect(toList());
        if ( hasOne(matches) ) {
            return getOne(matches);
        } else {
            return variantsDontContainSatisfiableAnswer();
        }
    }
    
    public Answer answerWith(int choiceNumber) {
        if ( this.isChoiceInVariantsNaturalRange(choiceNumber) ) {
            return answerOfVariant(this.variants.get(choiceNumber - 1));
        } else {
            return variantsDontContainSatisfiableAnswer();
        }
    }
}
