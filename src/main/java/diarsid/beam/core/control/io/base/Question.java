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

import static diarsid.beam.core.control.io.base.Answer.answerOf;
import static diarsid.beam.core.control.io.base.Answer.noAnswer;
import static diarsid.beam.core.util.CollectionsUtils.containsOne;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.util.CollectionsUtils.getOne;

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
    
    public Question with(Variant variant) {
        this.variants.add(variant);
        return this;
    }
    
    public Question with(String variant) {
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
    
    public Answer ifPartOfAnyVariant(String possibleFragment) {
        List<Answer> matches = this.variants
                .stream()
                .filter(variant -> { 
                    if ( variant.hasDisplayText() ) {
                        return containsIgnoreCase(variant.getDisplay(), possibleFragment);
                    } else {
                        return containsIgnoreCase(variant.get(), possibleFragment);
                    }
                })
                .map(variant -> answerOf(variant)).collect(toList());
        if ( containsOne(matches) ) {
            return getOne(matches);
        } else {
            return noAnswer();
        }
    }
    
    public Answer answerWith(int choiceNumber) {
        if ( this.isChoiceInVariantsNaturalRange(choiceNumber) ) {
            return answerOf(this.variants.get(choiceNumber - 1));
        } else {
            return noAnswer();
        }
    }
}
