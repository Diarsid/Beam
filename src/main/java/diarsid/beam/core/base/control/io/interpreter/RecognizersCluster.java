/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import java.util.Set;
import java.util.TreeSet;

import diarsid.beam.core.base.control.io.commands.Command;

import static java.util.Arrays.asList;

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;

/**
 *
 * @author Diarsid
 */
class RecognizersCluster implements Recognizer {
    
    private final Set<PrioritizedRecognizer> childRecognizers;
    
    RecognizersCluster(PrioritizedRecognizer... recognizers) {
        this.childRecognizers = new TreeSet<>();
        this.childRecognizers.addAll(asList(recognizers));
    }
        
    @Override
    public final Command assess(Input input) {
        if ( this.childRecognizers.isEmpty() ) {
            return undefinedCommand();
        } else if ( this.childRecognizers.size() == 1 ) {
            return this.childRecognizers.iterator().next().assess(input);
        } else {
            Command command;
            for (Recognizer recognizer : this.childRecognizers) {
                command = recognizer.assess(input);
                if ( command.type().isDefined() ) {
                    return command;
                }
            }
            return undefinedCommand();
//            return this.childRecognizers
//                .stream()
//                .map(recognizer -> recognizer.assess(input))
//                .filter(command -> command.type().isDefined())
//                .findFirst()
//                .orElse(undefinedCommand());
        }  
    }
}
