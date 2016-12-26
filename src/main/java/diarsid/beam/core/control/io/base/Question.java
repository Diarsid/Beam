/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static diarsid.beam.core.control.io.base.Answer.answerOf;
import static diarsid.beam.core.control.io.base.Answer.noAnswer;

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
    
    public Answer answerWith(int choiceNumber) {
        if ( this.isChoiceInVariantsNaturalRange(choiceNumber) ) {
            return answerOf(this.variants.get(choiceNumber - 1));
        } else {
            return noAnswer();
        }
    }
}
