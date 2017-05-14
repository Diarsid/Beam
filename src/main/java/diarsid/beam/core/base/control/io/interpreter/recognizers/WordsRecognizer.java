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

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;


public class WordsRecognizer extends NodeRecognizer {
    
    private final Set<String> contorlWords;
    
    WordsRecognizer(String... controlWords) {
        this.contorlWords = unmodifiableSet(stream(controlWords)
                .map(word -> lower(word).trim())
                .collect(toSet()));
    }

    @Override
    public Command assess(Input input) {
        if ( this.currentArgIsControlWord(input) ) {
            return super.delegateRecognitionForward(input.toNextArg());
        } else {
            return undefinedCommand();
        }
    }

    private boolean currentArgIsControlWord(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                containsWordInIgnoreCase(this.contorlWords, lower(input.currentArg()));
    }
}
