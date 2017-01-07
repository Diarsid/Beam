/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.control.io.base.VariantAnswer.answerOfVariant;
import static diarsid.beam.core.control.io.base.VariantAnswer.noAnswerFromVariants;
import static diarsid.beam.core.util.CollectionsUtils.containsOne;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class VariantsQuestion implements Serializable {
    
    private final String question;
    private final List<Variant> variants;
    
    public VariantsQuestion(String question) {
        this.question = question;
        this.variants = new ArrayList<>();
    }
    
    public VariantsQuestion(String question, List<? extends ConvertableToVariant> variants) {
        this.question = question;
        this.variants = variants
                .stream()
                .map(convertable -> convertable.convertToVariant())
                .collect(toList());
    }
    
    public VariantsQuestion with(Variant variant) {
        this.variants.add(variant);
        return this;
    }
    
    public VariantsQuestion with(String variant) {
        this.variants.add(new Variant(variant));
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
    
    public VariantAnswer ifPartOfAnyVariant(String possibleFragment) {
        List<VariantAnswer> matches = this.variants
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
        if ( containsOne(matches) ) {
            return getOne(matches);
        } else {
            return noAnswerFromVariants();
        }
    }
    
    public VariantAnswer answerWith(int choiceNumber) {
        if ( this.isChoiceInVariantsNaturalRange(choiceNumber) ) {
            return answerOfVariant(this.variants.get(choiceNumber - 1));
        } else {
            return noAnswerFromVariants();
        }
    }
}
