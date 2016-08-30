/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.List;

import static diarsid.beam.core.modules.executor.commandscache.ActionChoice.formulateChoiceFor;
import static diarsid.beam.core.modules.executor.commandscache.ActionRequest.actionRequestOf;

/**
 *
 * @author Diarsid
 */
public class FakeChoiceProducer {
    
    public FakeChoiceProducer() {
    }
    
    public static ActionChoice choice(String arg, List<String> variants, String madeChoice) {
        return formulateChoiceFor(actionRequestOf(arg, variants), madeChoice);
    }
    
    public static ActionRequest request(String arg, List<String> variants) {
        return actionRequestOf(arg, variants);
    }
}
