/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import java.util.Set;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.NodeRecognizer;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.analyze.similarity.Similarity.hasStrictSimilar;
import static diarsid.beam.core.base.control.io.commands.EmptyCommand.incorrectCommand;
import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.ArgsExpectation.EXPECTS_MORE_ARGS;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.EmptyArgsTolerance.TOLERATE_EMPTY_ARGS;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;


public class WordsRecognizer extends NodeRecognizer {
    
    private final Set<String> controlWords;
    private final ArgsExpectation moreArgsExpectation;
    private final EmptyArgsTolerance emptyArgsTolerance;
    
    WordsRecognizer(
            EmptyArgsTolerance emptyArgsTolerance, 
            ArgsExpectation moreArgsExpectation, 
            String... controlWords) {
        this.moreArgsExpectation = moreArgsExpectation;
        this.emptyArgsTolerance = emptyArgsTolerance;
        this.controlWords = unmodifiableSet(stream(controlWords)
                .map(word -> lower(word).trim())
                .collect(toSet()));
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
                        if ( this.currentArgIsControlWord(input) ) {
                            return incorrectCommand();
                        } else {
                            return undefinedCommand();
                        }
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
        return containsWordInIgnoreCase(this.controlWords, input.currentArg()) ||
               hasStrictSimilar(this.controlWords, input.currentArg());
    }
}
