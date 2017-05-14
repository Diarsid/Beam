/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.NodeRecognizer;

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.base.util.PathUtils.isAcceptableRelativePath;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;


public class RelativePathRecognizer extends NodeRecognizer {
    
    RelativePathRecognizer() {
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
