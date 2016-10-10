/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.Arrays;
import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
class OperationCandidatesResolver {
    
    private final IoInnerModule ioEngine;
    
    OperationCandidatesResolver(IoInnerModule ioEngine) {
        this.ioEngine = ioEngine;
    }
    
    String resolve(String operation, String variant1, String variant2) {
        return this.askUserToResolveOperationCandidates(operation, variant1, variant2);
    }
    
    private String resolveQuestion(String question, List<String> variants) {
        int chosen = this.ioEngine.resolveVariants(
                question, variants);
        if ( chosen > 0 ) {
            return variants.get(chosen - 1);
        } else {
            return "";
        }
    }
    
    private String askUserToResolveOperationCandidates(
            String operation, String variant1, String variant2) {
        return this.resolveQuestion(
                "choose action for '" + operation + "' command:", 
                Arrays.asList(new String[] {variant1, variant2})
        );  
    }
}
