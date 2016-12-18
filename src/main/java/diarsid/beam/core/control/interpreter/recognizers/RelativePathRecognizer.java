/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.util.PathUtils.isAcceptableRelativePath;
import static diarsid.beam.core.util.PathUtils.normalizeSeparators;


public class RelativePathRecognizer extends NodeRecognizer {
    
    public RelativePathRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( this.argIsMeaningfulPath(input) ) {
            input.resetCurrentArg(normalizeSeparators(input.currentArg()));
            return super.delegateRecognitionForward(input);
        } else {
            return undefinedCommand();
        }
    }

    private boolean argIsMeaningfulPath(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                isAcceptableRelativePath(input.currentArg());
    }
}