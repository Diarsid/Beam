/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import static diarsid.beam.core.base.control.io.base.interaction.NotGivenAnswer.HELP_REQUEST_ANSWER;
import static diarsid.beam.core.base.control.io.base.interaction.NotGivenAnswer.REJECTED_ANSWER;
import static diarsid.beam.core.base.control.io.base.interaction.NotGivenAnswer.UNSATISFIED_ANSWER;

/**
 *
 * @author Diarsid
 */
public class Answers {    
    
    public static Answer variantsDontContainSatisfiableAnswer() {
        return UNSATISFIED_ANSWER;
    }
    
    public static Answer rejectedAnswer() {
        return REJECTED_ANSWER;
    }
    
    public static Answer helpRequestAnswer() {
        return HELP_REQUEST_ANSWER;
    }
    
    public static Answer answerOfVariant(Variant variant) {
        return new GivenAnswer(variant);
    }
}
