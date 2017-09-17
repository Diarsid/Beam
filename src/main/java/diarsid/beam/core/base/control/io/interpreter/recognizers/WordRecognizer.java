/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.NodeRecognizer;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isStrictSimilar;
import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.ArgsExpectation.EXPECTS_MORE_ARGS;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.EmptyArgsTolerance.TOLERATE_EMPTY_ARGS;
import static diarsid.beam.core.base.util.StringUtils.lower;


public class WordRecognizer extends NodeRecognizer {
    
    private final String controlWord;
    private final ArgsExpectation moreArgsExpectation;
    private final EmptyArgsTolerance emptyArgsTolerance;
    
    WordRecognizer(
            EmptyArgsTolerance emptyArgsTolerance, 
            ArgsExpectation moreArgsExpectation, 
            String controlWord) {
        this.emptyArgsTolerance = emptyArgsTolerance;
        this.moreArgsExpectation = moreArgsExpectation;
        this.controlWord = lower(controlWord).trim();
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            if ( this.moreArgsExpectation.equals(EXPECTS_MORE_ARGS) ) {
                if ( input.hasMoreArgsAfterCurrent() ) {
                    if ( this.currentArgIsControlWord(input) ) {
                        return super.delegateRecognitionForwardIncorrectIfUndefined(input.toNextArg());
                    } else {
                        return undefinedCommand();
                    }                    
                } else {
                    if ( this.emptyArgsTolerance.equals(TOLERATE_EMPTY_ARGS) ) {                        
                        if ( this.currentArgIsControlWord(input) ) {
                            return super.delegateRecognitionForwardIncorrectIfUndefined(input.toNextArg());
                        } else {
                            return undefinedCommand();
                        }
                    } else {
                        return undefinedCommand();
                    }
                }
            } else {
                if ( input.hasMoreArgsAfterCurrent() ) {
                    return undefinedCommand();
                } else {
                    if ( this.currentArgIsControlWord(input) ) {
                        return super.delegateRecognitionForwardIncorrectIfUndefined(input.toNextArg());
                    } else {
                        return undefinedCommand();
                    }
                }
            }            
        } else {
            return undefinedCommand();
        }
    }

    private boolean currentArgIsControlWord(Input input) {
        return this.controlWord.equalsIgnoreCase(input.currentArg()) ||
               isStrictSimilar(this.controlWord, input.currentArg());
    }
}
