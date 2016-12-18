/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.executor.OpenPathCommand;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.util.PathUtils.isAcceptableRelativePath;


public class OpenPathRecognizer extends NodeRecognizer {
    
    public OpenPathRecognizer() {
    }
    
    private boolean pathArgIsMeaningful(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                isAcceptableRelativePath(input.currentArg());
    }

    @Override
    public Command assess(Input input) {
        if ( this.pathArgIsMeaningful(input) ) {
            return new OpenPathCommand(input.currentArg());
        } else {
            return undefinedCommand();
        }
    }
}
